package bjc.dicelang.ast;

import bjc.utils.funcdata.FunctionalList;
import bjc.utils.funcdata.IList;
import bjc.utils.funcdata.IMap;
import bjc.utils.funcdata.ITree;
import bjc.utils.funcdata.Tree;

import bjc.dicelang.ast.nodes.DiceASTType;
import bjc.dicelang.ast.nodes.IDiceASTNode;
import bjc.dicelang.ast.nodes.VariableDiceNode;

/**
 * Inline variables in a dice AST
 * 
 * @author ben
 *
 */
public class DiceASTInliner {
	/**
	 * Inline all the variables in the AST
	 * 
	 * @param ast
	 *            The AST to inline variables into
	 * @param enviroment
	 *            The enviroment to inline from
	 * @return The inlined AST
	 */
	public static ITree<IDiceASTNode> inlineAll(ITree<IDiceASTNode> ast,
			IMap<String, ITree<IDiceASTNode>> enviroment) {
		// Tell the compiler that the null is for the entire varargs
		// parameter, not a single one with a null value
		return selectiveInline(ast, enviroment, (String[]) null);
	}

	private static ITree<IDiceASTNode> inlineNode(IDiceASTNode node,
			IMap<String, ITree<IDiceASTNode>> enviroment,
			boolean specificInline, IList<String> variableNames) {
		// Only variables get inlined
		if (node.getType() != DiceASTType.VARIABLE) {
			return new Tree<>(node);
		}

		// Get the name of what we're inlining
		String variableName = ((VariableDiceNode) node).getVariable();

		// If we're inlining only certain variables, do so
		if (specificInline) {
			// Only inline the variable if we're supposed to
			if (variableNames.contains(variableName)) {
				// You can't inline non-existent variables
				if (!enviroment.containsKey(variableName)) {
					throw new UnsupportedOperationException(
							"Attempted to inline non-existant variable "
									+ variableName);
				}

				// Return the tree for the variable
				return enviroment.get(variableName);
			}
		} else {
			// You can't inline non-existent variables
			if (!enviroment.containsKey(variableName)) {
				throw new UnsupportedOperationException(
						"Attempted to inline non-existant variable "
								+ variableName);
			}

			// Return the tree for the variable
			return enviroment.get(variableName);
		}

		// return new Tree<>(node);
	}

	/**
	 * Inline the specified variables in the AST
	 * 
	 * @param ast
	 *            The AST to inline variables into
	 * @param enviroment
	 *            The enviroment to inline from
	 * @param variables
	 *            The variables to inline
	 * @return The inlined AST
	 */
	public static ITree<IDiceASTNode> selectiveInline(
			ITree<IDiceASTNode> ast,
			IMap<String, ITree<IDiceASTNode>> enviroment,
			IList<String> variables) {
		return selectiveInline(ast, enviroment,
				variables.toArray(new String[0]));
	}

	/**
	 * Inline the specified variables in the AST
	 * 
	 * @param ast
	 *            The AST to inline variables into
	 * @param enviroment
	 *            The enviroment to inline from
	 * @param variables
	 *            The variables to inline
	 * @return The inlined AST
	 */
	public static ITree<IDiceASTNode> selectiveInline(
			ITree<IDiceASTNode> ast,
			IMap<String, ITree<IDiceASTNode>> enviroment,
			String... variables) {
		// If we're selectively inlining, do so
		if (variables != null && variables.length > 0) {
			IList<String> variableNames = new FunctionalList<>(variables);

			// Selectively inline each tree node
			return ast.flatMapTree((node) -> {
				return inlineNode(node, enviroment, true, variableNames);
			});
		}

		// Inline everything in each node
		return ast.flatMapTree((node) -> {
			return inlineNode(node, enviroment, false, null);
		});
	}
}
