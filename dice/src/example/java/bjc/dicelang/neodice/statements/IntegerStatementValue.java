package bjc.dicelang.neodice.statements;

import static bjc.dicelang.neodice.statements.StatementValue.Type.*;

public class IntegerStatementValue extends StatementValue {
	public final int value;
	
	public IntegerStatementValue(int value) {
		super(INTEGER);
		
		this.value = value;
	}
	
	@Override
	public String toString() {
		return "(" + value + ")";
	}
}