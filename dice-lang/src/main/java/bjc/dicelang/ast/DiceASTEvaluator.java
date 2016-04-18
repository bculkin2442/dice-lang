package bjc.dicelang.ast;

import bjc.dicelang.ComplexDice;
import bjc.dicelang.ast.nodes.DiceASTType;
import bjc.dicelang.ast.nodes.DiceLiteralNode;
import bjc.dicelang.ast.nodes.DiceLiteralType;
import bjc.dicelang.ast.nodes.IDiceASTNode;
import bjc.dicelang.ast.nodes.ILiteralDiceNode;
import bjc.dicelang.ast.nodes.IntegerLiteralNode;
import bjc.dicelang.ast.nodes.OperatorDiceNode;
import bjc.dicelang.ast.nodes.VariableDiceNode;
import bjc.utils.data.IPair;
import bjc.utils.data.LazyPair;
import bjc.utils.data.Pair;
import bjc.utils.funcdata.FunctionalMap;
import bjc.utils.funcdata.IFunctionalList;
import bjc.utils.funcdata.IFunctionalMap;
import bjc.utils.funcdata.ITree;
import bjc.utils.funcdata.Tree;

/**
 * Evaluate a dice AST to an integer value
 * 
 * @author ben
 *
 */
public class DiceASTEvaluator {
	/**
	 * Build the map of operations to use when collapsing the AST
	 * 
	 * @param enviroment
	 *            The enviroment to evaluate bindings and such against
	 * @return The operations to use when collapsing the AST
	 */
	private static IFunctionalMap<IDiceASTNode, IOperatorCollapser>
			buildOperations(
					IFunctionalMap<String, ITree<IDiceASTNode>> enviroment) {
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

		operatorCollapsers.put(OperatorDiceNode.ASSIGN, (nodes) -> {
			return parseBinding(enviroment, nodes);
		});

		operatorCollapsers.put(OperatorDiceNode.COMPOUND,
				DiceASTEvaluator::parseCompound);

		operatorCollapsers.put(OperatorDiceNode.GROUP,
				DiceASTEvaluator::parseGroup);

		return operatorCollapsers;
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
	public static int evaluateAST(ITree<IDiceASTNode> expression,
			IFunctionalMap<String, ITree<IDiceASTNode>> enviroment) {
		IFunctionalMap<IDiceASTNode, IOperatorCollapser> collapsers =
				buildOperations(enviroment);

		return expression.collapse(
				(node) -> evaluateLeaf(node, enviroment), collapsers::get,
				(pair) -> pair.getLeft());
	}

	private static IPair<Integer, ITree<IDiceASTNode>> evaluateLeaf(
			IDiceASTNode leafNode,
			IFunctionalMap<String, ITree<IDiceASTNode>> enviroment) {
		ITree<IDiceASTNode> returnedAST = new Tree<>(leafNode);

		switch (leafNode.getType()) {
			case LITERAL:
				return new Pair<>(evaluateLiteral(leafNode), returnedAST);

			case VARIABLE:
				return new LazyPair<>(() -> {
					return bindLiteralValue(leafNode, enviroment);
				}, () -> returnedAST);

			case OPERATOR:
			default:
				throw new UnsupportedOperationException(
						"Node '" + leafNode + "' cannot be a leaf.");
		}
	}

	private static Integer bindLiteralValue(IDiceASTNode leafNode,
			IFunctionalMap<String, ITree<IDiceASTNode>> enviroment) {
		String variableName = ((VariableDiceNode) leafNode).getVariable();

		if (enviroment.containsKey(variableName)) {
			int result =
					evaluateAST(enviroment.get(variableName), enviroment);

			return result;
		}

		// Value to allow for assignments
		return 0;
	}

	private static int evaluateLiteral(IDiceASTNode leafNode) {
		DiceLiteralType literalType =
				((ILiteralDiceNode) leafNode).getLiteralType();

		switch (literalType) {
			case DICE:
				return ((DiceLiteralNode) leafNode).getValue();
			case INTEGER:
				return ((IntegerLiteralNode) leafNode).getValue();
			default:
				throw new UnsupportedOperationException("Literal value '"
						+ leafNode + "' is of a type (" + literalType
						+ ") not currently supported.");
		}
	}

	private static IPair<Integer, ITree<IDiceASTNode>> parseBinding(
			IFunctionalMap<String, ITree<IDiceASTNode>> enviroment,
			IFunctionalList<IPair<Integer, ITree<IDiceASTNode>>> nodes) {
		if (nodes.getSize() != 2) {
			throw new UnsupportedOperationException(
					"Can only bind nodes with two children. Problem children are "
							+ nodes);
		}

		IPair<Integer, ITree<IDiceASTNode>> nameNode = nodes.getByIndex(0);
		IPair<Integer, ITree<IDiceASTNode>> valueNode =
				nodes.getByIndex(1);

		// Force valueNode to materialize for debugging purposes
		valueNode.merge((l, r) -> null);

		return nameNode.bindRight((nameTree) -> {
			return valueNode.bind((valueValue, valueTree) -> {
				if (containsSimpleVariable(nameTree)) {
					String varName = nameTree.transformHead((nameNod) -> {
						return ((VariableDiceNode) nameNod).getVariable();
					});

					enviroment.put(varName, valueTree);

					return new Pair<>(valueValue, nameTree);
				}

				throw new IllegalStateException(
						"Statement that shouldn't be hit was hit.");
			});
		});
	}

	private static boolean
			containsSimpleVariable(ITree<IDiceASTNode> nameTree) {
		return nameTree.transformHead((nameNod) -> {
			if (nameNod.getType() != DiceASTType.VARIABLE) {
				throw new UnsupportedOperationException(
						"Assigning to complex variables isn't supported. Problem node is "
								+ nameNod);
			}

			return true;
		});
	}

	private static IPair<Integer, ITree<IDiceASTNode>> parseCompound(
			IFunctionalList<IPair<Integer, ITree<IDiceASTNode>>> nodes) {
		if (nodes.getSize() != 2) {
			throw new UnsupportedOperationException(
					"Can only form a group from two dice");
		}

		IPair<Integer, ITree<IDiceASTNode>> leftDiceNode =
				nodes.getByIndex(0);
		IPair<Integer, ITree<IDiceASTNode>> rightDiceNode =
				nodes.getByIndex(1);

		return leftDiceNode.bind((leftDiceValue, leftDiceTree) -> {
			return rightDiceNode.bind((rightDiceValue, rightDiceTree) -> {
				Integer result =
						Integer.parseInt(Integer.toString(leftDiceValue)
								+ Integer.toString(rightDiceValue));

				return new Pair<>(result,
						new Tree<>(OperatorDiceNode.GROUP, leftDiceTree,
								rightDiceTree));
			});
		});
	}

	private static IPair<Integer, ITree<IDiceASTNode>> parseGroup(
			IFunctionalList<IPair<Integer, ITree<IDiceASTNode>>> nodes) {
		if (nodes.getSize() != 2) {
			throw new UnsupportedOperationException(
					"Can only form a group from two dice");
		}

		IPair<Integer, ITree<IDiceASTNode>> numberDiceNode =
				nodes.getByIndex(0);
		IPair<Integer, ITree<IDiceASTNode>> diceTypeNode =
				nodes.getByIndex(1);

		return numberDiceNode.bind((numberDiceValue, numberDiceTree) -> {
			return diceTypeNode.bind((diceTypeValue, diceTypeTree) -> {
				ComplexDice cDice =
						new ComplexDice(numberDiceValue, diceTypeValue);

				return new Pair<>(cDice.roll(),
						new Tree<>(OperatorDiceNode.GROUP, numberDiceTree,
								diceTypeTree));
			});
		});
	}
}
