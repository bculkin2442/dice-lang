package bjc.dicelang.scl.tokens;

import java.util.HashMap;
import java.util.Map;

import bjc.dicelang.scl.Errors;
import bjc.utils.parserutils.TokenUtils;

import static bjc.dicelang.scl.Errors.ErrorKey.*;
import static bjc.dicelang.scl.tokens.TokenType.*;

/**
 * Base class for SCL tokens.
 * 
 * @author student
 *
 */
public class SCLToken {
	/**
	 * The type of the token.
	 */
	public TokenType type;

	/**
	 * Convert a string into a token.
	 * 
	 * @param token
	 *            The string to convert into a token.
	 * @return The token.
	 */
	public static SCLToken tokenizeString(final String token) {
		if (litTokens.containsKey(token)) {
			return new SCLToken(litTokens.get(token));
		} else if (token.startsWith("\\")) {
			return new SymbolSCLToken(token.substring(1));
		} else if (WordSCLToken.isBuiltinWord(token)) {
			return new WordSCLToken(token);
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

	protected static final Map<String, TokenType> litTokens;

	protected SCLToken() {

	}

	protected SCLToken(TokenType typ) {
		type = typ;
	}

	static {
		/* Init literal tokens. */
		litTokens = new HashMap<>();

		litTokens.put("'", SQUOTE);
		litTokens.put("\"", DQUOTE);
		litTokens.put("[", OBRACKET);
		litTokens.put("{", OBRACE);
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