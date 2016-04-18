package bjc.dicelang.examples;

import bjc.dicelang.ast.nodes.IDiceASTNode;
import bjc.utils.funcdata.IFunctionalMap;
import bjc.utils.funcdata.ITree;

/**
 * Sanitize the references in an AST so that a variable that refers to
 * itself in its definition has the occurance of it replaced with its
 * previous definition
 * 
 * @author ben
 *
 */
public class DiceASTReferenceSanitizer {
	/**
	 * Sanitize the references in an AST
	 * 
	 * @param ast
	 * @param enviroment
	 * @return The sanitized AST
	 */
	public static ITree<IDiceASTNode> sanitize(ITree<IDiceASTNode> ast,
			IFunctionalMap<String, ITree<IDiceASTNode>> enviroment) {
		// TODO implement me
		return null;
	}
}
