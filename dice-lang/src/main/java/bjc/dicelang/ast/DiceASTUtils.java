package bjc.dicelang.ast;

import bjc.utils.funcdata.ITree;

import bjc.dicelang.IDiceExpression;
import bjc.dicelang.ScalarDie;
import bjc.dicelang.ast.nodes.DiceASTType;
import bjc.dicelang.ast.nodes.DiceLiteralNode;
import bjc.dicelang.ast.nodes.IDiceASTNode;
import bjc.dicelang.ast.nodes.ILiteralDiceNode;
import bjc.dicelang.ast.nodes.IntegerLiteralNode;

/**
 * Functions that are useful when dealing with dice ASTs
 * 
 * @author ben
 *
 */
public class DiceASTUtils {
	/**
	 * Check if a dice AST contains a simple variable reference
	 * 
	 * @param nameTree
	 * @return Whether or not a dice AST contains a simple variable
	 *         reference
	 */
	public static boolean containsSimpleVariable(
			ITree<IDiceASTNode> nameTree) {
		return nameTree.transformHead((nameNod) -> {
			if (nameNod.getType() != DiceASTType.VARIABLE) {
				return false;
			}

			return true;
		});
	}

	/**
	 * Convert an AST tree to a dice expression, if possible.
	 * 
	 * @param tree
	 *            The tree to convert
	 * @return The tree as a dice expression
	 * 
	 * @throws ClassCastException
	 *             if the head of the tree is not a literal (implements
	 *             {@link ILiteralDiceNode})
	 * @throws UnsupportedOperationException
	 *             if the head of the tree is not optimizable
	 */
	public static IDiceExpression toExpression(ITree<IDiceASTNode> tree) {
		ILiteralDiceNode litNode = (ILiteralDiceNode) tree.getHead();

		switch (litNode.getLiteralType()) {
			case DICE:
				return ((DiceLiteralNode) litNode).getValue();
			case INTEGER:
				return new ScalarDie(
						((IntegerLiteralNode) litNode).getValue());
			default:
				throw new UnsupportedOperationException(
						"This type of literal isn't convertable to an expression");
		}
	}

	/**
	 * Convert an AST tree to an integer, if possible.
	 * 
	 * @param tree
	 *            The tree to convert
	 * @return The tree as an integer
	 * 
	 * @throws ClassCastException
	 *             if the head of the tree is not a literal (implements
	 *             {@link ILiteralDiceNode})
	 * @throws UnsupportedOperationException
	 *             if the head of the tree is not optimizable
	 */
	public static int toInt(ITree<IDiceASTNode> tree) {
		return tree.transformHead((node) -> {
			return ((ILiteralDiceNode) node).optimize();
		});
	}
}
