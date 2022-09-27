package bjc.dicelang.neodice.statements;

import static bjc.dicelang.neodice.statements.StatementValue.Type.*;

import java.util.*;

import bjc.dicelang.neodice.*;

/**
 * A StatementValue that represents a die pool
 * @author Ben Culkin
 *
 */
public class DiePoolStatementValue extends StatementValue {
    /** The type of the contained value. */
	public final Type elementType;
	/** The die pool itself. */
	public final DiePool<StatementValue> value;
	
	/**
	 * Create a new diepool value.
	 * 
	 * @param elementType The contained type.
	 * @param value The die pool itself.
	 */
	public DiePoolStatementValue(Type elementType, DiePool<StatementValue> value) {
		super(DIEPOOL);
		
		this.elementType = elementType;
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