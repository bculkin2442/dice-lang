package bjc.dicelang.old.ast;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import bjc.dicelang.ComplexDice;
import bjc.dicelang.IDiceExpression;
import bjc.dicelang.ast.nodes.DiceASTType;
import bjc.dicelang.ast.nodes.IDiceASTNode;
import bjc.dicelang.ast.nodes.LiteralDiceNode;
import bjc.dicelang.ast.nodes.OperatorDiceNode;
import bjc.dicelang.ast.nodes.VariableDiceNode;
import bjc.utils.data.GenHolder;
import bjc.utils.data.Pair;
import bjc.utils.funcdata.bst.ITreePart.TreeLinearizationMethod;
import bjc.utils.parserutils.AST;

/**
 * An implementation of {@link IDiceExpression} backed by an AST of
 * {@link IDiceASTNode}s
 * 
 * @author ben
 *
 */
public class DiceASTExpression implements IDiceExpression {
	private static final class ArithmeticCollapser
			implements IOperatorCollapser {
		private OperatorDiceNode		type;

		private BinaryOperator<Integer>	valueOp;

		public ArithmeticCollapser(OperatorDiceNode type,
				BinaryOperator<Integer> valueOp) {
			this.type = type;
			this.valueOp = valueOp;
		}

		@Override
		public Pair<Integer, AST<IDiceASTNode>> apply(
				Pair<Integer, AST<IDiceASTNode>> leftNode,
				Pair<Integer, AST<IDiceASTNode>> rightNode) {
			return leftNode.merge((leftValue, leftAST) -> {
				return rightNode.merge((rightValue, rightAST) -> {
					if (type == OperatorDiceNode.DIVIDE
							&& rightValue == 0) {
						throw new ArithmeticException(
								"Attempted to divide by zero. The AST of the problem expression is "
										+ rightAST);
					}

					return new Pair<>(valueOp.apply(leftValue, rightValue),
							new AST<>(type, leftAST, rightAST));
				});
			});
		}
	}

	private static final class VariableRetriever
			implements Function<IDiceASTNode, String> {
		@Override
		public String apply(IDiceASTNode node) {
			if (node.getType() != DiceASTType.VARIABLE) {
				throw new UnsupportedOperationException(
						"Attempted to assign to something that isn't a variable."
								+ " This isn't supported yet. The problem node is "
								+ node);
			}

			return ((VariableDiceNode) node).getVariable();
		}
	}

	/**
	 * Build the map of operations to use when collapsing the AST
	 * 
	 * @param enviroment
	 *            The enviroment to evaluate bindings and such against
	 * @return The operations to use when collapsing the AST
	 */
	private static Map<IDiceASTNode, IOperatorCollapser>
			buildOperations(Map<String, DiceASTExpression> enviroment) {
		Map<IDiceASTNode, IOperatorCollapser> operatorCollapsers =
				new HashMap<>();

		operatorCollapsers.put(OperatorDiceNode.ADD,
				new ArithmeticCollapser(OperatorDiceNode.ADD,
						(left, right) -> left + right));

		operatorCollapsers.put(OperatorDiceNode.SUBTRACT,
				new ArithmeticCollapser(OperatorDiceNode.SUBTRACT,
						(left, right) -> left - right));

		operatorCollapsers.put(OperatorDiceNode.MULTIPLY,
				new ArithmeticCollapser(OperatorDiceNode.MULTIPLY,
						(left, right) -> left * right));

		operatorCollapsers.put(OperatorDiceNode.DIVIDE,
				new ArithmeticCollapser(OperatorDiceNode.DIVIDE,
						(left, right) -> left / right));

		operatorCollapsers.put(OperatorDiceNode.ASSIGN, (left, right) -> {
			return parseBinding(enviroment, left, right);
		});

		operatorCollapsers.put(OperatorDiceNode.COMPOUND,
				DiceASTExpression::parseCompound);

		operatorCollapsers.put(OperatorDiceNode.GROUP,
				DiceASTExpression::parseGroup);

		operatorCollapsers.put(OperatorDiceNode.LET, (left, right) -> {
			return doLet(enviroment, left, right);
		});

		return operatorCollapsers;
	}

	private static Pair<Integer, AST<IDiceASTNode>> doLet(
			Map<String, DiceASTExpression> enviroment,
			Pair<Integer, AST<IDiceASTNode>> left,
			Pair<Integer, AST<IDiceASTNode>> right) {
		return left.merge((leftValue, leftAST) -> {
			return right.merge((rightValue, rightAST) -> {
				if (!leftAST
						.applyToHead(DiceASTExpression::isAssignNode)) {
					// Just ignore the left block then
					return new Pair<>(rightValue, rightAST);
				} else {
					// Left block has an assignment to handle
					String varName = leftAST.applyToLeft((leftBranch) -> {
						return getAssignmentVar(leftBranch);
					});

					return null;
				}
			});
		});
	}

	private static String getAssignmentVar(AST<IDiceASTNode> leftBranch) {
		return leftBranch.applyToHead((node) -> {
			return ((VariableDiceNode) node).getVariable();
		});
	}

	private static Boolean isAssignNode(IDiceASTNode node) {
		return node.getType() == DiceASTType.OPERATOR
				&& node == OperatorDiceNode.ASSIGN;
	}

