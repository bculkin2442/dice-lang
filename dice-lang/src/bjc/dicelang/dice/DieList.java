package bjc.dicelang.dice;

/**
 * Represents a group of dice.
 *
 * @author Ben Culkin.
 */
public interface DieList {
	/**
	 * Can this list be optimized?
	 *
	 * @return Whether or not this list cna be optimized.
	 */
	boolean canOptimize();
	/**
	 * Optimize this list, if it can be done.
	 *
	 * Invoking this on unoptimizable expression is undefined.
	 *
	 * @return The optimized form of this list.
	 */
	long[]  optimize();

	/**
	 * Roll this group of dice.
	 *
	 * @param A possible roll of this group.
	 */
	long[] roll();
}
