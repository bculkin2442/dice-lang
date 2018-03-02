package bjc.dicelang.dice;

/**
 * Represents one or more dice that produce a scalar result.
 *
 * @author Ben Culkin
 */
public interface Die {
	/**
	 * Can this die be optimized to a single number?
	 *
	 * @return Whether this die can be optimized or not.
	 */
	boolean canOptimize();

	/**
	 * Optimize this die to a single number.
	 *
	 * Calling optimize on dice that return false for canOptimize produces
	 * undefined behavior
	 *
	 * @return The optimized form of this die
	 */
	long optimize();

	/**
	 * Roll this die.
	 *
	 * @return A possible roll of this die
	 */
	long roll();

	/**
	 * Roll only a single portion of this die.
	 *
	 * @return A possible roll of a single portion of this die.
	 */
	long rollSingle();
}
