package bjc.dicelang.expr;

/*
 * @TODO 10/08/17 :TokenReorg
 * 	I am not a fan of this 'having a bunch of subclasses' in one thing I
 * 	seem to have been doing around this project. This should be multiple
 * 	subclasses, one for each value for TokenType.
 */
/**
 * Represents a lexical token.
 *
 * @author Ben Culkin
 */
public class Token {
	/* The state for this token. */
	private final Tokens tks;

	/**
	 * The type of the token.
	 *
	 * Determines which fields have a value.
	 */
	public final TokenType typ;

	/** The integer value attached to this token. */
	public int intValue;

	/** The original string this token was part of. */
	public String rawValue;

	/**
	 * Create a new token.
	 *
	 * @param type
	 *                The type of this token.
	 *
	 * @param raw
	 *                The string this token came from.
	 *
	 * @param toks
	 *                The state for this token
	 */
	public Token(final TokenType type, final String raw, final Tokens toks) {
		this.typ = type;

		rawValue = raw;
		tks = toks;
	}

	@Override
	public String toString() {
		String typeStr = typ.toString();
		       typeStr = String.format("%s (%s)", typeStr, typ.name());

		if (typ == TokenType.VREF) {
			typeStr += " (ind. " + intValue;
			typeStr += ", sym. \"" + tks.symbolTable.get(intValue) + "\")";
		}

		return String.format("%s (originally from: %s)", typeStr, rawValue);
	}

	/**
	 * Convert this token into the string representation of it.
	 *
	 * @return The string representation of it.
	 */
	public String toExpr() {
		switch (typ) {
		case ADD:
			return "+";
		case SUBTRACT:
			return "-";
		case MULTIPLY:
			return "*";
		case DIVIDE:
			return "/";
		case VREF:
			return tks.symbolTable.get(intValue);
		case OPAREN:
			return "(";
		case CPAREN:
			return ")";
		default:
			return "???";
		}
	}
}
