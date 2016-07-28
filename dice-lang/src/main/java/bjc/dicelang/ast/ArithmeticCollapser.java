package bjc.dicelang.ast;

import java.util.function.BinaryOperator;

import bjc.utils.data.IPair;
import bjc.utils.data.Pair;
import bjc.utils.funcdata.IList;
import bjc.utils.funcdata.ITree;
import bjc.utils.funcdata.Tree;

import bjc.dicelang.ast.nodes.IDiceASTNode;
import bjc.dicelang.ast.nodes.OperatorDiceNode;

/**
 * Responsible for collapsing arithmetic operators
 * 
 * @author ben
 *
 */
final class ArithmeticCollapser implements IOperatorCollapser {
	// The type of operator we're collapsing
	private OperatorDiceNode		type;

	// The operator to use to collapse operators
	private BinaryOperator<Integer>	valueOp;

	private int						initialValue;

	public ArithmeticCollapser(OperatorDiceNode type,
			BinaryOperator<Integer> valueOp, int initVal) {
		this.type = type;
		this.valueOp = valueOp;
		this.initialValue = initVal;
	}

	@Override
	public IPair<IResult, ITree<IDiceASTNode>> apply(
			IList<IPair<IResult, ITree<IDiceASTNode>>> nodes) {
		IPair<IResult, ITree<IDiceASTNode>> initialState = new Pair<>(
				new IntegerResult(initialValue), new Tree<>(type));

		BinaryOperator<IPair<IResult, ITree<IDiceASTNode>>> reducer = (
				currentState, accumulatedState) -> {
			// Force evaluation of accumulated state to prevent
			// certain bugs from occuring
			//accumulatedState.merge((l, r) -> null);

			return reduceStates(accumulatedState, currentState);
		};

		IPair<IResult, ITree<IDiceASTNode>> reducedState = nodes
				.reduceAux(initialState, reducer, (state) -> state);

		return reducedState;
	}

	private IList<IResult> combineArrayResults(IResult accumulatedValue,
			IResult currentValue) {
		IList<IResult> currentList = ((ArrayResult) currentValue)
				.getValue();
		IList<IResult> accumulatedList = ((ArrayResult) accumulatedValue)
				.getValue();

		if (currentList.getSize() != accumulatedList.getSize()) {
			throw new UnsupportedOperationException(
					"Can only apply operations to equal-length arrays");
		}

		IList<IResult> resultList = currentList.combineWith(
				accumulatedList, (currentNode, accumulatedNode) -> {
					boolean currentNotInt = currentNode
							.getType() != ResultType.INTEGER;
					boolean accumulatedNotInt = accumulatedNode
							.getType() != ResultType.INTEGER;

					if (currentNotInt || accumulatedNotInt) {
						throw new UnsupportedOperationException(
								"Nesting of array operations isn't allowed");
					}

					int accumulatedInt = ((IntegerResult) accumulatedNode)
							.getValue();
					int currentInt = ((IntegerResult) currentNode)
							.getValue();

					IResult combinedValue = new IntegerResult(
							valueOp.apply(accumulatedInt, currentInt));
					return combinedValue;
				});
		return resultList;
	}

	private IPair<IResult, ITree<IDiceASTNode>> doArithmeticCollapse(
			IResult accumulatedValue, ITree<IDiceASTNode> accumulatedTree,
			IResult currentValue) {
		if (accumulatedValue.getType() == ResultType.DUMMY
				|| currentValue.getType() == ResultType.DUMMY) {
			DummyResult result = new DummyResult(
					"Found dummy result with either accumulated dummy ("
							+ ((DummyResult) accumulatedValue).getData()
							+ ") or current dummy ("
							+ ((DummyResult) currentValue).getData()
							+ ").");
			
			return new Pair<>(result, accumulatedTree);
		}

		boolean currentIsInt = currentValue
				.getType() == ResultType.INTEGER;
		boolean accumulatedIsInt = accumulatedValue
				.getType() == ResultType.INTEGER;

		if (!currentIsInt) {
			if (!accumulatedIsInt) {
				IList<IResult> resultList = combineArrayResults(
						accumulatedValue, currentValue);

				return new Pair<>(new ArrayResult(resultList),
						accumulatedTree);
			}

			IList<IResult> resultList = halfCombineLists(
					((ArrayResult) currentValue).getValue(),
					accumulatedValue, true);

			return new Pair<>(new ArrayResult(resultList),
					accumulatedTree);
		} else if (!accumulatedIsInt) {
			IList<IResult> resultList = halfCombineLists(
					((ArrayResult) accumulatedValue).getValue(),
					currentValue, false);

			return new Pair<>(new ArrayResult(resultList),
					accumulatedTree);
		}

		int accumulatedInt = ((IntegerResult) accumulatedValue).getValue();
		int currentInt = ((IntegerResult) currentValue).getValue();

		int combinedValue = valueOp.apply(accumulatedInt, currentInt);

		return new Pair<>(new IntegerResult(combinedValue),
				accumulatedTree);
	}

	private IList<IResult> halfCombineLists(IList<IResult> list,
			IResult scalar, boolean scalarLeft) {
		if (scalar.getType() != ResultType.INTEGER) {
			throw new UnsupportedOperationException(
					"Nested array operations not supported");
		}

		int scalarInt = ((IntegerResult) scalar).getValue();

		return list.map((element) -> {
			if (element.getType() != ResultType.INTEGER) {
				throw new UnsupportedOperationException(
						"Nested array operations not supported");
			}

			int elementInt = ((IntegerResult) element).getValue();

			IResult combinedValue;

			if (scalarLeft) {
				combinedValue = new IntegerResult(
						valueOp.apply(scalarInt, elementInt));
			} else {
				combinedValue = new IntegerResult(
						valueOp.apply(elementInt, scalarInt));
			}

			return combinedValue;
		});
	}

	private IPair<IResult, ITree<IDiceASTNode>> reduceStates(
			IPair<IResult, ITree<IDiceASTNode>> accumulatedState,
			IPair<IResult, ITree<IDiceASTNode>> currentState) {
		return accumulatedState
				.bind((accumulatedValue, accumulatedTree) -> {
					return currentState
							.bind((currentValue, currentTree) -> {
								accumulatedTree.addChild(currentTree);

								return doArithmeticCollapse(
										accumulatedValue, accumulatedTree,
										currentValue);
							});
				});
	}
}