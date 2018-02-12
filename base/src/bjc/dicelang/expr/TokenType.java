package bjc.dicelang.expr;

/**
 * Represents the type of this token.
 *
 * @author Ben Culkin
 */
public enum TokenType {
	/*
	 * @NOTE Do we want to switch to auto-numbering the tokens? They were manually
	 * numbered because this was an assignment for PoPL and that was what Dr. Naz
	 * wanted.
	 */

	/** Represents + */
	ADD(14, true, 0),
	/** Represents - */
	SUBTRACT(15, true, 0),
	/** Represents * */
	MULTIPLY(16, true, 1),
	/** Represents / */
	DIVIDE(17, true, 1),

	/** Represents variable names. */
	VREF(11),

	/** Represents ( */
	OPAREN(0, false, 100),
	/** Represents ) */
	CPAREN(0, false, 100);

	/** The ID number for this token type. */
	public final int nVal;

	/** Whether or not this type of token is an operator. */
	public final boolean isOperator;
	/** The priority of this operator, if it is one. */
	public final int operatorPriority;

	/* Create a new token. */
	private TokenType(final int num, final boolean isOp, final int priority) {
		nVal = num;
		isOperator = isOp;
		operatorPriority = priority;
	}

	private TokenType(final int num) {
		this(num, false, -1);
	}

	@Override
	public String toString() {
		return Integer.toString(nVal);
	}
}
