package bjc.dicelang.ast;

import java.util.function.BinaryOperator;

import bjc.dicelang.ComplexDice;

import bjc.dicelang.ast.nodes.DiceASTType;
import bjc.dicelang.ast.nodes.DiceLiteralNode;
import bjc.dicelang.ast.nodes.DiceLiteralType;
import bjc.dicelang.ast.nodes.IDiceASTNode;
import bjc.dicelang.ast.nodes.ILiteralDiceNode;
import bjc.dicelang.ast.nodes.IntegerLiteralNode;
import bjc.dicelang.ast.nodes.OperatorDiceNode;
import bjc.dicelang.ast.nodes.VariableDiceNode;

import bjc.utils.data.GenHolder;
import bjc.utils.data.IPair;
import bjc.utils.data.Pair;
import bjc.utils.funcdata.FunctionalMap;
import bjc.utils.funcdata.IFunctionalMap;
import bjc.utils.funcdata.bst.ITreePart.TreeLinearizationMethod;
import bjc.utils.parserutils.AST;

/**
 * Evaluate a dice AST to an integer value
 * 
 * @author ben
 *
 */
public class DiceASTEvaluator {
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
		public IPair<Integer, AST<IDiceASTNode>> apply(
				IPair<Integer, AST<IDiceASTNode>> leftNode,
				IPair<Integer, AST<IDiceASTNode>> rightNode) {
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

	/**
	 * Evaluate the provided AST to a numeric value
	 * 
	 * @param expression
	 *            The expression to evaluate
	 * @param enviroment
	 *            The enviroment to look up variables in
	 * @return The integer value of the expression
	 */
	public static int evaluateAST(AST<IDiceASTNode> expression,
			IFunctionalMap<String, AST<IDiceASTNode>> enviroment) {
		IFunctionalMap<IDiceASTNode, IOperatorCollapser> collapsers =
				buildOperations(enviroment);

		return expression.collapse((node) -> {
			return evaluateLeaf(node, enviroment);
		}, collapsers::get, (pair) -> {
			return pair.merge((left, right) -> left);
		});
	}

	/**
	 * Build the map of operations to use when collapsing the AST
	 * 
	 * @param enviroment
	 *            The enviroment to evaluate bindings and such against
	 * @return The operations to use when collapsing the AST
	 */
	private static IFunctionalMap<IDiceASTNode, IOperatorCollapser>
			buildOperations(
					IFunctionalMap<String, AST<IDiceASTNode>> enviroment) {
		IFunctionalMap<IDiceASTNode, IOperatorCollapser> operatorCollapsers =
				new FunctionalMap<>();

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
				DiceASTEvaluator::parseCompound);

		operatorCollapsers.put(OperatorDiceNode.GROUP,
				DiceASTEvaluator::parseGroup);

		return operatorCollapsers;
	}

	private static IPair<Integer, AST<IDiceASTNode>> parseBinding(
			IFunctionalMap<String, AST<IDiceASTNode>> enviroment,
			IPair<Integer, AST<IDiceASTNode>> left,
			IPair<Integer, AST<IDiceASTNode>> right) {
		return left.merge((leftValue, leftAST) -> {
			return right.merge((rightValue, rightAST) -> {
				String variableName = leftAST.applyToHead((node) -> {
					if (node.getType() != DiceASTType.VARIABLE) {
						throw new UnsupportedOperationException(
								"Attempted to assign to '" + node
										+ "' which is not a variable");
					}

					return ((VariableDiceNode) node).getVariable();
				});

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
					enviroment.put(variableName, rightAST);
				} else {
					// Do nothing, last is a auto-handled meta-variable
				}

				return new Pair<>(rightValue, new AST<>(
						OperatorDiceNode.ASSIGN, leftAST, rightAST));
			});
		});
	}

	private static IPair<Integer, AST<IDiceASTNode>> parseCompound(
			IPair<Integer, AST<IDiceASTNode>> leftNode,
			IPair<Integer, AST<IDiceASTNode>> rightNode) {
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

	private static IPair<Integer, AST<IDiceASTNode>> parseGroup(
			IPair<Integer, AST<IDiceASTNode>> leftNode,
			IPair<Integer, AST<IDiceASTNode>> rightNode) {
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

	private static IPair<Integer, AST<IDiceASTNode>> evaluateLeaf(
			IDiceASTNode leafNode,
			IFunctionalMap<String, AST<IDiceASTNode>> enviroment) {
		int returnedValue = 0;

		switch (leafNode.getType()) {
			case LITERAL:
				returnedValue = evaluateLiteral(leafNode, returnedValue);

				break;
			case VARIABLE:
				String variableName =
						((VariableDiceNode) leafNode).getVariable();

				returnedValue = evaluateAST(enviroment.get(variableName),
						enviroment);
				break;
			case OPERATOR:
				throw new UnsupportedOperationException(
						"Operator '" + leafNode + "' cannot be a leaf.");
			default:
				break;

		}

		return new Pair<>(returnedValue, new AST<>(leafNode));
	}

	private static int evaluateLiteral(IDiceASTNode leafNode,
			int returnedValue) {
		DiceLiteralType literalType =
				((ILiteralDiceNode) leafNode).getLiteralType();

		switch (literalType) {
			case DICE:
				returnedValue = ((DiceLiteralNode) leafNode).getValue();
				break;
			case INTEGER:
				returnedValue = ((IntegerLiteralNode) leafNode).getValue();
				break;
			default:
				throw new UnsupportedOperationException("Literal value '"
						+ leafNode + "' is of a type (" + literalType
						+ ") not currently supported.");
		}
		return returnedValue;
	}
}
