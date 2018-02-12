package bjc.dicelang.scl;

import java.util.HashMap;
import java.util.Map;

import bjc.dicelang.Errors;
import bjc.utils.esodata.SimpleStack;
import bjc.utils.esodata.Stack;
import bjc.utils.funcdata.FunctionalList;
import bjc.utils.funcdata.IList;
import bjc.utils.parserutils.TokenUtils;

import static bjc.dicelang.Errors.ErrorKey.*;
import static bjc.dicelang.scl.SCLToken.Type.*;
import static bjc.dicelang.scl.WordSCLToken.Word.*;

/*
 * @TODO 10/08/17 Ben Culkin :SCLReorg
 * 	This is a large enough class that it should maybe be split into
 * 	subclasses.
 */
/**
 * Runs a Stream Control Language (SCL) program.
 *
 * SCL is a stack-based concatenative language based mostly off of Postscript
 * and Factor, with inspiration from various other languages.
 *
 * @author Ben Culkin
 */
public class StreamControlEngine {
	/* The stream engine we're hooked to. */
	private final StreamEngine eng;

	/* The current stack state. */
	private final Stack<SCLToken> curStack;

	/* Map of user defined words. */
	private final Map<String, SCLToken> words;

	/**
	 * Create a new stream control engine.
	 *
	 * @param engine
	 *            The engine to control.
	 */
	public StreamControlEngine(final StreamEngine engine) {
		eng = engine;

		words = new HashMap<>();
		curStack = new SimpleStack<>();
	}

	/**
	 * Run a SCL program.
	 *
	 * @param tokens
	 *            The program to run.
	 *
	 * @return Whether the program executed successfully.
	 */
	public boolean runProgram(final String[] tokens) {
		for (int i = 0; i < tokens.length; i++) {
			/* Tokenize each token. */
			final String token = tokens[i];
			final SCLToken tok = SCLToken.tokenizeString(token);

			if (tok == null) {
				System.out.printf("ERROR: Tokenization failed for '%s'\n", token);
				return false;
			}

			/* Handle token types. */
			switch (tok.type) {
			case SQUOTE:
				/* Handle single-quotes. */
				i = handleSingleQuote(i, tokens);
				if (i == -1) {
					return false;
				}
				break;
			case OBRACKET:
				/* Handle delimited brackets. */
				i = handleDelim(i, tokens, "]");
				if (i == -1) {
					return false;
				}
				break;
			case OBRACE:
				/* Handle delimited braces. */
				i = handleDelim(i, tokens, "}");
				if (i == -1) {
					return false;
				}
				final SCLToken brak = curStack.pop();
				curStack.push(new ArraySCLToken(((WordListSCLToken) brak).tokenVals));
				break;

			case WORD:
				/* Handle words. */
				if (!handleWord((WordSCLToken) tok)) {
					System.out.printf("WARNING: Execution of word '%s' failed\n", tok);
				}
				break;
			default:
				/* Put it onto the stack. */
				curStack.push(tok);
				break;
			}
		}

		return true;
	}

	private boolean handleWord(final WordSCLToken tk) {
		boolean succ = true;

		/* Handle each type of word. */
		/*
		 * @NOTE This should probably use something other than a switch statement.
		 */
		switch (tk.wordVal) {
		case NEWSTREAM:
			eng.newStream();
			break;
		case LEFTSTREAM:
			succ = eng.leftStream();
			if (!succ) {
				return false;
			}
			break;
		case RIGHTSTREAM:
			succ = eng.rightStream();
			if (!succ) {
				return false;
			}
			break;
		case DELETESTREAM:
			succ = eng.deleteStream();
			if (!succ) {
				return false;
			}
			break;
		case MERGESTREAM:
			succ = eng.mergeStream();
			if (!succ) {
				return false;
			}
			break;
		case MAKEARRAY:
			succ = makeArray();
			if (!succ) {
				return false;
			}
			break;
		case MAKEEXEC:
			succ = toggleExec(true);
			if (!succ) {
				return false;
			}
			break;
		case MAKEUNEXEC:
			succ = toggleExec(false);
			if (!succ) {
				return false;
			}
			break;
		case STACKCOUNT:
			curStack.push(new IntSCLToken(curStack.size()));
			break;
		case STACKEMPTY:
			curStack.push(new BooleanSCLToken(curStack.empty()));
			break;
		case DROP:
			if (curStack.size() == 0) {
				Errors.inst.printError(EK_SCL_SUNDERFLOW, tk.toString());
				return false;
			}
			curStack.drop();
			break;
		case NDROP:
			succ = handleNDrop();
			if (!succ) {
				return false;
			}
			break;
		case NIP:
			if (curStack.size() < 2) {
				Errors.inst.printError(EK_SCL_SUNDERFLOW, tk.toString());
				return false;
			}
			curStack.nip();
			break;
		case NNIP:
			succ = handleNNip();
			if (!succ) {
				return false;
			}
			break;
		default:
			Errors.inst.printError(EK_SCL_UNWORD, tk.toString());
			return false;
		}

		return true;
	}

