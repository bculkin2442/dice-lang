package bjc.dicelang.neodice.statements;

import static bjc.dicelang.neodice.statements.StatementValue.Type.*;

import java.util.*;

/**
 * Represents the boolean type for diebox.
 * @author Ben Culkin
 *
 */
public class BooleanStatementValue extends StatementValue {
	private boolean value;
	
	/** The true boolean instance. */
	public static final BooleanStatementValue TRUE_INST  = new BooleanStatementValue(true);
	/** The false boolean instance. */
	public static final BooleanStatementValue FALSE_INST = new BooleanStatementValue(false);
	
	private BooleanStatementValue(boolean value) {
		super(BOOLEAN);
		
		this.value = value;
	}
	
	@Override
	public String toString() {
		return value ? "(true)" : "(false)";
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

		BooleanStatementValue other = (BooleanStatementValue) obj;
		
		return value == other.value;
	}
}