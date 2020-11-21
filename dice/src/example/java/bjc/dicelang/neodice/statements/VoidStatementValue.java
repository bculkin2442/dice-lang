package bjc.dicelang.neodice.statements;

import static bjc.dicelang.neodice.statements.StatementValue.Type.*;

/**
 * @author Ben Culkin
 *
 */
public class VoidStatementValue extends StatementValue {
	public static final VoidStatementValue VOID_INST = new VoidStatementValue();
	
	private VoidStatementValue() {
		super(VOID);
	}
	
	@Override
	public String toString() {
		return "(void)";
	}
}
