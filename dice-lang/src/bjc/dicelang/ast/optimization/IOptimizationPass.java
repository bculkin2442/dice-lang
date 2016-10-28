package bjc.dicelang.ast.optimization;

import bjc.utils.funcdata.IList;
import bjc.utils.funcdata.ITree;

import bjc.dicelang.ast.nodes.IDiceASTNode;

/**
 * Represents a pass of optimizations over a dice AST
 * 
 * @author ben
 *
 */
public interface IOptimizationPass {
	/**
	 * Optimize a leaf in the tree
	 * 
	 * @param leafNode
	 *            The node to optimize
	 * @return The optimized node
	 */
	public ITree<IDiceASTNode> optimizeLeaf(IDiceASTNode leafNode);

	/**
	 * Optimize an operator in an AST node
	 * 
	 * @param operator
	 *            The operator being optimized
	 * @param children
	 *            The children of the operator being optimized
	 * @return The optimized node
	 */
	public ITree<IDiceASTNode> optimizeOperator(IDiceASTNode operator,
			IList<ITree<IDiceASTNode>> children);
}
