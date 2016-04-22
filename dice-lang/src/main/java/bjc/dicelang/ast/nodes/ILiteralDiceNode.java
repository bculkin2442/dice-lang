package bjc.dicelang.ast.nodes;

import org.apache.commons.lang3.StringUtils;

/**
 * Represents a literal of some type in the AST
 * 
 * @author ben
 *
 */
public interface ILiteralDiceNode extends IDiceASTNode {
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

	/**
	 * Check if this node can be optimized to a constant
	 * 
	 * @return Whether or not this node can be optimized to a constant
	 * @see bjc.dicelang.IDiceExpression#canOptimize()
	 */
	boolean canOptimize();

	/**
	 * Get the type of literal this node represents
	 * 
	 * @return The type of literal this node represents
	 */
	DiceLiteralType getLiteralType();

	@Override
	default DiceASTType getType() {
		return DiceASTType.LITERAL;
	}

	@Override
	default boolean isOperator() {
		return false;
	}

	/**
	 * Optimize this node to a constant if possible
	 * 
	 * @return This node in constant form if possible
	 * @see bjc.dicelang.IDiceExpression#optimize()
	 */
	int optimize();
}
