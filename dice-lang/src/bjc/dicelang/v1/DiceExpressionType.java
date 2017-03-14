package bjc.dicelang.v1;

/**
 * Enumeration for basic dice expression operators
 */
public enum DiceExpressionType {
	/**
	 * Add two expressions
	 */
	ADD,

	/**
	 * Divide two expressions
	 */
	DIVIDE,

	/**
	 * Multiply two expressions
	 */
	MULTIPLY,

	/**
	 * Subtract two expressions
	 */
	SUBTRACT;

	@Override
	public String toString() {
		switch(this) {
		case ADD:
			return "+";
		case DIVIDE:
			return "/";
		case MULTIPLY:
			return "*";
		case SUBTRACT:
			return "-";
		default:
			throw new IllegalArgumentException(
					"Got passed  a invalid ScalarExpressionType " + this + ". WAT");
		}
	};
}
