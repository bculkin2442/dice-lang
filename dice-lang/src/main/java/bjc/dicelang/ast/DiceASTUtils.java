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
	 *            The tree to check for a reference in
	 * @return Whether or not a dice AST contains a simple variable
	 *         reference
	 */
	public static boolean containsSimpleVariable(
			ITree<IDiceASTNode> nameTree) {
		return nameTree.transformHead((nameNode) -> {
			if (nameNode.getType() != DiceASTType.VARIABLE) {
				return false;
			}

			return true;
		});
	}

	/**
	 * Convert an literal AST node to a dice expression, if possible.
	 * 
	 * @param tree
	 *            The node to convert in tree form
	 * @return The tree as a dice expression
	 * 
	 * @throws ClassCastException
	 *             if the head of the tree is not a literal (implements
	 *             {@link ILiteralDiceNode})
	 * @throws UnsupportedOperationException
	 *             if the head of the tree is not optimizable
	 */
	public static IDiceExpression literalToExpression(
			ITree<IDiceASTNode> tree) {
		ILiteralDiceNode literalNode = (ILiteralDiceNode) tree.getHead();

		switch (literalNode.getLiteralType()) {
			case DICE:
				return ((DiceLiteralNode) literalNode).getValue();
			case INTEGER:
				return new ScalarDie(
						((IntegerLiteralNode) literalNode).getValue());
			default:
				throw new UnsupportedOperationException(
						"This type of literal isn't convertable to an expression");
		}
	}

	/**
	 * Convert an literal AST node to an integer, if possible.
	 * 
	 * @param tree
	 *            The literal node to convert, as a tree
	 * @return The node as an integer
	 * 
	 * @throws ClassCastException
	 *             if the head of the tree is not a literal (implements
	 *             {@link ILiteralDiceNode})
	 * @throws UnsupportedOperationException
	 *             if the head of the tree is not optimizable
	 */
	public static int literalToInteger(ITree<IDiceASTNode> tree) {
		return tree.transformHead((node) -> {
			return ((ILiteralDiceNode) node).optimize();
		});
	}
}
