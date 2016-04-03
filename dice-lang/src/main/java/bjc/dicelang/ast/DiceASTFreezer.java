package bjc.dicelang.ast;

import java.util.function.Function;

import bjc.utils.funcdata.FunctionalList;
import bjc.utils.funcdata.FunctionalMap;
import bjc.utils.parserutils.AST;

/**
 * Freeze references in a dice AST, replacing variable references with what
 * the variables refer to
 * 
 * @author ben
 *
 */
public class DiceASTFreezer {
	private static class NodeFreezer
			implements Function<IDiceASTNode, AST<IDiceASTNode>> {
		private FunctionalMap<String, AST<IDiceASTNode>> enviroment;

		public NodeFreezer(FunctionalMap<String, AST<IDiceASTNode>> env) {
			enviroment = env;
		}

		@Override
		public AST<IDiceASTNode> apply(IDiceASTNode nod) {
			if (nod.getType() == DiceASTType.VARIABLE) {
				return expandNode((VariableDiceNode) nod);
			} else {
				return new AST<>(nod);
			}
		}

		protected AST<IDiceASTNode>
				expandNode(VariableDiceNode variableNode) {
			String varName = variableNode.getVariable();

			if (!enviroment.containsKey(varName)) {
				throw new IllegalArgumentException(
						"Attempted to freeze reference"
								+ " to an undefined variable " + varName);
			}

			return enviroment.get(varName);
		}
	}

	private static final class SelectiveFreezer extends NodeFreezer {

		private FunctionalList<String> variableNames;

		public SelectiveFreezer(
				FunctionalMap<String, AST<IDiceASTNode>> env,
				FunctionalList<String> varNames) {
			super(env);
			variableNames = varNames;
		}

		@Override
		protected AST<IDiceASTNode>
				expandNode(VariableDiceNode variableNode) {
			if (variableNames.contains(variableNode.getVariable())) {
				return super.expandNode(variableNode);
			} else {
				return new AST<>(variableNode);
			}
		}
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
	public static AST<IDiceASTNode> freezeAST(AST<IDiceASTNode> tree,
			FunctionalMap<String, AST<IDiceASTNode>> env) {
		return selectiveFreeze(tree, env);
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
	public static AST<IDiceASTNode> freezeAST(DiceASTExpression tree,
			FunctionalMap<String, DiceASTExpression> env) {
		return freezeAST(tree.getAst(),
				env.mapValues(expression -> expression.getAst()));
	}

	/**
	 * Freeze references to specified variables
	 * 
	 * @param tree
	 *            The tree-backed expression to freeze references in
	 * @param env
	 *            The enviroment to resolve variables against
	 * @param varNames
	 *            The names of the variables to freeze
	 * @return An AST with the specified variables frozen
	 */
	public static AST<IDiceASTNode> selectiveFreeze(AST<IDiceASTNode> tree,
			FunctionalMap<String, AST<IDiceASTNode>> env,
			String... varNames) {
		return selectiveFreeze(tree, env, new FunctionalList<>(varNames));
	}

	/**
	 * Freeze references to specified variables
	 * 
	 * @param tree
	 *            The tree-backed expression to freeze references in
	 * @param env
	 *            The enviroment to resolve variables against
	 * @param varNames
	 *            The names of the variables to freeze
	 * @return An AST with the specified variables frozen
	 */
	public static AST<IDiceASTNode> selectiveFreeze(AST<IDiceASTNode> tree,
			FunctionalMap<String, AST<IDiceASTNode>> env,
			FunctionalList<String> varNames) {
		return tree.expand(new SelectiveFreezer(env, varNames));
	}
}