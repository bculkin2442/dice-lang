package bjc.dicelang.scl.tokens;

/**
 * Represents an integer token.
 * 
 * @author student
 *
 */
public class IntSCLToken extends SCLToken {
	/**
	 * The integer value of the token.
	 */
	public long intVal;

	/**
	 * Create a new integer token.
	 * 
	 * @param iVal
	 *            The value of the token.
	 */
	public IntSCLToken(final long iVal) {
		super(Type.ILIT);

		intVal = iVal;
	}
}