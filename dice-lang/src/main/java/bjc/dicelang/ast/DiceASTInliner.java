package bjc.dicelang.ast;

import bjc.dicelang.ast.nodes.DiceASTType;
import bjc.dicelang.ast.nodes.IDiceASTNode;
import bjc.dicelang.ast.nodes.VariableDiceNode;
import bjc.utils.funcdata.FunctionalList;
import bjc.utils.funcdata.IFunctionalList;
import bjc.utils.funcdata.IFunctionalMap;
import bjc.utils.funcdata.ITree;
import bjc.utils.funcdata.Tree;

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
			IFunctionalMap<String, ITree<IDiceASTNode>> enviroment) {
		// Tell the compiler that the null is for the entire varargs
		// parameter, not a single one with a null value
		return selectiveInline(ast, enviroment, (String[]) null);
	}

	private static ITree<IDiceASTNode> inlineNode(IDiceASTNode node,
			IFunctionalMap<String, ITree<IDiceASTNode>> enviroment,
			boolean specificInline,
			IFunctionalList<String> variableNames) {
		if (node.getType() != DiceASTType.VARIABLE) {
			return new Tree<>(node);
		}

		String variableName = ((VariableDiceNode) node).getVariable();

		if (specificInline) {
			if (variableNames.contains(variableName)) {
				if (!enviroment.containsKey(variableName)) {
					throw new UnsupportedOperationException(
							"Attempted to inline non-existant variable "
									+ variableName);
				}

				return enviroment.get(variableName);
			}
		} else {
			if (!enviroment.containsKey(variableName)) {
				throw new UnsupportedOperationException(
						"Attempted to inline non-existant variable "
								+ variableName);
			}

			return enviroment.get(variableName);
		}

		return new Tree<>(node);
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
			IFunctionalMap<String, ITree<IDiceASTNode>> enviroment,
			String... variables) {
		if (variables != null && variables.length > 0) {
			IFunctionalList<String> variableNames =
					new FunctionalList<>(variables);

			return ast.flatMapTree((node) -> {
				return inlineNode(node, enviroment, true, variableNames);
			});
		}

		return ast.flatMapTree((node) -> {
			return inlineNode(node, enviroment, false, null);
		});
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
			IFunctionalMap<String, ITree<IDiceASTNode>> enviroment,
			IFunctionalList<String> variables) {
		return selectiveInline(ast, enviroment,
				variables.toArray(new String[0]));
	}
}
