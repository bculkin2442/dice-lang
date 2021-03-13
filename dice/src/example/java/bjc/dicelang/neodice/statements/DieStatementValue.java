package bjc.dicelang.neodice.statements;

import static bjc.dicelang.neodice.statements.StatementValue.Type.*;

import java.util.*;

import bjc.dicelang.neodice.*;

/**
 * A StatementValue that represents a die.
 * @author Ben Culkin
 *
 */
public class DieStatementValue extends StatementValue {
    /** The type of values this die rolls. */
	public final Type sideType;
	/** The die itself. */
	public final Die<StatementValue> value;
	
	/**
	 * Create a new die StatementValue.
	 * 
	 * @param sideType The type of value this die rolls.
	 * @param value The die itself.
	 */
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