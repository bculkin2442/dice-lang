package bjc.dicelang.neodice;

/**
 * The exception thrown when something goes wrong with diebox.
 * @author Ben Culkin
 *
 */
public class DieBoxException extends RuntimeException {
	private static final long serialVersionUID = 1851356458656622896L;

	/** Create a new exception */
	public DieBoxException() {
		super();
	}
	
	/**
	 * Create a new exception with a given message.
	 * 
	 * @param message The message for the exception.
	 */
	public DieBoxException(String message) {
		super(message);
	}
	
	/**
	 * Create a new exception with a given formatted message.
	 * 
	 * @param format The format string.
	 * @param args The format arguments.
	 */
	public DieBoxException(String format, Object... args) {
		super(String.format(format, args));
	}
	
	/**
	 * Create a new diebox exception with a cause.
	 * @param cause The cause of this exception.
	 */
	public DieBoxException(Throwable cause) {
		super(cause);
	}

    /**
     * Create a new diebox exception with a cause and message.
     * @param cause The cause of this exception.
     * @param message The message for the exception.
     */
	public DieBoxException(Throwable cause, String message) {
		super(message, cause);
	}

    /**
     * Create a new diebox exception with a cause and formatted message.
     * @param cause The cause of this exception.
     * @param format The format string.
     * @param args The format arguments.
     */
	public DieBoxException(Throwable cause, String format, Object... args) {
		super(String.format(format, args), cause);
	}
}