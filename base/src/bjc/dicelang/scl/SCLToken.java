package bjc.dicelang.scl;

import java.util.HashMap;
import java.util.Map;

import bjc.dicelang.Errors;
import bjc.utils.parserutils.TokenUtils;

import static bjc.dicelang.Errors.ErrorKey.EK_SCL_INVTOKEN;

import static bjc.dicelang.scl.SCLToken.Type.*;
import static bjc.dicelang.scl.SCLToken.Word.*;

public class SCLToken {

	public static enum Type {
		/* Natural tokens. These come directly from strings */
		ILIT, FLIT, BLIT, SQUOTE, DQUOTE, OBRACKET, OBRACE, SYMBOL, WORD,

		/* Synthetic tokens. These are produced from special tokens. */
		SLIT, WORDS, ARRAY,
	}

	public static enum Word {
		/* Array manipulation */
		MAKEARRAY, MAKEEXEC, MAKEUNEXEC,
		/* Stream manipulation */
		NEWSTREAM, LEFTSTREAM, RIGHTSTREAM, DELETESTREAM, MERGESTREAM,
		/* Stack manipulation */
		STACKCOUNT, STACKEMPTY, DROP, NDROP, NIP, NNIP,
	}

	public SCLToken.Type type;

	public static SCLToken tokenizeString(final String token) {
		if (litTokens.containsKey(token)) {
			return new IntSCLToken(litTokens.get(token));
		} else if (token.startsWith("\\")) {
			return new StringSCLToken(true, token.substring(1));
		} else if (builtinWords.containsKey(token)) {
			return new WordSCLToken(builtinWords.get(token));
		} else if (token.equals("true")) {
			return new BooleanSCLToken(true);
		} else if (token.equals("false")) {
			return new BooleanSCLToken(false);
		} else if (TokenUtils.isInt(token)) {
			return new IntSCLToken(Long.parseLong(token));
		} else if (TokenUtils.isDouble(token)) {
			return new FloatSCLToken(Double.parseDouble(token));
		} else {
			Errors.inst.printError(EK_SCL_INVTOKEN, token);
			return null;
		}
	}

	protected static final Map<String, Type> litTokens;
	protected static final Map<String, Word> builtinWords;

	protected SCLToken() {

	}

	protected SCLToken(Type typ) {
		type = typ;
	}

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SCLToken other = (SCLToken) obj;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SCLToken [type=" + type + "]";
	}
}