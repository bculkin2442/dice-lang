package bjc.dicelang.ast;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import bjc.dicelang.ComplexDice;
import bjc.dicelang.CompoundDice;
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
				(leftNode, rightNode) -> {
					return leftNode.merge((leftValue, leftAST) -> {
						return rightNode.merge((rightValue, rightAST) -> {
							return new Pair<>(leftValue + rightValue,
									new AST<>(OperatorDiceNode.ADD,
											leftAST, rightAST));
						});
					});

				});
		operatorCollapsers.put(OperatorDiceNode.SUBTRACT,
				(leftNode, rightNode) -> {
					return leftNode.merge((leftValue, leftAST) -> {
						return rightNode.merge((rightValue, rightAST) -> {
							return new Pair<>(leftValue - rightValue,
									new AST<>(OperatorDiceNode.SUBTRACT,
											leftAST, rightAST));
						});
					});
				});

		operatorCollapsers.put(OperatorDiceNode.MULTIPLY,
				(leftNode, rightNode) -> {
					return leftNode.merge((leftValue, leftAST) -> {
						return rightNode.merge((rightValue, rightAST) -> {
							return new Pair<>(leftValue * rightValue,
									new AST<>(OperatorDiceNode.MULTIPLY,
											leftAST, rightAST));
						});
					});

				});
		operatorCollapsers.put(OperatorDiceNode.DIVIDE,
				(leftNode, rightNode) -> {
					return leftNode.merge((leftValue, leftAST) -> {
						return rightNode.merge((rightValue, rightAST) -> {
							if (rightValue == 0) {
								throw new ArithmeticException(
										"Attempted to divide by zero. The AST of the problem expression is "
												+ rightAST);
							}

							return new Pair<>(leftValue / rightValue,
									new AST<>(OperatorDiceNode.DIVIDE,
											leftAST, rightAST));
						});
					});
				});

		operatorCollapsers.put(OperatorDiceNode.ASSIGN, (left, right) -> {
			return parseBinding(enviroment, left, right);
		});

		operatorCollapsers.put(OperatorDiceNode.COMPOUND,
				DiceASTExpression::parseCompound);

		operatorCollapsers.put(OperatorDiceNode.GROUP,
				DiceASTExpression::parseGroup);

		return operatorCollapsers;
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
	 * @param tokn
	 *            The token to evaluate
	 * @return A pair consisting of the token's value and the AST it
	 *         represents
	 */
	private Pair<Integer, AST<IDiceASTNode>> evalLeaf(IDiceASTNode tokn) {
		if (tokn.getType() == DiceASTType.VARIABLE) {
			return parseVariable(tokn);
		} else if (tokn.getType() == DiceASTType.LITERAL) {
			return parseLiteral(tokn);
		} else {
			throw new UnsupportedOperationException("Found leaf operator "
					+ tokn + ". These aren't supported.");
		}
	}

	private static Pair<Integer, AST<IDiceASTNode>>
			parseLiteral(IDiceASTNode tokn) {
		LiteralDiceNode literalNode = (LiteralDiceNode) tokn;
		String dat = literalNode.getData();

		if (isValidInfixOperator(dat, "c")) {
			String[] strangs = dat.split("c");

			return new Pair<>(new CompoundDice(strangs).roll(),
					new AST<>(tokn));
		} else if (isValidInfixOperator(dat, "d")) {
			/*
			 * Handle dice groups
			 */
			return new Pair<>(ComplexDice.fromString(dat).roll(),
					new AST<>(tokn));
		} else {
			try {
				return new Pair<>(Integer.parseInt(dat), new AST<>(tokn));
			} catch (NumberFormatException nfex) {
				throw new UnsupportedOperationException(
						"Found malformed leaf token " + tokn);
			}
		}
	}

	private static boolean isValidInfixOperator(String dat, String op) {
		return StringUtils.countMatches(dat, op) == 1
				&& !dat.equalsIgnoreCase(op) && !dat.startsWith(op);
	}

	private Pair<Integer, AST<IDiceASTNode>>
			parseVariable(IDiceASTNode tokn) {
		String varName = ((VariableDiceNode) tokn).getVariable();

		if (env.containsKey(varName)) {
			return new Pair<>(env.get(varName).roll(), new AST<>(tokn));
		} else {
			// Handle special case for defining variables
			return new Pair<>(0, new AST<>(tokn));
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

		return ast.collapse(this::evalLeaf, operations::get,
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
}