package bjc.dicelang.ast.optimization;

import java.util.function.BinaryOperator;

import bjc.utils.data.ITree;
import bjc.utils.funcdata.IList;
import bjc.utils.data.Tree;

import bjc.dicelang.ast.DiceASTUtils;
import bjc.dicelang.ast.nodes.DiceASTType;
import bjc.dicelang.ast.nodes.IDiceASTNode;
import bjc.dicelang.ast.nodes.ILiteralDiceNode;
import bjc.dicelang.ast.nodes.IntegerLiteralNode;
import bjc.dicelang.ast.nodes.OperatorDiceNode;

class ArithmeticCollapser {
	private BinaryOperator<Integer>	reducer;
	private OperatorDiceNode		type;

	public ArithmeticCollapser(BinaryOperator<Integer> reducr,
			OperatorDiceNode typ) {
		reducer = reducr;
		this.type = typ;
	}

	public ITree<IDiceASTNode> collapse(
			IList<ITree<IDiceASTNode>> children) {
		boolean allConstant = children.allMatch((subtree) -> {
			return subtree.transformHead((node) -> {
				if (node.getType() == DiceASTType.LITERAL) {
					return ((ILiteralDiceNode) node).canOptimize();
				}

				return false;
			});
		});

		if (!allConstant) {
			return new Tree<>(type, children);
		}

		int initState = DiceASTUtils.literalToInteger(children.first());

		return children.tail().reduceAux(initState,
				(currentNode, state) -> {
					return reducer.apply(state,
							DiceASTUtils.literalToInteger(currentNode));
				}, (state) -> new Tree<>(new IntegerLiteralNode(state)));
	}
}
