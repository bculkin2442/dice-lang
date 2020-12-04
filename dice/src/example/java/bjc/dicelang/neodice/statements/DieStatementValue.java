package bjc.dicelang.neodice.statements;

import static bjc.dicelang.neodice.statements.StatementValue.Type.*;

import java.util.*;

import bjc.dicelang.neodice.*;

public class DieStatementValue extends StatementValue {
	public final Type sideType;
	public final Die<StatementValue> value;
	
	public DieStatementValue(Type sideType, Die<StatementValue> value) {
		super(DIE);
		
		this.sideType = sideType;
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

		DieStatementValue other = (DieStatementValue) obj;
		
		return Objects.equals(value, other.value);
	}
}