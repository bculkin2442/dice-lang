package bjc.dicelang.neodice.statements;

import static bjc.dicelang.neodice.statements.StatementValue.Type.*;

/**
 * Statement value that represents an integer.
 * @author Ben Culkin
 *
 */
public class IntegerStatementValue extends StatementValue {
	/** The integer value. */
    public final int value;
	
	/**
	 * Create an integer statement value.
	 * @param value The int to use as the value.
	 */
	public IntegerStatementValue(int value) {
		super(INTEGER);
		
		this.value = value;
	}
	
	@Override
	public String toString() {
		return "(" + value + ")";
	}
}