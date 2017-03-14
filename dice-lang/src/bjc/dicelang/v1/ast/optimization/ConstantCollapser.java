package bjc.dicelang.v1.ast.optimization;

import bjc.dicelang.v1.ComplexDice;
import bjc.dicelang.v1.ast.DiceASTUtils;
import bjc.dicelang.v1.ast.nodes.IDiceASTNode;
import bjc.dicelang.v1.ast.nodes.IntegerLiteralNode;
import bjc.dicelang.v1.ast.nodes.OperatorDiceNode;
import bjc.utils.data.ITree;
import bjc.utils.data.Tree;
import bjc.utils.funcdata.IList;

/**
 * Collapses operations with constants to constants
 *
 * @author ben
 *
 */
public class ConstantCollapser implements IOptimizationPass {
	private static final ArithmeticCollapser additionCollapser = new ArithmeticCollapser(
			(left, right) -> left + right, OperatorDiceNode.ADD);

	private static final ArithmeticCollapser divideCollapser = new ArithmeticCollapser(
			(left, right) -> left / right, OperatorDiceNode.DIVIDE);

	private static final ArithmeticCollapser multiplyCollapser = new ArithmeticCollapser(
			(left, right) -> left * right, OperatorDiceNode.MULTIPLY);

	private static final ArithmeticCollapser subtractCollapser = new ArithmeticCollapser(
			(left, right) -> left - right, OperatorDiceNode.SUBTRACT);

	private static final ArithmeticCollapser compoundCollapser = new ArithmeticCollapser(
			(left, right) -> Integer.parseInt(Integer.toString(left) + Integer.toString(left)),
			OperatorDiceNode.COMPOUND);

	@Override
	public ITree<IDiceASTNode> optimizeLeaf(IDiceASTNode leafNode) {
		// We don't do anything special here
		return new Tree<>(leafNode);
	}

	@Override
	public ITree<IDiceASTNode> optimizeOperator(IDiceASTNode operator, IList<ITree<IDiceASTNode>> children) {
		if(!operator.isOperator()) return new Tree<>(operator, children);

		switch((OperatorDiceNode) operator) {
		case ADD:
			return additionCollapser.collapse(children);
		case DIVIDE:
			return divideCollapser.collapse(children);
		case MULTIPLY:
			return multiplyCollapser.collapse(children);
		case SUBTRACT:
			return subtractCollapser.collapse(children);
		case COMPOUND:
			return compoundCollapser.collapse(children);
		case GROUP:
			if(children.getSize() != 2) return new Tree<>(operator, children);

			ComplexDice dice = new ComplexDice(DiceASTUtils.literalToExpression(children.getByIndex(0)),
					DiceASTUtils.literalToExpression(children.getByIndex(1)));

			if(dice.canOptimize()) return new Tree<>(new IntegerLiteralNode(dice.optimize()));

			return new Tree<>(operator, children);
		case ARRAY:
			if(children.getSize() != 1) return new Tree<>(operator, children);

			return children.first();
		case ASSIGN:
		case LET:
		default:
			// We don't optimize these operators
			return new Tree<>(operator, children);
		}
	}
}
