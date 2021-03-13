package bjc.dicelang.neodice.statements;

import static bjc.dicelang.neodice.statements.StatementValue.Type.*;

/**
 * The statement value of the null type.
 * @author Ben Culkin
 *
 */
public class VoidStatementValue extends StatementValue {
    /** The singular instance of the null value. */
    public static final VoidStatementValue VOID_INST = new VoidStatementValue();
	
	private VoidStatementValue() {
		super(VOID);
	}
	
	@Override
	public String toString() {
		return "(void)";
	}
}
