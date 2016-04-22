package bjc.dicelang;

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
	 * Roll the dice once
	 * 
	 * @return The result of rowing the dice
	 */
	public int roll();

	/**
	 * Optimize this expression to a scalar value
	 * 
	 * @return This expression, optimized to a scalar value
	 * 
	 * @throws UnsupportedOperationException
	 *             if this type of expression can't be optimized
	 */
	public default int optimize() {
		throw new UnsupportedOperationException(
				"Can't optimize this type of expression");
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
	 * Parse this node into an expression
	 * 
	 * @param expression
	 *            The string to convert to an expression
	 * 
	 * @return The string in expression form
	 */
	static IDiceExpression toExpression(String expression) {
		String literalData = expression;

		if (StringUtils.containsInfixOperator(literalData, "c")) {
			String[] strangs = literalData.split("c");

			return new CompoundDice(strangs);
		} else if (StringUtils.containsInfixOperator(literalData, "d")) {
			/*
			 * Handle dice groups
			 */
			return ComplexDice.fromString(literalData);
		} else {
			try {
				return new ScalarDie(Integer.parseInt(literalData));
			} catch (NumberFormatException nfex) {
				UnsupportedOperationException usex = new UnsupportedOperationException(
						"Found malformed leaf token " + expression);

				usex.initCause(nfex);

				throw usex;
			}
		}
	}
}