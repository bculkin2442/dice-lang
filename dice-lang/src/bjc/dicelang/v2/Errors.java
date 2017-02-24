package bjc.dicelang.v2;

public class Errors {
	public static enum ErrorKey {
		// Define Errors
		// Incorrect define guard syntax
		EK_DFN_PREDSYN,
		// Incorrect define search syntax
		EK_DFN_SRCSYN,
		// Recursive define recursed too many times
		EK_DFN_RECUR,

		// Console Errors
		// Unknown console pragma
		EK_CONS_INVPRAG,
		// Improperly formatted define
		EK_CONS_INVDEFINE,

		// Language Engine Errors
		// Found closing doublebrace w/out opening doublebrace
		EK_ENG_NOOPENING,
		// Reached end of command w/out balanced doublebraces
		EK_ENG_NOCLOSING,
		
		// Tokenizer Errors
		// Found an unexpected grouping token
		EK_TOK_UNGROUP,
		// Invalid base for a flexadecimal number
		EK_TOK_INVBASE,
		// Invalid flexadecimal number in a given base
		EK_TOK_INVFLEX,
		
		// Evaluator Errors
		// Unknown node type
		EK_EVAL_INVNODE,
		// Incorrect # of args to binary operator
		EK_EVAL_INVBIN,
		// Unknown binary operator
		EK_EVAL_UNBIN,
		// Math on strings doesn't work
		EK_EVAL_STRINGMATH,
		// Attempted divide by zero
		EK_EVAL_DIVZERO,
		// Unknown math operator
		EK_EVAL_UNMATH,
		// Unknown token reference
		EK_EVAL_UNTOK,
		// Unknown dice operator
		EK_EVAL_UNDICE,
		// Incorrect type to dice group operator
		EK_EVAL_INVDGROUP,
		// Incorrect type to other dice operator
		EK_EVAL_INVDICE,
		// Mismatched types to math operator
		EK_EVAL_MISMATH,

		// Parser Error
		// Group closing where there couldn't be an opener
		EK_PARSE_NOCLOSE,
		// Group closing without group opener
		EK_PARSE_UNCLOSE,
		// Incorrect # of arguments to binary operator
		EK_PARSE_BINARY,
		// Not enough operands to binary operator
		EK_PARSE_UNOPERAND,
		// Unrecognized token type
		EK_PARSE_INVTOKEN,

		// Shunter Error
		// Unary operator expected a operand, but got an operator
		EK_SHUNT_NOTADV,
		// Unary operator expected an operator, but got an operand
		EK_SHUNT_NOTADJ,
		// Unary operator expected an operator, but didn't find one
		EK_SHUNT_NOOP,
		// Asked for opening grouping operator, but couldn't find one
		EK_SHUNT_NOGROUP,
		// No group for group seperator to attach to
		EK_SHUNT_INVSEP,

		// Stream Errors
		// Attempted to switch to a non-existant stream
		EK_STRM_NONEX,
		// Can't delete the last stream
		EK_STRM_LAST,
		// Unknown stream command
		EK_STRM_INVCOM,

	}

	public static enum ErrorMode {
		WIZARD, DEV
	}

	private ErrorMode mode;

	public void printError(ErrorKey key, String... args) {
		switch(mode) {
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

	private void devError(ErrorKey key, String[] args) {
		switch(key) {
			case EK_DFN_PREDSYN:
				System.out.printf("\tERROR: Incorrect define guard syntax %s\n", args[0]);
				break;
			case EK_DFN_SRCSYN:
				System.out.printf("\tERROR: Incorrect define match syntax %s\n", args[0]);
				break;
			case EK_DFN_RECUR:
				System.out.printf("\tERROR: Recursive define didn't converge after %s iterations."
						+ " Original string was %s, last iteration was %s\n",
						args[0], args[1], args[2]);
				break;
			case EK_CONS_INVPRAG:
				System.out.printf("\tERROR: Unknown pragma %s\n", args[0]);
				break;
			case EK_CONS_INVDEFINE:
				System.out.printf("\tERROR: Improperly formatted define %s\n", args[0]);
				break;
			case EK_ENG_NOOPENING:
				System.out.printf("\tERROR: Encountered closing doublebrace without"
						+ " matching opening doublebrace\n");
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
				System.out.printf("\tERROR: Binary operators take 2 operand, not %s\n", args[0]);
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
			case EK_EVAL_UNMATH:
				System.out.printf("\tERROR: Unknown math binary operator: %s\n", args[0]);
				break;
			case EK_EVAL_UNTOK:
				System.out.printf("\tERROR: Unknown token ref %s\n", args[0]);
				break;
			case EK_EVAL_UNDICE:
				System.out.printf("\tERROR: Unknown dice operator %s\n", args[0]);
				break;
			case EK_EVAL_INVDGROUP:
				System.out.printf("\tERROR: Dice group operator expects scalar dice or integers,"
						+ " not %s\n", args[0]);
				break;
			case EK_EVAL_INVDICE:
				System.out.printf("\tERROR: Dice operators expect scalar dice, not %s\n", args[0]);
				break;
			case EK_EVAL_MISMATH:
				System.out.printf("\tERROR: Math operators expect two operands of the same type\n");
				break;
			case EK_PARSE_NOCLOSE:
				System.out.printf("\tERROR: Group closing with no possible group opener\n");
				break;
			case EK_PARSE_UNCLOSE:
				System.out.printf("\tERROR: Found group closer without opener: (closing was %s"
						+ ", expected %s)\n", args[0], args[1]);
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
				System.out.printf("\tERROR: Unary operator %s is an adverb, but there is no operator"
						+ " to apply it to\n", args[0]);
				break;
			case EK_SHUNT_NOGROUP:
				System.out.printf("\tERROR: Couldn't find matching grouping %s (expected %s)\n",
						args[0], args[1]);
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
			default:
				System.out.printf("\tERROR ERROR: Unknown error key %s\n", key);
		}
	}

	public final static Errors inst;

	static {
		inst = new Errors();

		inst.mode = ErrorMode.DEV;
	}
}
