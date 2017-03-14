package bjc.dicelang.v1.ast.nodes;

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
	 *                The token to check
	 * @return The type the literal would be if it is one, or null otherwise
	 */
	static DiceLiteralType getLiteralType(String tok) {
		String diceGroup = "\\d*d\\d+\\";

		String diceGroupOrNumber = "[(?:" + diceGroup + ")(?:\\d+)]";

		if(tok.matches("\\A" + diceGroupOrNumber + "?" + "c" + diceGroupOrNumber + "\\Z"))
			return DiceLiteralType.DICE;

		if(tok.matches("\\A" + diceGroup + "Z")) return DiceLiteralType.DICE;

		try {
			Integer.parseInt(tok);
			return DiceLiteralType.INTEGER;
		} catch(NumberFormatException nfex) {
			// We don't care about details
			// This probably shouldn't return null, but I believe it
			// does so
			// because where its called checks that. @FIXME
			return null;
		}
	}

	/**
	 * Check if this node can be optimized to a constant
	 *
	 * @return Whether or not this node can be optimized to a constant
	 * @see bjc.dicelang.v1.IDiceExpression#canOptimize()
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
	 * @see bjc.dicelang.v1.IDiceExpression#optimize()
	 */
	int optimize();
}
