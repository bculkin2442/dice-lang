package bjc.dicelang.scl;

import bjc.dicelang.Errors;

import bjc.utils.esodata.Stack;
import bjc.utils.esodata.Tape;
import bjc.utils.funcdata.IList;
import bjc.utils.funcdata.FunctionalList;
import bjc.utils.funcutils.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static bjc.dicelang.Errors.ErrorKey.*;
import static bjc.dicelang.scl.StreamControlEngine.Token.Type.*;

/**
 * Runs a Stream Control Language (SCL) program.
 *
 * SCL is a stack-based concatenative language based mostly off of Postscript
 * and Factor, with inspiration from various other languages.
 */
public class StreamControlEngine {
	static class Token {
		public static enum Type {
			/*
			 * Natural tokens.
			 * These come directly from strings
			 */
			ILIT,     FLIT,  BLIT,
			SQUOTE,   DQUOTE,
			OBRACKET, OBRACE,
			SYMBOL,   WORD,

			/*
			 * Synthetic tokens.
			 * These are produced from special tokens.
			 */
			SLIT,
			WORDS, ARRAY,

			/*
			 * Word tokens
			 * These are subordinate to WORD tokens
			 */
			/*
			 * Array manipulation
			 */
			MAKEARRAY,
			MAKEEXEC,   MAKEUNEXEC,
			/*
			 * Stream manipulation
			 */
			NEWSTREAM,
			LEFTSTREAM, RIGHTSTREAM,
			DELETESTREAM, MERGESTREAM,
			/*
			 * Stack manipulation
			 */
			STACKCOUNT, STACKEMPTY,
			DROP,       NDROP,
			NIP,        NNIP,
		}

		/*
		 * The type of this token
		 */
		public Type type;

		/*
		 * Used for
		 * 	ILIT
		 */
		public long intVal;
		/*
		 * Used for
		 * 	FLIT
		 */
		public double floatVal;
		/*
		 * Used for
		 * 	BLIT
		 */
		public boolean boolVal;
		/*
		 * Used for
		 * 	SYMBOL
		 * 	SLIT
		 */
		public String stringVal;
		/*
		 * Used for
		 * 	WORD
		 */
		public Token tokenVal;
		/*
		 * Used for
		 * 	WORDS
		 * 	ARRAY
		 */
		public IList<Token> tokenVals;

		public Token(Type typ) {
			type = typ;
		}

		public Token(Type typ, long iVal) {
			this(typ);

			intVal = iVal;
		}

		public Token(Type typ, double dVal) {
			this(typ);

			floatVal = dVal;
		}

		public Token(Type typ, boolean bVal) {
			this(typ);

			boolVal = bVal;
		}

		public Token(Type typ, String sVal) {
			this(typ);

			stringVal = sVal;
		}

		public Token(Type typ, Token tVal) {
			this(typ);

			tokenVal = tVal;
		}

		public Token(Type typ, Token.Type tVal) {
			this(typ, new Token(tVal));
		}


		public Token(Type typ, IList<Token> tVals) {
			this(typ);

			tokenVals = tVals;
		}

		public static Token tokenizeString(String token) {
			if(litTokens.containsKey(token)) {
				return new Token(litTokens.get(token));
			} else if(token.startsWith("\\")) {
				return new Token(SYMBOL, token.substring(1));
			} else if(builtinWords.containsKey(token)) {
				return new Token(WORD, builtinWords.get(token));
			} else if(token.equals("true")) {
				return new Token(BLIT, true);
			} else if(token.equals("false")) {
				return new Token(BLIT, false);
			} else if(StringUtils.isInt(token)) {
				return new Token(ILIT, Long.parseLong(token));
			} else if(StringUtils.isDouble(token)) {
				return new Token(FLIT, Double.parseDouble(token));
			} else {
				Errors.inst.printError(EK_SCL_INVTOKEN, token);
				return null;
			}
		}

		private static final Map<String, Token.Type> litTokens;
		private static final Map<String, Token.Type> builtinWords; 

