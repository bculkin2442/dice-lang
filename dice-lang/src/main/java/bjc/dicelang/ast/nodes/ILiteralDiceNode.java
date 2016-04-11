package bjc.dicelang.ast.nodes;

import org.apache.commons.lang3.StringUtils;

/**
 * Represents a literal of some type in the AST
 * 
 * @author ben
 *
 */
public interface ILiteralDiceNode extends IDiceASTNode {
	@Override
	default DiceASTType getType() {
		return DiceASTType.LITERAL;
	}

	@Override
	default boolean isOperator() {
		return false;
	}

	/**
	 * Get the type of literal this node represents
	 * 
	 * @return The type of literal this node represents
	 */
	DiceLiteralType getLiteralType();

	/**
	 * Check if a token represents a literal, and if so, what type
	 * 
	 * @param tok
	 *            The token to check
	 * @return The type the literal would be if it is one, or null
	 *         otherwise
	 */
	static DiceLiteralType getLiteralType(String tok) {
		if (StringUtils.countMatches(tok, 'c') == 1
				&& !tok.equalsIgnoreCase("c")) {
			return DiceLiteralType.DICE;
		} else if (StringUtils.countMatches(tok, 'd') == 1
				&& !tok.equalsIgnoreCase("d")) {
			return DiceLiteralType.DICE;
		} else {
			try {
				Integer.parseInt(tok);
				return DiceLiteralType.INTEGER;
			} catch (@SuppressWarnings("unused") NumberFormatException nfex) {
				// We don't care about details
				return null;
			}
		}
	}
}
