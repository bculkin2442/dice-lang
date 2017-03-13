package bjc.dicelang.v1.ast;

import bjc.dicelang.v1.ast.nodes.IDiceASTNode;
import bjc.dicelang.v1.ast.optimization.IOptimizationPass;
import bjc.utils.data.ITree;
import bjc.utils.funcdata.FunctionalList;
import bjc.utils.funcdata.IList;
import bjc.utils.funcdata.IMap;

/**
 * Contains optimizations appliable to a dice AST
 * 
 * @author ben
 *
 */
public class DiceASTOptimizer {
	private IList<IOptimizationPass> passes;

	/**
	 * Create a new optimizer
	 */
	public DiceASTOptimizer() {
		passes = new FunctionalList<>();
	}

	/**
	 * Add a pass to the list of optimization passes
	 * 
	 * @param pass
	 *                The pass to add
	 */
	public void addPass(IOptimizationPass pass) {
		passes.add(pass);
	}

	/**
	 * Optimize the passed in tree
	 * 
	 * @param ast
	 *                The tree to optimize
	 * @param enviroment
	 *                The enviroment for variable references
	 * @return The optimized tree
	 */
	public ITree<IDiceASTNode> optimizeTree(ITree<IDiceASTNode> ast, IMap<String, ITree<IDiceASTNode>> enviroment) {
		ITree<IDiceASTNode> optimizedTree = passes.reduceAux(ast, (currentPass, currentTree) -> {
			return currentTree.collapse(currentPass::optimizeLeaf, (operator) -> {
				return (nodes) -> {
					return currentPass.optimizeOperator(operator, nodes);
				};
			}, (tree) -> tree);
		}, (tree) -> tree);
		return optimizedTree;
	}
}
