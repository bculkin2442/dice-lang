package bjc.dicelang.v1;

import bjc.utils.funcutils.StringUtils;

/**
 * An expression for something that can be rolled like a polyhedral die
 *
 * @author ben
 *
 */
@FunctionalInterface
public interface IDiceExpression {
	/**
	 * Parse a string into an expression.
	 *
	 * It can accept the following types of expressions
	 * <ul>
	 * <li>Simple integers - '2'</li>
	 * <li>Simple dice - 'd6'</li>
	 * <li>Groups of simple dice - '2d6'</li>
	 * <li>Number concatenation - '2c6'</li>
	 * <li>Dice concatenation - '1d10c1d10</li>
	 * </ul>
	 *
	 * Dice concatenation is like using 2 d10s to emulate a d100, so instead
	 * of adding them, it reads them side by side.
	 *
	 * @param expression
	 *                The string to convert to an expression
	 *
	 * @return The string, converted into expression form
	 */
	static IDiceExpression toExpression(String expression) {
		String literalData = expression;

		String diceMatcher = "\\Ad\\d+\\Z";

		if(StringUtils.containsInfixOperator(literalData, "c")) {
			// Parse a compound die
			String[] strangs = literalData.split("c");

			return new CompoundDice(strangs);
		} else if(StringUtils.containsInfixOperator(literalData, "d"))
			// Handle groups of similiar dice
			return ComplexDice.fromString(literalData);
		else if(literalData.matches(diceMatcher))
			// Handle people who put 'd6' instead of '1d6'
			return new Die(Integer.parseInt(literalData.substring(1)));
		else {
			// Parse a scalar number
			try {
				return new ScalarDie(Integer.parseInt(literalData));
			} catch(NumberFormatException nfex) {
				UnsupportedOperationException usex = new UnsupportedOperationException(
						"Found malformed leaf token " + expression + ". Floating point numbers "
								+ "are not supported.");

				usex.initCause(nfex);

				throw usex;
			}
		}
	}

	/**
	 * Check if this expression can be optimized to a scalar value
	 *
	 * @return Whether or not this expression can be optimized to a scalar
	 *         value
	 */
	public default boolean canOptimize() {
		return false;
	}

	/**
	 * Optimize this expression to a scalar value
	 *
	 * @return This expression, optimized to a scalar value
	 *
	 * @throws UnsupportedOperationException
	 *                 if this type of expression can't be optimized
	 */
	public default int optimize() {
		throw new UnsupportedOperationException("Can't optimize this type of expression");
	}

	/**
	 * Roll the dice once
	 *
	 * @return The result of rowing the dice
	 */
	public int roll();
}
