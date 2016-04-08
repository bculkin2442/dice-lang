package bjc.dicelang.old.ast;

import java.util.function.Function;

import bjc.dicelang.ast.nodes.DiceASTType;
import bjc.dicelang.ast.nodes.IDiceASTNode;
import bjc.dicelang.ast.nodes.VariableDiceNode;
import bjc.utils.funcdata.FunctionalList;
import bjc.utils.funcdata.FunctionalMap;
import bjc.utils.funcdata.IFunctionalList;
import bjc.utils.funcdata.IFunctionalMap;
import bjc.utils.parserutils.AST;

/**
 * Inline references in a dice AST, replacing variable references with what
 * the variables refer to
 * 
 * @author ben
 *
 */
public class DiceASTInliner {
	private static class NodeInliner
			implements Function<IDiceASTNode, AST<IDiceASTNode>> {
		private IFunctionalMap<String, AST<IDiceASTNode>> enviroment;

		public NodeInliner(IFunctionalMap<String, AST<IDiceASTNode>> env) {
			enviroment = env;
		}

		@Override
		public AST<IDiceASTNode> apply(IDiceASTNode nod) {
			if (nod.getType() == DiceASTType.VARIABLE) {
				return expandNode((VariableDiceNode) nod);
			}

			return new AST<>(nod);
		}

		protected AST<IDiceASTNode> expandNode(
				VariableDiceNode variableNode) {
			String varName = variableNode.getVariable();

			if (!enviroment.containsKey(varName)) {
				throw new IllegalArgumentException(
						"Attempted to freeze reference"
								+ " to an undefined variable " + varName);
			}

			return enviroment.get(varName);
		}
	}

	private static final class SelectiveInliner extends NodeInliner {

		private IFunctionalList<String> variableNames;

		public SelectiveInliner(
				IFunctionalMap<String, AST<IDiceASTNode>> env,
				IFunctionalList<String> varNames) {
			super(env);

			variableNames = varNames;
		}

		@Override
		protected AST<IDiceASTNode> expandNode(
				VariableDiceNode variableNode) {
			if (variableNames.contains(variableNode.getVariable())) {
				return super.expandNode(variableNode);
			}

			return new AST<>(variableNode);
		}
	}

	/**
	 * Inline the references in an AST
	 * 
	 * @param tree
	 *            The tree to inline references in
	 * @param env
	 *            The enviroment to get reference values from
	 * @return The tree with references inlined
	 */
	public static AST<IDiceASTNode> inlineAST(AST<IDiceASTNode> tree,
			IFunctionalMap<String, AST<IDiceASTNode>> env) {
		return selectiveInline(tree, env);
	}

	/**
	 * Inline the references in an expression backed by an AST
	 * 
	 * @param tree
	 *            The tree-backed expression to inline references in
	 * @param env
	 *            The enviroment to get reference values from
	 * @return The tree with references inlined
	 */
	public static AST<IDiceASTNode> inlineAST(DiceASTExpression tree,
			FunctionalMap<String, DiceASTExpression> env) {
		return inlineAST(tree.getAst(),
				env.mapValues(expression -> expression.getAst()));
	}

	/**
	 * Inline references to specified variables
	 * 
	 * @param tree
	 *            The tree-backed expression to inline references in
	 * @param env
	 *            The enviroment to resolve variables against
	 * @param varNames
	 *            The names of the variables to inline
	 * @return An AST with the specified variables inlined
	 */
	public static AST<IDiceASTNode> selectiveInline(AST<IDiceASTNode> tree,
			IFunctionalMap<String, AST<IDiceASTNode>> env,
			String... varNames) {
		return selectiveInline(tree, env, new FunctionalList<>(varNames));
	}

	/**
	 * Inline references to specified variables
	 * 
	 * @param tree
	 *            The tree-backed expression to inline references in
	 * @param env
	 *            The enviroment to resolve variables against
	 * @param varNames
	 *            The names of the variables to inline
	 * @return An AST with the specified variables inline
	 */
	public static AST<IDiceASTNode> selectiveInline(AST<IDiceASTNode> tree,
			IFunctionalMap<String, AST<IDiceASTNode>> env,
			IFunctionalList<String> varNames) {
		return tree.flatMapTree(new SelectiveInliner(env, varNames));
	}
}