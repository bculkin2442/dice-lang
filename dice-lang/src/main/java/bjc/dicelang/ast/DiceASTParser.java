package bjc.dicelang.ast;

import java.util.InputMismatchException;

import bjc.dicelang.old.ast.nodes.IDiceASTNode;
import bjc.dicelang.old.ast.nodes.LiteralDiceNode;
import bjc.dicelang.old.ast.nodes.OperatorDiceNode;
import bjc.dicelang.old.ast.nodes.VariableDiceNode;
import bjc.utils.funcdata.IFunctionalList;
import bjc.utils.parserutils.AST;
import bjc.utils.parserutils.TreeConstructor;

/**
 * Parse a string expression into AST form. Doesn't do anything else
 * 
 * @author ben
 *
 */
public class DiceASTParser {
	/**
	 * Create an AST from a list of tokens
	 * 
	 * @param tokens
	 *            The list of tokens to convert
	 * @return An AST built from the tokens
	 */
	public static AST<IDiceASTNode> createFromString(
			IFunctionalList<String> tokens) {
		AST<String> rawTokens =
				TreeConstructor.constructTree(tokens, (token) -> {
					return isOperatorNode(token);
				}, (operator) -> false, null);
		// The last argument is valid because there are no special
		// operators yet, so it'll never get called

		return rawTokens.rebuildTree(DiceASTParser::convertLeafNode,
				DiceASTParser::convertOperatorNode);
	}

	private static boolean isOperatorNode(String token) {
		try {
			OperatorDiceNode.fromString(token);
			return true;
		} catch (@SuppressWarnings("unused") IllegalArgumentException iaex) {
			// We don't care about details
			return false;
		}
	}

	private static IDiceASTNode convertLeafNode(String leafNode) {
		if (LiteralDiceNode.isLiteral(leafNode)) {
			return new LiteralDiceNode(leafNode);
		}

		return new VariableDiceNode(leafNode);
	}

	private static IDiceASTNode convertOperatorNode(String operatorNode) {
		try {
			return OperatorDiceNode.fromString(operatorNode);
		} catch (IllegalArgumentException iaex) {
			InputMismatchException imex = new InputMismatchException(
					"Attempted to parse invalid operator " + operatorNode);

			imex.initCause(iaex);

			throw imex;
		}
	}
}
