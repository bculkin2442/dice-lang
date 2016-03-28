package bjc.utils.dice.ast;

import java.util.Map;

import bjc.utils.parserutils.AST;

/**
 * Freeze references in a dice AST, replacing variable references with what
 * the variables refer to
 * 
 * @author ben
 *
 */
public class DiceASTFreezer {
	/**
	 * Expand a reference
	 * 
	 * @param vnode
	 *            The node containing the reference to expand
	 * @param env
	 *            The enviroment to expand against
	 * @return The expanded reference
	 */
	private static AST<IDiceASTNode> expandNode(VariableDiceNode vnode,
			Map<String, AST<IDiceASTNode>> env) {
		return env.get(vnode.getVariable());
	}

	/**
	 * Expand a reference
	 * 
	 * @param vnode
	 *            The node containing the reference to expand
	 * @param env
	 *            The enviroment to expand against
	 * @return The expanded reference
	 */
	private static AST<IDiceASTNode> expandNode2(VariableDiceNode vnode,
			Map<String, DiceASTExpression> env) {
		return env.get(vnode.getVariable()).getAst();
	}

	/**
	 * Freeze the references in an AST
	 * 
	 * @param tree
	 *            The tree to freeze references in
	 * @param env
	 *            The enviroment to get reference values from
	 * @return The tree with references frozen
	 */
	@SuppressWarnings("unused")
	public static AST<IDiceASTNode> freezeAST(AST<IDiceASTNode> tree,
			Map<String, AST<IDiceASTNode>> env) {
		return tree.collapse((nod) -> {
			if (nod instanceof VariableDiceNode) {
				return expandNode((VariableDiceNode) nod, env);
			} else {
				// Type is specified here so compiler can know the type
				// we're using
				return new AST<IDiceASTNode>(nod);
			}
		} , (op) -> (left, right) -> {
			return new AST<IDiceASTNode>(op, left, right);
		} , (r) -> r);
	}

	/**
	 * Freeze the references in an expression backed by an AST
	 * 
	 * @param tree
	 *            The tree-backed expression to freeze references in
	 * @param env
	 *            The enviroment to get reference values from
	 * @return The tree with references frozen
	 */
	@SuppressWarnings("unused")
	public static AST<IDiceASTNode> freezeAST(DiceASTExpression tree,
			Map<String, DiceASTExpression> env) {
		return tree.getAst().collapse((nod) -> {
			if (nod instanceof VariableDiceNode) {
				return expandNode2((VariableDiceNode) nod, env);
			} else {
				// Type is specified here so compiler can know the type
				// we're using
				return new AST<IDiceASTNode>(nod);
			}
		} , (op) -> (left, right) -> {
			return new AST<IDiceASTNode>(op, left, right);
		} , (r) -> r);
	}
}
