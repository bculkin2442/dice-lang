package bjc.dicelang.dicev2;

import java.util.function.LongPredicate;

/**
 * Utility class for creating compare points.
 * @author Ben Culkin
 *
 */
public class ComparePoints {
	/**
	 * Create a compare point for checking 'less than'
	 * @param val The value to check if we are less than.
	 * @return A compare point that does the specified check.
	 */
	public static LongPredicate isLess(long val) {
		return (arg) -> arg < val;
	}
	
	/**
	 * Create a compare point for checking 'equals'
	 * @param val The value to check if we are equal to.
	 * @return A compare point that does the specified check.
	 */
	public static LongPredicate isEqual(long val) {
		return (arg) -> arg == val;
	}
	
	/**
	 * Create a compare point for checking 'greater than'
	 * @param val The value to check if we are greater than.
	 * @return A compare point that does the specified check.
	 */
	public static LongPredicate isGreater(long val) {
		return (arg) -> arg > val;
	}
}