		static {
			litTokens = new HashMap<>();

			litTokens.put("'",  SQUOTE);
			litTokens.put("\"", DQUOTE);
			litTokens.put("[",  OBRACKET);
			litTokens.put("{", OBRACE);

			builtinWords = new HashMap<>();

			builtinWords.put("makearray", MAKEARRAY);
			builtinWords.put("+stream",   NEWSTREAM);
			builtinWords.put(">stream",   LEFTSTREAM);
			builtinWords.put("<stream",   RIGHTSTREAM);
			builtinWords.put("-stream",   DELETESTREAM);
			builtinWords.put("<-stream",   MERGESTREAM);
			builtinWords.put("cvx",       MAKEEXEC);
			builtinWords.put("cvux",      MAKEUNEXEC);
			builtinWords.put("#",         STACKCOUNT);
			builtinWords.put("empty?",    STACKEMPTY);
			builtinWords.put("drop",      DROP);
			builtinWords.put("ndrop",     NDROP);
			builtinWords.put("nip",       NIP);
			builtinWords.put("nnip",      NNIP);
		}
	}

	private StreamEngine eng;

	private Stack<Token> curStack;

	private Map<String, Token> words;

	public StreamControlEngine(StreamEngine eng) {
		this.eng = eng;

		words = new HashMap<>();
	}

	/**
	 * Run a SCL program.
	 *
	 * @param tokens The program to run
	 *
	 * @return Whether the program executed succesfully
	 */
	public boolean runProgram(String[] tokens) {
		for(int i = 0; i < tokens.length; i++) {
			String token = tokens[i];
			Token tok = Token.tokenizeString(token);
			
			if(tok == null) {
				return false;
			}

			switch(tok.type) {
			case SQUOTE:
				i = handleSingleQuote(i, tokens);
				if(i == -1) return false;
				break;
			case OBRACKET:
				i = handleDelim(i, tokens, "]");
				if(i == -1) return false;
				break;
			case OBRACE:
				i = handleDelim(i, tokens, "}");
				if(i == -1) return false;
				Token brak = curStack.pop();
				curStack.push(new Token(ARRAY, brak.tokenVals));
				break;
			case WORD:
				handleWord(tok);
				break;
			default:
				curStack.push(tok);
				break;
			}
		}

		return true;
	}

	private boolean handleWord(Token tk) {
		boolean succ = true;

		switch(tk.tokenVal.type) {
		case NEWSTREAM:
			eng.newStream();
			break;
		case LEFTSTREAM:
			succ = eng.leftStream();
			if(!succ) return false;
			break;
		case RIGHTSTREAM:
			succ = eng.rightStream();
			if(!succ) return false;
			break;
		case DELETESTREAM:
			succ = eng.deleteStream();
			if(!succ) return false;
			break;
		case MERGESTREAM:
			succ = eng.mergeStream();
			if(!succ) return false;
			break;
		case MAKEARRAY:
			succ = makeArray();
			if(!succ) return false;
			break;
		case MAKEEXEC:
			succ = toggleExec(true);
			if(!succ) return false;
			break;
		case MAKEUNEXEC:
			succ = toggleExec(false);
			if(!succ) return false;
			break;
		case STACKCOUNT:
			curStack.push(new Token(ILIT, curStack.size()));
			break;
		case STACKEMPTY:
			curStack.push(new Token(BLIT, curStack.empty()));
			break;
		case DROP:
			if(curStack.size() == 0) {
				Errors.inst.printError(EK_SCL_SUNDERFLOW, tk.tokenVal.type.toString());
				return false;
			}
			curStack.drop();
			break;
		case NDROP:
			succ = handleNDrop();
			if(!succ) return false;
			break;
		case NIP:
			if(curStack.size() < 2) {
				Errors.inst.printError(EK_SCL_SUNDERFLOW, tk.tokenVal.type.toString());
				return false;
			}
			
			curStack.nip();
			break;
		case NNIP:
			succ = handleNNip();
			if(!succ) return false;
			break;
		default:
			Errors.inst.printError(EK_SCL_UNWORD, tk.tokenVal.type.toString());
			return false;
		}

		return true;
	}

