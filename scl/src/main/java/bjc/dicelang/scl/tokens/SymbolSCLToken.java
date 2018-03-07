package bjc.dicelang.scl.tokens;

/**
 * Represents a symbol literal.
 * 
 * @author student
 *
 */
public class SymbolSCLToken extends StringSCLToken {

	/**
	 * Create a symbol literal
	 * 
	 * @param val
	 *            The value of the symbol.
	 */
	public SymbolSCLToken(String val) {
		super(true, val);
	}

	@Override
	public String toString() {
		return "SymbolSCLToken [stringVal=" + stringVal + "]";
	}
}