	/* Handle nipping a specified number of items. */
	private boolean handleNNip() {
		final SCLToken num = curStack.pop();

		if (num.type != ILIT) {
			Errors.inst.printError(EK_SCL_INVARG, num.type.toString());
			return false;
		}

		final int n = (int) ((IntSCLToken) num).intVal;

		if (curStack.size() < n) {
			Errors.inst.printError(EK_SCL_SUNDERFLOW, NNIP.toString());
			return false;
		}

		curStack.nip(n);
		return true;
	}

	/* Handle dropping a specified number of items. */
	private boolean handleNDrop() {
		final SCLToken num = curStack.pop();

		if (num.type != ILIT) {
			Errors.inst.printError(EK_SCL_INVARG, num.type.toString());
			return false;
		}

		final int n = (int) ((IntSCLToken) num).intVal;

		if (curStack.size() < n) {
			Errors.inst.printError(EK_SCL_SUNDERFLOW, NDROP.toString());
			return false;
		}

		curStack.drop(n);
		return true;
	}

	/* Handle toggling the executable flag on an array. */
	private boolean toggleExec(final boolean exec) {
		final SCLToken top = curStack.top();

		if (exec) {
			if (top.type != ARRAY) {
				Errors.inst.printError(EK_SCL_INVARG, top.toString());
				return false;
			}

			top.type = WORDS;
		} else {
			if (top.type != WORDS) {
				Errors.inst.printError(EK_SCL_INVARG, top.toString());
				return false;
			}

			top.type = ARRAY;
		}

		return true;
	}

	/* Handle creating an array. */
	private boolean makeArray() {
		final SCLToken num = curStack.pop();

		if (num.type != ILIT) {
			Errors.inst.printError(EK_SCL_INVARG, num.type.toString());
		}

		final IList<SCLToken> arr = new FunctionalList<>();

		for (int i = 0; i < ((IntSCLToken) num).intVal; i++) {
			arr.add(curStack.pop());
		}

		curStack.push(new ArraySCLToken(arr));

		return true;
	}

	/* Handle a delimited series of tokens. */
	private int handleDelim(final int i, final String[] tokens, final String delim) {
		final IList<SCLToken> toks = new FunctionalList<>();

		int n = i + 1;

		if (n >= tokens.length) {
			Errors.inst.printError(EK_SCL_MMQUOTE);
			return -1;
		}

		String tok = tokens[n];

		while (!tok.equals(delim)) {
			final SCLToken ntok = SCLToken.tokenizeString(tok);

			switch (ntok.type) {
			case SQUOTE:
				n = handleSingleQuote(n, tokens);
				if (n == -1) {
					return -1;
				}
				toks.add(curStack.pop());
				break;
			case OBRACKET:
				n = handleDelim(n, tokens, "]");
				if (n == -1) {
					return -1;
				}
				toks.add(curStack.pop());
				break;
			case OBRACE:
				n = handleDelim(i, tokens, "}");
				if (n == -1) {
					return -1;
				}
				final SCLToken brak = curStack.pop();
				toks.add(new ArraySCLToken(((WordListSCLToken) brak).tokenVals));
				break;
			default:
				toks.add(ntok);
			}

			/* Move to the next token */
			n += 1;

			if (n >= tokens.length) {
				Errors.inst.printError(EK_SCL_MMQUOTE);
				return -1;
			}

			tok = tokens[n];
		}

		/* Skip the closing bracket */
		n += 1;

		/*
		 * @NOTE Instead of being hardcoded, this should be a parameter.
		 */
		curStack.push(new WordsSCLToken(toks));

		return n;
	}

	/* Handle a single-quoted string. */
	private int handleSingleQuote(final int i, final String[] tokens) {
		final StringBuilder sb = new StringBuilder();

		int n = i + 1;

		if (n >= tokens.length) {
			Errors.inst.printError(EK_SCL_MMQUOTE);
			return -1;
		}

		String tok = tokens[n];

		while (!tok.equals("'")) {
			if (tok.matches("\\\\+'")) {
				/* Handle escaped quotes. */
				sb.append(tok.substring(1));
			} else {
				sb.append(tok);
			}

			/* Move to the next token */
			n += 1;

			if (n >= tokens.length) {
				Errors.inst.printError(EK_SCL_MMQUOTE);
				return -1;
			}

			tok = tokens[n];
		}

		/*
		 * Skip the single quote
		 */
		n += 1;

		String strang = TokenUtils.descapeString(sb.toString());

		curStack.push(new StringSCLToken(false, strang));

		return n;
	}
}
