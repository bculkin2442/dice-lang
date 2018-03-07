package bjc.dicelang.scl.tokens;

/**
 * Represents a literal string token.
 * 
 * @author student
 *
 */
public class StringLitSCLToken extends StringSCLToken {

	/**
	 * Create a new literal string token.
	 * 
	 * @param val
	 *            The string value of the token.
	 */
	public StringLitSCLToken(String val) {
		super(false, val);
	}

	@Override
	public String toString() {
		return "StringLitSCLToken [stringVal=" + stringVal + "]";
	}
}
