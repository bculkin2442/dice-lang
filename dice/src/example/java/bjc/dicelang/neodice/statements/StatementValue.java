package bjc.dicelang.neodice.statements;

import java.util.*;

public abstract class StatementValue {
	public static enum Type {
		VOID,
		BOOLEAN,
		INTEGER,
		
		DIE,
		DIEPOOL,
		
		ARRAY,
	}
	
	public final Type type;
	
	protected StatementValue(Type type) {
		this.type = type;
	}

	@Override
	public int hashCode() {
		return Objects.hash(type);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)                  return true;
		if (obj == null)                  return false;
		if (getClass() != obj.getClass()) return false;

		StatementValue other = (StatementValue) obj;
		
		return type == other.type;
	}
}