package bjc.dicelang.ast;

import java.util.function.BinaryOperator;

import bjc.dicelang.ast.nodes.IDiceASTNode;
import bjc.dicelang.ast.nodes.OperatorDiceNode;
import bjc.utils.data.IPair;
import bjc.utils.data.Pair;
import bjc.utils.funcdata.IFunctionalList;
import bjc.utils.funcdata.ITree;
import bjc.utils.funcdata.Tree;

/**
 * Responsible for collapsing arithmetic operators
 * 
 * @author ben
 *
 */
final class ArithmeticCollapser
		implements IOperatorCollapser {
	private OperatorDiceNode		type;

	private BinaryOperator<Integer>	valueOp;

	public ArithmeticCollapser(OperatorDiceNode type,
			BinaryOperator<Integer> valueOp) {
		this.type = type;
		this.valueOp = valueOp;
	}

	@Override
	public IPair<Integer, ITree<IDiceASTNode>> apply(
			IFunctionalList<IPair<Integer, ITree<IDiceASTNode>>> nodes) {
		IPair<Integer, ITree<IDiceASTNode>> initState =
				new Pair<>(0, new Tree<>(type));

		BinaryOperator<IPair<Integer, ITree<IDiceASTNode>>> reducer =
				(accumulatedState, currentState) -> {
					return reduceStates(accumulatedState,
							currentState);
				};

		return nodes.reduceAux(initState, reducer, (state) -> state);
	}

	private IPair<Integer, ITree<IDiceASTNode>> reduceStates(
			IPair<Integer, ITree<IDiceASTNode>> accumulatedState,
			IPair<Integer, ITree<IDiceASTNode>> currentState) {
		return accumulatedState
				.bind((accumulatedValue, accumulatedTree) -> {
					return currentState
							.bind((currentValue, currentTree) -> {
								accumulatedTree.addChild(currentTree);

								Integer combinedValue =
										valueOp.apply(accumulatedValue,
												currentValue);

								return new Pair<>(combinedValue,
										accumulatedTree);
							});
				});
	}
}