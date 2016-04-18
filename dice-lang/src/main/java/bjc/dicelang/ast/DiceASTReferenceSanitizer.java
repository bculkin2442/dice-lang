package bjc.dicelang.ast;

import bjc.dicelang.ast.nodes.IDiceASTNode;
import bjc.dicelang.ast.nodes.OperatorDiceNode;
import bjc.dicelang.ast.nodes.VariableDiceNode;
import bjc.utils.funcdata.IFunctionalMap;
import bjc.utils.funcdata.ITree;
import bjc.utils.funcdata.TopDownTransformResult;
import bjc.utils.funcdata.Tree;

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
		return ast.topDownTransform(
				DiceASTReferenceSanitizer::shouldSanitize, (subTree) -> {
					return doSanitize(subTree, enviroment);
				});
	}

	private static TopDownTransformResult
			shouldSanitize(IDiceASTNode node) {
		if (!node.isOperator()) {
			return TopDownTransformResult.SKIP;
		}

		switch (((OperatorDiceNode) node)) {
			case ASSIGN:
				return TopDownTransformResult.TRANSFORM;
			case LET:
				return TopDownTransformResult.PASSTHROUGH;
			case ADD:
			case COMPOUND:
			case DIVIDE:
			case GROUP:
			case MULTIPLY:
			case SUBTRACT:
			default:
				return TopDownTransformResult.SKIP;
		}
	}

	private static ITree<IDiceASTNode> doSanitize(ITree<IDiceASTNode> ast,
			IFunctionalMap<String, ITree<IDiceASTNode>> enviroment) {
		if (ast.getChildrenCount() != 2) {
			throw new UnsupportedOperationException(
					"Assignment must have two arguments.");
		}

		ITree<IDiceASTNode> nameTree = ast.getChild(0);
		ITree<IDiceASTNode> valueTree = ast.getChild(1);

		if (!DiceASTUtils.containsSimpleVariable(nameTree)) {
			throw new UnsupportedOperationException(
					"Assignment must be between a variable and a expression");
		}

		String variableName = nameTree.transformHead(
				(node) -> ((VariableDiceNode) node).getVariable());

		if (enviroment.containsKey(variableName)) {
			// We should always inline out references to last, because it
			// will always change
			ITree<IDiceASTNode> inlinedValue =
					DiceASTInliner.selectiveInline(valueTree, enviroment,
							variableName, "last");

			return new Tree<>(ast.getHead(), nameTree, inlinedValue);
		}

		return ast;
	}
}
