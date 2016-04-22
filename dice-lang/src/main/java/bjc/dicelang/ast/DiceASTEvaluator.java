package bjc.dicelang.ast;

import java.util.function.Supplier;

import bjc.utils.data.IHolder;
import bjc.utils.data.IPair;
import bjc.utils.data.Identity;
import bjc.utils.data.LazyPair;
import bjc.utils.data.Pair;
import bjc.utils.funcdata.FunctionalList;
import bjc.utils.funcdata.FunctionalMap;
import bjc.utils.funcdata.IFunctionalList;
import bjc.utils.funcdata.IFunctionalMap;
import bjc.utils.funcdata.ITree;
import bjc.utils.funcdata.Tree;

import bjc.dicelang.ComplexDice;
import bjc.dicelang.ast.nodes.DiceASTType;
import bjc.dicelang.ast.nodes.DiceLiteralNode;
import bjc.dicelang.ast.nodes.DiceLiteralType;
import bjc.dicelang.ast.nodes.IDiceASTNode;
import bjc.dicelang.ast.nodes.ILiteralDiceNode;
import bjc.dicelang.ast.nodes.IntegerLiteralNode;
import bjc.dicelang.ast.nodes.OperatorDiceNode;
import bjc.dicelang.ast.nodes.VariableDiceNode;

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
	private static IFunctionalMap<IDiceASTNode, IOperatorCollapser> buildOperations(
			IFunctionalMap<String, ITree<IDiceASTNode>> enviroment) {
		IFunctionalMap<IDiceASTNode, IOperatorCollapser> operatorCollapsers = new FunctionalMap<>();

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

		operatorCollapsers.put(OperatorDiceNode.ARRAY, (nodes) -> {

			// This is so that arrays respect lazy results properly
			Supplier<IResult> resultSupplier = () -> {
				IFunctionalList<IResult> resultList = new FunctionalList<>();

				nodes.forEach((node) -> {
					resultList.add(node.getLeft());
				});

				return new ArrayResult(resultList);
			};

			Supplier<ITree<IDiceASTNode>> treeSupplier = () -> {
				ITree<IDiceASTNode> returnedTree = new Tree<>(
						OperatorDiceNode.ARRAY);

				nodes.forEach((element) -> {
					returnedTree.addChild(element.getRight());
				});

				return returnedTree;
			};

			return new LazyPair<>(resultSupplier, treeSupplier);
		});

		return operatorCollapsers;
	}

	private static IPair<IResult, ITree<IDiceASTNode>> parseLet(
			IFunctionalMap<String, ITree<IDiceASTNode>> enviroment,
			IFunctionalList<IPair<IResult, ITree<IDiceASTNode>>> nodes) {
		if (nodes.getSize() != 2) {
			throw new UnsupportedOperationException(
					"Can only use let with two expressions.");
		}

		ITree<IDiceASTNode> bindTree = nodes.getByIndex(0).getRight();
		ITree<IDiceASTNode> expressionTree = nodes.getByIndex(1)
				.getRight();

		IFunctionalMap<String, ITree<IDiceASTNode>> letEnviroment = enviroment
				.extend();

		evaluateAST(bindTree, letEnviroment);
		IResult exprResult = evaluateAST(expressionTree, letEnviroment);

		IFunctionalList<ITree<IDiceASTNode>> childrn = nodes
				.map((pair) -> pair.getRight());

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
	public static IResult evaluateAST(ITree<IDiceASTNode> expression,
			IFunctionalMap<String, ITree<IDiceASTNode>> enviroment) {
		IFunctionalMap<IDiceASTNode, IOperatorCollapser> collapsers = buildOperations(
				enviroment);

		return expression.collapse(
				(node) -> evaluateLeaf(node, enviroment), collapsers::get,
				(pair) -> pair.getLeft());
	}

	private static IPair<IResult, ITree<IDiceASTNode>> evaluateLeaf(
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

	private static IResult bindLiteralValue(IDiceASTNode leafNode,
			IFunctionalMap<String, ITree<IDiceASTNode>> enviroment) {
		String variableName = ((VariableDiceNode) leafNode).getVariable();

		if (enviroment.containsKey(variableName)) {
			IResult result = evaluateAST(enviroment.get(variableName),
					enviroment);

			return result;
		}

		throw new UnsupportedOperationException(
				"Attempted to deref unbound variable " + variableName);
	}

	private static IResult evaluateLiteral(IDiceASTNode leafNode) {
		DiceLiteralType literalType = ((ILiteralDiceNode) leafNode)
				.getLiteralType();

		switch (literalType) {
			case DICE:
				int diceRoll = ((DiceLiteralNode) leafNode).getValue()
						.roll();

				return new IntegerResult(diceRoll);
			case INTEGER:
				int val = ((IntegerLiteralNode) leafNode).getValue();

				return new IntegerResult(val);
			default:
				throw new UnsupportedOperationException("Literal value '"
						+ leafNode + "' is of a type (" + literalType
						+ ") not currently supported.");
		}
	}

	private static IPair<IResult, ITree<IDiceASTNode>> parseBinding(
			IFunctionalMap<String, ITree<IDiceASTNode>> enviroment,
			IFunctionalList<IPair<IResult, ITree<IDiceASTNode>>> nodes) {
		if (nodes.getSize() != 2) {
			throw new UnsupportedOperationException(
					"Can only bind nodes with two children. Problem children are "
							+ nodes);
		}

		IPair<IResult, ITree<IDiceASTNode>> nameNode = nodes.getByIndex(0);
		IPair<IResult, ITree<IDiceASTNode>> valueNode = nodes
				.getByIndex(1);

		return nameNode.bindRight((nameTree) -> {
			return valueNode.bind((valueValue, valueTree) -> {
				if (DiceASTUtils.containsSimpleVariable(nameTree)) {
					String varName = nameTree.transformHead((nameNod) -> {
						return ((VariableDiceNode) nameNod).getVariable();
					});

					enviroment.put(varName, valueTree);

					return new Pair<>(valueValue, nameTree);
				} else if (nameTree.getHead() == OperatorDiceNode.ARRAY) {
					if (valueTree.getHead() == OperatorDiceNode.ARRAY) {
						if (nameTree.getChildrenCount() != valueTree
								.getChildrenCount()) {
							throw new UnsupportedOperationException(
									"Array assignment must be between two equal length arrays");
						}

						IHolder<Integer> childCount = new Identity<>(0);

						nameTree.doForChildren((child) -> {
							doArrayAssign(enviroment, nameNode, nameTree,
									valueTree, childCount, child);

							childCount.transform(val -> val + 1);
						});

						return new Pair<>(valueValue, nameTree);
					}

					nameTree.doForChildren((child) -> {
						String varName = child.transformHead((nameNod) -> {
							return ((VariableDiceNode) nameNod)
									.getVariable();
						});

						enviroment.put(varName, valueTree);
					});

					return new Pair<>(valueValue, nameTree);
				}

				throw new UnsupportedOperationException(
						"Assigning to complex variables isn't supported. Problem node is "
								+ nameNode.getRight());
			});
		});
	}

	private static void doArrayAssign(
			IFunctionalMap<String, ITree<IDiceASTNode>> enviroment,
			IPair<IResult, ITree<IDiceASTNode>> nameNode,
			ITree<IDiceASTNode> nameTree, ITree<IDiceASTNode> valueTree,
			IHolder<Integer> childCount, ITree<IDiceASTNode> child) {
		if (nameTree.getHead().getType() != DiceASTType.VARIABLE) {
			throw new UnsupportedOperationException(
					"Assigning to complex variables isn't supported. Problem node is "
							+ nameNode.getRight());
		}

		String varName = child.transformHead((nameNod) -> {
			return ((VariableDiceNode) nameNod).getVariable();
		});

		enviroment.put(varName, valueTree.getChild(childCount.getValue()));

		childCount.transform(val -> val + 1);
	}

	private static IPair<IResult, ITree<IDiceASTNode>> parseGroup(
			IFunctionalList<IPair<IResult, ITree<IDiceASTNode>>> nodes) {
		if (nodes.getSize() != 2) {
			throw new UnsupportedOperationException(
					"Can only form a group from two dice");
		}

		IPair<IResult, ITree<IDiceASTNode>> numberDiceNode = nodes
				.getByIndex(0);
		IPair<IResult, ITree<IDiceASTNode>> diceTypeNode = nodes
				.getByIndex(1);

		return numberDiceNode.bind((numberDiceValue, numberDiceTree) -> {
			return diceTypeNode.bind((diceTypeValue, diceTypeTree) -> {
				ComplexDice cDice = new ComplexDice(
						((IntegerResult) numberDiceValue).getValue(),
						((IntegerResult) diceTypeValue).getValue());

				return new Pair<>(new IntegerResult(cDice.roll()),
						new Tree<>(OperatorDiceNode.GROUP, numberDiceTree,
								diceTypeTree));
			});
		});
	}
}