	private boolean handleNNip() {
		Token num = curStack.pop();

		if(num.type != ILIT) {
			Errors.inst.printError(EK_SCL_INVARG, num.type.toString());
			return false;
		}

		int n = (int)num.intVal;
		
		if(curStack.size() < n) {
			Errors.inst.printError(EK_SCL_SUNDERFLOW, NNIP.toString());
			return false;
		}

		curStack.nip(n);
		return true;
	}

	private boolean handleNDrop() {
		Token num = curStack.pop();

		if(num.type != ILIT) {
			Errors.inst.printError(EK_SCL_INVARG, num.type.toString());
			return false;
		}

		int n = (int)num.intVal;
		
		if(curStack.size() < n) {
			Errors.inst.printError(EK_SCL_SUNDERFLOW, NDROP.toString());
			return false;
		}

		curStack.drop(n);
		return true;
	}

	private boolean toggleExec(boolean exec) {
		Token top = curStack.top();

		if(exec) {
			if(top.type != ARRAY) {
				Errors.inst.printError(EK_SCL_INVARG, top.toString());
				return false;
			}

			top.type = WORDS;
		} else {
			if(top.type != WORDS) {
				Errors.inst.printError(EK_SCL_INVARG, top.toString());
				return false;
			}

			top.type = ARRAY;
		}

		return true;
	}

	private boolean makeArray() {
		Token num = curStack.pop();

		if(num.type != ILIT) {
			Errors.inst.printError(EK_SCL_INVARG, num.type.toString());
		}

		IList<Token> arr = new FunctionalList<>();

		for(int i = 0; i < num.intVal; i++) {
			arr.add(curStack.pop());
		}

		curStack.push(new Token(ARRAY, arr));

		return true;
	}

	private int handleDelim(int i, String[] tokens, String delim) {
		IList<Token> toks = new FunctionalList<>();

		int n      = i + 1;
		if(n >= tokens.length) {
			Errors.inst.printError(EK_SCL_MMQUOTE);
			return -1;
		}
		String tok = tokens[n];

		while(!tok.equals(delim)) {
			Token ntok = Token.tokenizeString(tok);

			switch(ntok.type) {
			case SQUOTE:
				n = handleSingleQuote(n, tokens);
				if(n == -1) return -1;
				toks.add(curStack.pop());
				break;
			case OBRACKET:
				n = handleDelim(n, tokens, "]");
				if(n == -1) return -1;
				toks.add(curStack.pop());
				break;
			case OBRACE:
				i = handleDelim(i, tokens, "}");
				if(i == -1) return -1;
				Token brak = curStack.pop();
				toks.add(new Token(ARRAY, brak.tokenVals));
				break;
			default:
				toks.add(ntok);
			}
			/*
			 * Move to the next token
			 */
			n  += 1;
			if(n >= tokens.length) {
				Errors.inst.printError(EK_SCL_MMQUOTE);
				return -1;
			}
			tok = tokens[n];
		}

		/*
		 * Skip the closing bracket
		 */
		n += 1;

		curStack.push(new Token(WORDS, toks));

		return n;
	}

	private int handleSingleQuote(int i, String[] tokens) {
		StringBuilder sb = new StringBuilder();

		int n      = i + 1;
		if(n >= tokens.length) {
			Errors.inst.printError(EK_SCL_MMQUOTE);
			return -1;
		}
		String tok = tokens[n];

		while(!tok.equals("'")) {
			if(tok.matches("\\\\+'")) {
				/*
				 * Handle escaped quotes.
				 */
				sb.append(tok.substring(1));
			} else {
				sb.append(tok);
			}

			/*
			 * Move to the next token
			 */
			n  += 1;
			if(n >= tokens.length) {
				Errors.inst.printError(EK_SCL_MMQUOTE);
				return -1;
			}
			tok = tokens[n];
		}

		/*
		 * Skip the single quote
		 */
		n += 1;

		curStack.push(new Token(SLIT, StringUtils.descapeString(sb.toString())));

		return n;
	}
}