	private static Pair<Integer, AST<IDiceASTNode>> parseBinding(
			Map<String, DiceASTExpression> enviroment,
			Pair<Integer, AST<IDiceASTNode>> left,
			Pair<Integer, AST<IDiceASTNode>> right) {
		return left.merge((leftValue, leftAST) -> {
			return right.merge((rightValue, rightAST) -> {
				String variableName = leftAST
						.collapse(new VariableRetriever(), (operator) -> {
							throw new UnsupportedOperationException(
									"Can only assign to plain variable names. The problem operator is "
											+ operator);
						}, (returnedAST) -> returnedAST);

				GenHolder<Boolean> selfReference = new GenHolder<>(false);

				DiceASTReferenceChecker refChecker =
						new DiceASTReferenceChecker(selfReference,
								variableName);

				rightAST.traverse(TreeLinearizationMethod.PREORDER,
						refChecker);

				// Ignore meta-variable that'll be auto-frozen to restore
				// definition sanity
				if (selfReference.unwrap((bool) -> bool)
						&& !variableName.equals("last")) {
					throw new UnsupportedOperationException(
							"Variable '" + variableName
									+ "' references itself. Problematic definition: \n\t"
									+ rightAST);
				}

				if (!variableName.equals("last")) {
					enviroment.put(variableName,
							new DiceASTExpression(rightAST, enviroment));
				} else {
					// Do nothing, last is a auto-handled meta-variable
				}

				return new Pair<>(rightValue, new AST<>(
						OperatorDiceNode.ASSIGN, leftAST, rightAST));
			});
		});
	}

	private static Pair<Integer, AST<IDiceASTNode>> parseCompound(
			Pair<Integer, AST<IDiceASTNode>> leftNode,
			Pair<Integer, AST<IDiceASTNode>> rightNode) {
		return leftNode.merge((leftValue, leftAST) -> {
			return rightNode.merge((rightValue, rightAST) -> {
				int compoundValue =
						Integer.parseInt(Integer.toString(leftValue)
								+ Integer.toString(rightValue));

				return new Pair<>(compoundValue, new AST<>(
						OperatorDiceNode.COMPOUND, leftAST, rightAST));
			});
		});
	}

	private static Pair<Integer, AST<IDiceASTNode>> parseGroup(
			Pair<Integer, AST<IDiceASTNode>> leftNode,
			Pair<Integer, AST<IDiceASTNode>> rightNode) {
		return leftNode.merge((leftValue, leftAST) -> {
			return rightNode.merge((rightValue, rightAST) -> {
				if (leftValue < 0) {
					throw new UnsupportedOperationException(
							"Can't attempt to roll a negative number of dice."
									+ " The problematic AST is "
									+ leftAST);
				} else if (rightValue < 1) {
					throw new UnsupportedOperationException(
							"Can't roll dice with less than one side."
									+ " The problematic AST is "
									+ rightAST);
				}

				int rolledValue =
						new ComplexDice(leftValue, rightValue).roll();

				return new Pair<>(rolledValue, new AST<>(
						OperatorDiceNode.GROUP, leftAST, rightAST));
			});
		});
	}

	/**
	 * The AST this expression will evaluate
	 */
	private AST<IDiceASTNode>				ast;

	/**
	 * The enviroment to evaluate bindings and such against
	 */
	private Map<String, DiceASTExpression>	env;

	/**
	 * Create a new dice expression backed by an AST
	 * 
	 * @param ast
	 *            The AST backing this expression
	 * @param env
	 *            The enviroment to evaluate bindings against
	 */
	public DiceASTExpression(AST<IDiceASTNode> ast,
			Map<String, DiceASTExpression> env) {
		this.ast = ast;
		this.env = env;
	}

	/**
	 * Expand a leaf AST token into a pair for evaluation
	 * 
	 * @param leafNode
	 *            The token to evaluate
	 * @return A pair consisting of the token's value and the AST it
	 *         represents
	 */
	private Pair<Integer, AST<IDiceASTNode>>
			evaluateLeaf(IDiceASTNode leafNode) {
		if (leafNode.getType() == DiceASTType.VARIABLE) {
			VariableDiceNode node = (VariableDiceNode) leafNode;

			return parseVariable(node);
		} else if (leafNode.getType() == DiceASTType.LITERAL) {
			LiteralDiceNode node = (LiteralDiceNode) leafNode;

			return node.toParseValue();
		} else {
			throw new UnsupportedOperationException("Found leaf operator "
					+ leafNode + ". These aren't supported.");
		}
	}

	private Pair<Integer, AST<IDiceASTNode>>
			parseVariable(VariableDiceNode leafNode) {
		String varName = leafNode.getVariable();

		if (env.containsKey(varName)) {
			return new Pair<>(env.get(varName).roll(),
					new AST<>(leafNode));
		} else {
			// Handle special case for defining variables
			return new Pair<>(0, new AST<>(leafNode));
		}
	}

	/**
	 * Get the AST bound to this expression
	 * 
	 * @return the ast
	 */
	public AST<IDiceASTNode> getAst() {
		return ast;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see bjc.utils.dice.IDiceExpression#roll()
	 */
	@Override
	public int roll() {
		Map<IDiceASTNode, IOperatorCollapser> operations =
				buildOperations(env);

		return ast.collapse(this::evaluateLeaf, operations::get,
				(returnedValue) -> returnedValue
						.merge((left, right) -> left));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ast.toString();
	}

	@Override
	public int optimize() {
		throw new UnsupportedOperationException(
				"Use DiceASTOptimizer for optimizing these");
	}

	@Override
	public boolean canOptimize() {
		return false;
	}
}