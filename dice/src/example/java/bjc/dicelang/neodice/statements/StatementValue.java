package bjc.dicelang.neodice.statements;

import java.util.*;

/**
 * Represents a value for diebox, as a base class.
 * @author Ben Culkin
 *
 */
public abstract class StatementValue {
    /**
     * The type of the value.
     * @author Ben Culkin
     *
     */
	public static enum Type {
	    /** The 'void' value. There is only one value of this type. */
		VOID,
		/** The 'boolean' type. There is one true value, and one false value. */
		BOOLEAN,
		/** Represents an integer. */
		INTEGER,
		
		/** Represents a single die. */
		DIE,
		/** Represents a pool of dice. */
		DIEPOOL,
		
		/** Represents an array of some type. */
		ARRAY,
	}
	
	/** The type of this value. */
	public final Type type;
	
	/**
	 * Create a new statement value.
	 * @param type The type of the value.
	 */
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