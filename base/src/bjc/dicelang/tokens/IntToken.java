package bjc.dicelang.tokens;

/**
 * Represents an integer literal.
 * 
 * @author EVE
 *
 */
public class IntToken extends Token {
	/**
	 * The literal value.
	 */
	public final long value;

	/**
	 * Create a new integer literal.
	 * 
	 * @param val
	 *        The integer to use.
	 */
	public IntToken(long val) {
		super(Type.INT_LIT);

		value = val;
	}
}
