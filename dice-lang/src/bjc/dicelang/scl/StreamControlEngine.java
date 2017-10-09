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
import static bjc.dicelang.scl.StreamControlEngine.Token.Type.*;

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
	/*
	 * @TODO 10/08/17 Ben Culkin :TokenSplit
	 * 	Again with the multiple subclasses in one class. Split it so
	 * 	that each subclass only has the fields it needs.
	 */
	public static class Token {
		public static enum Type {
			/* Natural tokens. These come directly from strings */
			ILIT, FLIT, BLIT, SQUOTE, DQUOTE, OBRACKET, OBRACE, SYMBOL, WORD,

			/* Synthetic tokens. These are produced from special tokens. */
			SLIT, WORDS, ARRAY,

			/* Word tokens These are subordinate to WORD tokens */
			/*
			 * @NOTE
			 * 	These should really be in their own enum.
			 */
			/* Array manipulation */
			MAKEARRAY, MAKEEXEC, MAKEUNEXEC,
			/* Stream manipulation */
			NEWSTREAM, LEFTSTREAM, RIGHTSTREAM, DELETESTREAM, MERGESTREAM,
			/* Stack manipulation */
			STACKCOUNT, STACKEMPTY, DROP, NDROP, NIP, NNIP,
		}

		/* The type of this token */
		public Type type;

		/* Used for ILIT */
		public long intVal;
		/* Used for FLIT */
		public double floatVal;
		/* Used for BLIT */
		public boolean boolVal;
		/* Used for SYMBOL & SLIT */
		public String stringVal;
		/* Used for WORD */
		public Token tokenVal;
		/* Used for WORDS & ARRAY */
		public IList<Token> tokenVals;

		/* Create a new token. */
		public Token(final Type typ) {
			type = typ;
		}

		/* Create a new token. */
		public Token(final Type typ, final long iVal) {
			this(typ);

			intVal = iVal;
		}

		/* Create a new token. */
		public Token(final Type typ, final double dVal) {
			this(typ);

			floatVal = dVal;
		}

		/* Create a new token. */
		public Token(final Type typ, final boolean bVal) {
			this(typ);

			boolVal = bVal;
		}

		/* Create a new token. */
		public Token(final Type typ, final String sVal) {
			this(typ);

			stringVal = sVal;
		}

		/* Create a new token. */
		public Token(final Type typ, final Token tVal) {
			this(typ);

			tokenVal = tVal;
		}

		/* Create a new token. */
		public Token(final Type typ, final Token.Type tVal) {
			this(typ, new Token(tVal));
		}

		/* Create a new token. */
		public Token(final Type typ, final IList<Token> tVals) {
			this(typ);

			tokenVals = tVals;
		}

		/* Convert a string into a token. */
		public static Token tokenizeString(final String token) {
			if (litTokens.containsKey(token)) {
				return new Token(litTokens.get(token));
			} else if (token.startsWith("\\")) {
				return new Token(SYMBOL, token.substring(1));
			} else if (builtinWords.containsKey(token)) {
				return new Token(WORD, builtinWords.get(token));
			} else if (token.equals("true")) {
				return new Token(BLIT, true);
			} else if (token.equals("false")) {
				return new Token(BLIT, false);
			} else if (TokenUtils.isInt(token)) {
				return new Token(ILIT, Long.parseLong(token));
			} else if (TokenUtils.isDouble(token)) {
				return new Token(FLIT, Double.parseDouble(token));
			} else {
				Errors.inst.printError(EK_SCL_INVTOKEN, token);
				return null;
			}
		}

		/* The literal tokens. */
		private static final Map<String, Token.Type>    litTokens;
		/* The builtin words. */
		private static final Map<String, Token.Type>    builtinWords;

		static {
			/* Init literal tokens. */
			litTokens = new HashMap<>();

			litTokens.put("'", SQUOTE);
			litTokens.put("\"", DQUOTE);
			litTokens.put("[", OBRACKET);
			litTokens.put("{", OBRACE);

			/* Init builtin words. */
			builtinWords = new HashMap<>();

			builtinWords.put("makearray", MAKEARRAY);
			builtinWords.put("+stream", NEWSTREAM);
			builtinWords.put(">stream", LEFTSTREAM);
			builtinWords.put("<stream", RIGHTSTREAM);
			builtinWords.put("-stream", DELETESTREAM);
			builtinWords.put("<-stream", MERGESTREAM);
			builtinWords.put("cvx", MAKEEXEC);
			builtinWords.put("cvux", MAKEUNEXEC);
			builtinWords.put("#", STACKCOUNT);
			builtinWords.put("empty?", STACKEMPTY);
			builtinWords.put("drop", DROP);
			builtinWords.put("ndrop", NDROP);
			builtinWords.put("nip", NIP);
			builtinWords.put("nnip", NNIP);
		}
	}

	/* The stream engine we're hooked to. */
	private final StreamEngine eng;

	/* The current stack state. */
	private final Stack<Token> curStack;

	/* Map of user defined words. */
	private final Map<String, Token> words;

	/**
	 * Create a new stream control engine.
	 *
	 * @param engine
	 *                The engine to control.
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
	 *                The program to run.
	 *
	 * @return Whether the program executed successfully.
	 */
	public boolean runProgram(final String[] tokens) {
		for (int i = 0; i < tokens.length; i++) {
			/* Tokenize each token. */
			final String token = tokens[i];
			final Token tok    = Token.tokenizeString(token);

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
				final Token brak = curStack.pop();
				curStack.push(new Token(ARRAY, brak.tokenVals));
				break;

			case WORD:
				/* Handle words. */
				if(!handleWord(tok)) {
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

	private boolean handleWord(final Token tk) {
		boolean succ = true;

		/* Handle each type of word. */
		/* 
		 * @NOTE
		 * 	This should probably use something other than a switch
		 * 	statement.
		 */
		switch (tk.tokenVal.type) {
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
			curStack.push(new Token(ILIT, curStack.size()));
			break;
		case STACKEMPTY:
			curStack.push(new Token(BLIT, curStack.empty()));
			break;
		case DROP:
			if (curStack.size() == 0) {
				Errors.inst.printError(EK_SCL_SUNDERFLOW, tk.tokenVal.type.toString());
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
				Errors.inst.printError(EK_SCL_SUNDERFLOW, tk.tokenVal.type.toString());
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
			Errors.inst.printError(EK_SCL_UNWORD, tk.tokenVal.type.toString());
			return false;
		}

		return true;
	}

	/* Handle nipping a specified number of items. */
	private boolean handleNNip() {
		final Token num = curStack.pop();

		if (num.type != ILIT) {
			Errors.inst.printError(EK_SCL_INVARG, num.type.toString());
			return false;
		}

		final int n = (int) num.intVal;

		if (curStack.size() < n) {
			Errors.inst.printError(EK_SCL_SUNDERFLOW, NNIP.toString());
			return false;
		}

		curStack.nip(n);
		return true;
	}

	/* Handle dropping a specified number of items. */
	private boolean handleNDrop() {
		final Token num = curStack.pop();

		if (num.type != ILIT) {
			Errors.inst.printError(EK_SCL_INVARG, num.type.toString());
			return false;
		}

		final int n = (int) num.intVal;

		if (curStack.size() < n) {
			Errors.inst.printError(EK_SCL_SUNDERFLOW, NDROP.toString());
			return false;
		}

		curStack.drop(n);
		return true;
	}

	/* Handle toggling the executable flag on an array. */
	private boolean toggleExec(final boolean exec) {
		final Token top = curStack.top();

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
		final Token num = curStack.pop();

		if (num.type != ILIT) {
			Errors.inst.printError(EK_SCL_INVARG, num.type.toString());
		}

		final IList<Token> arr = new FunctionalList<>();

		for (int i = 0; i < num.intVal; i++) {
			arr.add(curStack.pop());
		}

		curStack.push(new Token(ARRAY, arr));

		return true;
	}

	/* Handle a delimited series of tokens. */
	private int handleDelim(final int i, final String[] tokens, final String delim) {
		final IList<Token> toks = new FunctionalList<>();

		int n = i + 1;

		if (n >= tokens.length) {
			Errors.inst.printError(EK_SCL_MMQUOTE);
			return -1;
		}

		String tok = tokens[n];

		while (!tok.equals(delim)) {
			final Token ntok = Token.tokenizeString(tok);

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
				final Token brak = curStack.pop();
				toks.add(new Token(ARRAY, brak.tokenVals));
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

		/* @NOTE
		 * 	Instead of being hardcoded, this should be a parameter.
		 */
		curStack.push(new Token(WORDS, toks));

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

		curStack.push(new Token(SLIT, TokenUtils.descapeString(sb.toString())));

		return n;
	}
}
