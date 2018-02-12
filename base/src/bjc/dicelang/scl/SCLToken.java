package bjc.dicelang.scl;

import static bjc.dicelang.Errors.ErrorKey.EK_SCL_INVTOKEN;

import java.util.HashMap;
import java.util.Map;

import bjc.dicelang.Errors;
import bjc.utils.funcdata.IList;
import bjc.utils.parserutils.TokenUtils;

import static bjc.dicelang.scl.SCLToken.Type.*;

/*
 * @TODO 10/08/17 Ben Culkin :TokenSplit
 * 	Again with the multiple subclasses in one class. Split it so
 * 	that each subclass only has the fields it needs.
 */
public class SCLToken {
	public static enum Type {
		/* Natural tokens. These come directly from strings */
		ILIT, FLIT, BLIT, SQUOTE, DQUOTE, OBRACKET, OBRACE, SYMBOL, WORD,

		/* Synthetic tokens. These are produced from special tokens. */
		SLIT, WORDS, ARRAY,

		/* Word tokens These are subordinate to WORD tokens */
		/*
		 * @NOTE These should really be in their own enum.
		 */
		/* Array manipulation */
		MAKEARRAY, MAKEEXEC, MAKEUNEXEC,
		/* Stream manipulation */
		NEWSTREAM, LEFTSTREAM, RIGHTSTREAM, DELETESTREAM, MERGESTREAM,
		/* Stack manipulation */
		STACKCOUNT, STACKEMPTY, DROP, NDROP, NIP, NNIP,
	}

	/* The type of this token */
	public SCLToken.Type type;

	/* Used for ILIT */
	public long intVal;
	/* Used for FLIT */
	public double floatVal;
	/* Used for BLIT */
	public boolean boolVal;
	/* Used for SYMBOL & SLIT */
	public String stringVal;
	/* Used for WORD */
	public SCLToken tokenVal;
	/* Used for WORDS & ARRAY */
	public IList<SCLToken> tokenVals;

	/* Create a new token. */
	public SCLToken(final SCLToken.Type typ) {
		type = typ;
	}

	/* Create a new token. */
	public SCLToken(final SCLToken.Type typ, final long iVal) {
		this(typ);

		intVal = iVal;
	}

	/* Create a new token. */
	public SCLToken(final SCLToken.Type typ, final double dVal) {
		this(typ);

		floatVal = dVal;
	}

	/* Create a new token. */
	public SCLToken(final SCLToken.Type typ, final boolean bVal) {
		this(typ);

		boolVal = bVal;
	}

	/* Create a new token. */
	public SCLToken(final SCLToken.Type typ, final String sVal) {
		this(typ);

		stringVal = sVal;
	}

	/* Create a new token. */
	public SCLToken(final SCLToken.Type typ, final SCLToken tVal) {
		this(typ);

		tokenVal = tVal;
	}

	/* Create a new token. */
	public SCLToken(final SCLToken.Type typ, final SCLToken.Type tVal) {
		this(typ, new SCLToken(tVal));
	}

	/* Create a new token. */
	public SCLToken(final SCLToken.Type typ, final IList<SCLToken> tVals) {
		this(typ);

		tokenVals = tVals;
	}

	/* Convert a string into a token. */
	public static SCLToken tokenizeString(final String token) {
		if (litTokens.containsKey(token)) {
			return new SCLToken(litTokens.get(token));
		} else if (token.startsWith("\\")) {
			return new SCLToken(SYMBOL, token.substring(1));
		} else if (builtinWords.containsKey(token)) {
			return new SCLToken(WORD, builtinWords.get(token));
		} else if (token.equals("true")) {
			return new SCLToken(BLIT, true);
		} else if (token.equals("false")) {
			return new SCLToken(BLIT, false);
		} else if (TokenUtils.isInt(token)) {
			return new SCLToken(ILIT, Long.parseLong(token));
		} else if (TokenUtils.isDouble(token)) {
			return new SCLToken(FLIT, Double.parseDouble(token));
		} else {
			Errors.inst.printError(EK_SCL_INVTOKEN, token);
			return null;
		}
	}

	/* The literal tokens. */
	private static final Map<String, SCLToken.Type> litTokens;
	/* The builtin words. */
	private static final Map<String, SCLToken.Type> builtinWords;

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