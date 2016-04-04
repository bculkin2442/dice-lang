package bjc.dicelang;

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
}