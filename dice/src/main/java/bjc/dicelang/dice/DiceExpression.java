package bjc.dicelang.dice;

/**
 * Represents either a die or a die list.
 *
 * @author Ben Culkin
 */
public interface DiceExpression {
	/**
	 * Get the value of this expression as a string.
	 *
	 * @return The value of the expression as a string.
	 */
	public String value();

	/**
	 * Check if the expression is a list or not.
	 * 
	 * @return Whether or not the expression is a list.
	 */
	public boolean isList();
}
