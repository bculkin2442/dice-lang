package bjc.dicelang.dice;

/**
 * General exception thrown when things go wrong with dice.
 *
 * @author Ben Culkin
 */
public class DieException extends RuntimeException {
	private static final long serialVersionUID = 4235650970793087717L;

	/**
	 * Create a new die exception with a given message.
	 *
	 * @param msg
	 * 	The message for this exception.
	 */
	public DieException(String msg) {
		super(msg);
	}

	/**
	 * Create a new die exception with a given message and cause.
	 *
	 * @param msg
	 * 	The message for this exception.
	 *
	 * @param cause
	 * 	The cause for this exception.
	 */
	public DieException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
