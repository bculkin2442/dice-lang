package bjc.dicelang.neodice.statements;

import static bjc.dicelang.neodice.statements.StatementValue.Type.*;

import java.util.*;

import bjc.dicelang.neodice.*;

public class DiePoolStatementValue extends StatementValue {
	public final DiePool value;
	
	public DiePoolStatementValue(DiePool value) {
		super(DIEPOOL);
		
		this.value = value;
	}
	
	@Override
	public String toString() {
		return "(" + value.toString() + ")";
	}

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)                  return true;
		if (obj == null)                  return false;
		if (getClass() != obj.getClass()) return false;

		DiePoolStatementValue other = (DiePoolStatementValue) obj;
		
		return Objects.equals(value, other.value);
	}
}