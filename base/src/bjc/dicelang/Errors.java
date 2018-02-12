package bjc.dicelang;

/**
 * Repository for error messages.
 *
 * @TODO 10/08/17 Ben Culkin :ErrorRefactor This way of handling error messages
 *       is not easy to deal with. Something else needs to be done, but I'm not
 *       sure what at the moment.
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
		/* Define Errors */
		/**
		 * Incorrect define guard syntax
		 */
		EK_DFN_PREDSYN,
		/**
		 * Incorrect define search syntax
		 */
		EK_DFN_SRCSYN,
		/**
		 * Recursive define recursed too many times
		 */
		EK_DFN_RECUR,

		/* Console Errors */
		/**
		 * Unknown console pragma
		 */
		EK_CONS_INVPRAG,
		/**
		 * Improperly formatted define
		 */
		EK_CONS_INVDEFINE,

		/* Language Engine Errors */
		/**
		 * Found closing double-brace w/out opening double-brace
		 */
		EK_ENG_NOOPENING,
		/**
		 * Reached end of command w/out balanced double-braces
		 */
		EK_ENG_NOCLOSING,

		/* Tokenizer Errors */
		/**
		 * Found an unexpected grouping token
		 */
		EK_TOK_UNGROUP,
		/**
		 * Invalid base for a flexadecimal number
		 */
		EK_TOK_INVBASE,
		/**
		 * Invalid flexadecimal number in a given base
		 */
		EK_TOK_INVFLEX,

		/* Evaluator Errors */
		/**
		 * Unknown node type
		 */
		EK_EVAL_INVNODE,
		/**
		 * Incorrect # of args to binary operator
		 */
		EK_EVAL_INVBIN,
		/**
		 * Incorrect # of args to unary operator
		 */
		EK_EVAL_INVUNARY,
		/**
		 * Unknown binary operator
		 */
		EK_EVAL_UNBIN,
		/**
		 * Unknown unary operator
		 */
		EK_EVAL_UNUNARY,
		/**
		 * Math on strings doesn't work
		 */
		EK_EVAL_STRINGMATH,
		/**
		 * Attempted divide by zero
		 */
		EK_EVAL_DIVZERO,
		/**
		 * Attempted to divide dice
		 */
		EK_EVAL_DIVDICE,
		/**
		 * Unknown math operator
		 */
		EK_EVAL_UNMATH,
		/**
		 * Unknown token reference
		 */
		EK_EVAL_UNTOK,
		/**
		 * Unknown dice operator
		 */
		EK_EVAL_UNDICE,
		/**
		 * Incorrect type to dice group operator
		 */
		EK_EVAL_INVDGROUP,
		/**
		 * Incorrect type to dice creation operator
		 */
		EK_EVAL_INVDCREATE,
		/**
		 * Incorrect type to other dice operator
		 */
		EK_EVAL_INVDICE,
		/**
		 * Mismatched types to math operator
		 */
		EK_EVAL_MISMATH,
		/**
		 * Incorrect type to string operator
		 */
		EK_EVAL_INVSTRING,
		/**
		 * Unknown string operator
		 */
		EK_EVAL_UNSTRING,

		/* Parser Error */
		/**
		 * Group closing where there couldn't be an opener
		 */
		EK_PARSE_NOCLOSE,
		/**
		 * Group closing without group opener
		 */
		EK_PARSE_UNCLOSE,
		/**
		 * Incorrect # of arguments to binary operator
		 */
		EK_PARSE_BINARY,
		/**
		 * Not enough operands to binary operator
		 */
		EK_PARSE_UNOPERAND,
		/**
		 * Unrecognized token type
		 */
		EK_PARSE_INVTOKEN,

		/* Shunter Error */
		/**
		 * Unary operator expected a operand, but got an operator
		 */
		EK_SHUNT_NOTADV,
		/**
		 * Unary operator expected an operator, but got an operand
		 */
		EK_SHUNT_NOTADJ,
		/**
		 * Unary operator expected an operator, but didn't find one
		 */
		EK_SHUNT_NOOP,
		/**
		 * Asked for opening grouping operator, but couldn't find one
		 */
		EK_SHUNT_NOGROUP,
		/**
		 * No group for group seperator to attach to
		 */
		EK_SHUNT_INVSEP,
		/**
		 * Attempted to chain non-associative operator
		 */
		EK_SHUNT_NOTASSOC,

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

		/* CLI Argument Errors */
		/**
		 * Unknown CLI argument
		 */
		EK_CLI_UNARG,
		/**
		 * Missing sub-argument to argument
		 */
		EK_CLI_MISARG,
		/**
		 * Invalid define type
		 */
		EK_CLI_INVDFNTYPE,

		/* Miscellaneous errors */
		/**
		 * Unknown I/O problem
		 */
		EK_MISC_IOEX,
		/**
		 * File not found
		 */
		EK_MISC_NOFILE,

		/* Dice errors. */
		/* Recieved the wrong sort of expression to a die. */
		EK_DICE_INVTYPE,
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
	 *        The key of the error.
	 *
	 * @param args
	 *        The arguments for the error.
	 */
	public void printError(final ErrorKey key, final String... args) {
		switch (mode) {
		case WIZARD:
			if (key == ErrorKey.EK_MISC_NOFILE) {
				System.out.println("\t? 404");
			} else {
				System.out.println("\t? " + key.ordinal());
			}

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
		case EK_DFN_PREDSYN:
			System.out.printf("\tERROR: Incorrect define guard syntax %s\n", args[0]);
			break;

		case EK_DFN_SRCSYN:
			System.out.printf("\tERROR: Incorrect define match syntax %s\n", args[0]);
			break;

		case EK_DFN_RECUR:
			System.out.printf("\tERROR: Recursive define didn't converge after %s iterations."
					+ " Original string was %s, last iteration was %s\n", args[0], args[1], args[2]);
			break;

		case EK_CONS_INVPRAG:
			System.out.printf("\tERROR: Unknown pragma %s\n", args[0]);
			break;

		case EK_CONS_INVDEFINE:
			System.out.printf("\tERROR: Improperly formatted define %s\n", args[0]);
			break;

		case EK_ENG_NOOPENING:
			System.out.printf("\tERROR: Encountered closing doublebrace without" + " matching opening doublebrace\n");
			break;

		case EK_ENG_NOCLOSING:
			System.out.printf("\tERROR: Reached end of string before closing doublebrace was found\n");
			break;

		case EK_TOK_UNGROUP:
			System.out.printf("\tERROR: Unrecognized grouping token %s\n", args[0]);
			break;

		case EK_TOK_INVBASE:
			System.out.printf("\tERROR: Invalid flexadecimal base %s\n", args[0]);
			break;

		case EK_TOK_INVFLEX:
			System.out.printf("\tERROR: Invalid flexadecimal number %s in base %s\n", args[0], args[1]);
			break;

		case EK_EVAL_INVNODE:
			System.out.printf("\tERROR: Unknown node in evaluator: %s\n", args[0]);
			break;

		case EK_EVAL_INVBIN:
			System.out.printf("\tERROR: Binary operators take 2 operands, not %s\n" + "\tProblem node is %s\n", args[0],
					args[1]);
			break;

		case EK_EVAL_UNBIN:
			System.out.printf("\tERROR: Unknown binary operator %s\n", args[0]);
			break;

		case EK_EVAL_STRINGMATH:
			System.out.printf("\tERROR: Math operators don't work on strings\n");
			break;

		case EK_EVAL_DIVZERO:
			System.out.printf("\tERROR: Attempted divide by zero\n");
			break;

		case EK_EVAL_DIVDICE:
			System.out.printf("\tERROR: Dice cannot be divided\n");
			break;

		case EK_EVAL_UNMATH:
			System.out.printf("\tERROR: Unknown math binary operator: %s\n", args[0]);
			break;

		case EK_EVAL_UNTOK:
			System.out.printf("\tERROR: Unknown token ref %s\n", args[0]);
			break;

		case EK_EVAL_UNDICE:
			System.out.printf("\tERROR: Unknown dice operator %s\n", args[0]);
			break;

		case EK_EVAL_INVDCREATE:
			System.out.printf("\tERROR: Dice creation operator expects integers," + " not %s\n", args[0]);
			break;

		case EK_EVAL_INVDGROUP:
			System.out.printf("\tERROR: Dice group operator expects scalar dice or integers," + " not %s\n", args[0]);
			break;

		case EK_EVAL_INVDICE:
			System.out.printf("\tERROR: Dice operators expect scalar dice, not %s\n", args[0]);
			break;

		case EK_EVAL_MISMATH:
			System.out.printf("\tERROR: Math operators expect two operands of the same type\n");
			break;

		case EK_EVAL_INVSTRING:
			System.out.printf("\tERROR: Incorrect type %s to string operator\n", args[0]);
			break;

		case EK_EVAL_UNSTRING:
			System.out.printf("\tERROR: Unknown string operator %s\n", args[0]);
			break;

		case EK_PARSE_NOCLOSE:
			System.out.printf("\tERROR: Group closing with no possible group opener\n");
			break;

		case EK_PARSE_UNCLOSE:
			System.out.printf("\tERROR: Found group closer without opener: (closing was %s" + ", expected %s)\n",
					args[0], args[1]);
			break;

		case EK_PARSE_BINARY:
			System.out.printf("\tERROR: Expected at least two operands\n");
			break;

		case EK_PARSE_UNOPERAND:
			System.out.printf("\tERROR: Operator %s expected more operands than provided\n", args[0]);
			break;

		case EK_PARSE_INVTOKEN:
			System.out.printf("\tERROR: Unrecognized token type in parsing: %s\n", args[0]);
			break;

		case EK_SHUNT_NOTADV:
			System.out.printf("\tERROR: Unary operator %s is an adjective, not an adverb. It can't be"
					+ " applied to the operator %s\n", args[0], args[1]);
			break;

		case EK_SHUNT_NOTADJ:
			System.out.printf("\tERROR: Unary operator %s is an adjective, not an adverb. It can't be"
					+ " applied to the operator %s\n", args[0], args[1]);
			break;

		case EK_SHUNT_NOOP:
			System.out.printf("\tERROR: Unary operator %s is an adverb, but there is no operator" + " to apply it to\n",
					args[0]);
			break;

		case EK_SHUNT_NOGROUP:
			System.out.printf("\tERROR: Couldn't find matching grouping %s (expected %s)\n", args[0], args[1]);
			break;

		case EK_SHUNT_NOTASSOC:
			System.out.printf("\tERROR: Attempted to chain non-associative operator %s\n", args[0]);
			break;

		case EK_SHUNT_INVSEP:
			System.out.printf("\tERROR: Couldn't find grouper for group seperator to attach to\n");
			break;

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

		case EK_CLI_UNARG:
			System.out.printf("\tERROR: Unknown argument %s\n", args[0]);
			break;

		case EK_CLI_MISARG:
			System.out.printf("\tERROR: Missing subargument to command %s", args[0]);
			break;

		case EK_CLI_INVDFNTYPE:
			System.out.printf("\tERROR: Invalid define type %s\n", args[0]);
			break;

		case EK_MISC_IOEX:
			System.out.printf("\tERROR: I/O problem with file\n");
			break;

		case EK_MISC_NOFILE:
			System.out.printf("\tERROR: No such file %s\n", args[0]);
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
