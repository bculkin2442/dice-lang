package bjc.dicelang.ast;

import bjc.dicelang.ComplexDice;
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
				new ArithmeticCollapser(OperatorDiceNode.COMPOUND,
						(left, right) -> {
							return Integer.parseInt(Integer.toString(left)
									+ Integer.toString(right));
						}));

		operatorCollapsers.put(OperatorDiceNode.GROUP,
				DiceASTEvaluator::parseGroup);

		operatorCollapsers.put(OperatorDiceNode.LET, (nodes) -> {
			return parseLet(enviroment, nodes);
		});

		return operatorCollapsers;
	}

	private static IPair<Integer, ITree<IDiceASTNode>> parseLet(
			IFunctionalMap<String, ITree<IDiceASTNode>> enviroment,
			IFunctionalList<IPair<Integer, ITree<IDiceASTNode>>> nodes) {
		if (nodes.getSize() != 2) {
			throw new UnsupportedOperationException(
					"Can only use let with two expressions.");
		}

		ITree<IDiceASTNode> bindTree = nodes.getByIndex(0).getRight();
		ITree<IDiceASTNode> expressionTree =
				nodes.getByIndex(1).getRight();

		IFunctionalMap<String, ITree<IDiceASTNode>> letEnviroment =
				enviroment.extend();

		evaluateAST(bindTree, letEnviroment);
		int exprResult = evaluateAST(expressionTree, letEnviroment);

		IFunctionalList<ITree<IDiceASTNode>> childrn =
				nodes.map((pair) -> pair.getRight());

		return new Pair<>(exprResult,
				new Tree<>(OperatorDiceNode.LET, childrn));
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

		throw new UnsupportedOperationException(
				"Attempted to dereference unbound variable "
						+ variableName);
	}

	private static int evaluateLiteral(IDiceASTNode leafNode) {
		DiceLiteralType literalType =
				((ILiteralDiceNode) leafNode).getLiteralType();

		switch (literalType) {
			case DICE:
				return ((DiceLiteralNode) leafNode).getValue().roll();
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
				if (DiceASTUtils.containsSimpleVariable(nameTree)) {
					String varName = nameTree.transformHead((nameNod) -> {
						return ((VariableDiceNode) nameNod).getVariable();
					});

					enviroment.put(varName, valueTree);

					return new Pair<>(valueValue, nameTree);
				}

				throw new UnsupportedOperationException(
						"Assigning to complex variables isn't supported. Problem node is "
								+ nameNode.getRight());
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
