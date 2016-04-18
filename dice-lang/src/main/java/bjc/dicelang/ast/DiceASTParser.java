package bjc.dicelang.ast;

import java.util.InputMismatchException;

import bjc.dicelang.IDiceExpression;
import bjc.dicelang.ast.nodes.DiceLiteralNode;
import bjc.dicelang.ast.nodes.DiceLiteralType;
import bjc.dicelang.ast.nodes.IDiceASTNode;
import bjc.dicelang.ast.nodes.ILiteralDiceNode;
import bjc.dicelang.ast.nodes.IntegerLiteralNode;
import bjc.dicelang.ast.nodes.OperatorDiceNode;
import bjc.dicelang.ast.nodes.VariableDiceNode;
import bjc.utils.funcdata.IFunctionalList;
import bjc.utils.funcdata.ITree;
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
	public static ITree<IDiceASTNode>
			createFromString(IFunctionalList<String> tokens) {
		ITree<String> rawTokens =
				TreeConstructor.constructTree(tokens, (token) -> {
					return isOperatorNode(token);
				}, (operator) -> false, null);

		// The last argument is valid because there are no special
		// operators yet, so it'll never get called

		ITree<IDiceASTNode> tokenizedTree =
				rawTokens.rebuildTree(DiceASTParser::convertLeafNode,
						DiceASTParser::convertOperatorNode);
		
		return tokenizedTree;
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
		DiceLiteralType literalType =
				ILiteralDiceNode.getLiteralType(leafNode);

		if (literalType != null) {
			switch (literalType) {
				case DICE:
					return new DiceLiteralNode(
							IDiceExpression.toExpression(leafNode));
				case INTEGER:
					return new IntegerLiteralNode(
							Integer.parseInt(leafNode));
				default:
					throw new InputMismatchException(
							"Cannot convert string '" + leafNode
									+ "' into a literal.");
			}
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
