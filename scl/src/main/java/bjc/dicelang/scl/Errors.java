package bjc.dicelang.scl;

/**
 * Repository for error messages.
 *
 * @TODO 10/08/17 Ben Culkin :ErrorRefactor
 * 
 *       This way of handling error messages is not easy to deal with. Something
 *       else needs to be done, but I'm not sure what at the moment.
 *
 * @author EVE
 *
 */
public class Errors {
	/**
	 * The types of error message.
	 *
	 * @author EVE
	 *
	 */
	public static enum ErrorKey {
		/* Stream Errors */
		/**
		 * Attempted to switch to a non-existant stream
		 */
		EK_STRM_NONEX,
		/**
		 * Can't delete the last stream
		 */
		EK_STRM_LAST,
		/**
		 * Unknown stream command
		 */
		EK_STRM_INVCOM,
		/* SCL Errors */
		/**
		 * Unknown SCL token
		 */
		EK_SCL_INVTOKEN,
		/**
		 * Mismatched quote in SCL command
		 */
		EK_SCL_MMQUOTE,
		/**
		 * Stack underflow in SCL command
		 */
		EK_SCL_SUNDERFLOW,
		/**
		 * Unknown word in SCL command
		 */
		EK_SCL_UNWORD,
		/**
		 * Invalid argument to SCL command
		 */
		EK_SCL_INVARG,
	}

	/**
	 * The mode for the type of error messages to print out.
	 *
	 * @author EVE
	 *
	 */
	public static enum ErrorMode {
		/**
		 * Output error messages for wizards.
		 */
		WIZARD,
		/**
		 * Output error messages for developers.
		 */
		DEV
	}

	private ErrorMode mode;

	/**
	 * Print an error.
	 *
	 * @param key
	 *            The key of the error.
	 *
	 * @param args
	 *            The arguments for the error.
	 */
	public void printError(final ErrorKey key, final String... args) {
		switch (mode) {
		case WIZARD:
			System.out.println("\t? " + key.ordinal());
			break;

		case DEV:
			devError(key, args);
			break;

		default:
			System.out.println("\tERROR ERROR: Unknown error mode " + mode);
		}
	}

	private static void devError(final ErrorKey key, final String[] args) {
		switch (key) {
		case EK_STRM_NONEX:
			System.out.printf("\tERROR: Attempted to switch to non-existent stream\n");
			break;

		case EK_STRM_LAST:
			System.out.printf("\tERROR: Cannot delete last stream\n");
			break;

		case EK_STRM_INVCOM:
			System.out.printf("\tERROR: Unknown stream control command %s\n", args[0]);
			break;
			
		case EK_SCL_INVTOKEN:
			System.out.printf("\tERROR: Unknown SCL token %s\n", args[0]);
			break;

		case EK_SCL_MMQUOTE:
			System.out.printf("\tERROR: Mismatched delimiter in SCL command\n");
			break;

		case EK_SCL_SUNDERFLOW:
			System.out.printf("\tERROR: Not enough items in stack for word %s\n", args[0]);
			break;

		case EK_SCL_UNWORD:
			System.out.printf("\tERROR: Unknown word %s\n", args[0]);
			break;

		default:
			System.out.printf("\tERROR ERROR: Unknown error key %s\n", key);
		}
	}

	/**
	 * The instance of the errors.
	 */
	public final static Errors inst;

	static {
		inst = new Errors();

		inst.mode = ErrorMode.DEV;
	}
}
