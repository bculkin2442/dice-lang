package bjc.dicelang.v1.ast;

/**
 * Represents a integer-valued result
 *
 * @author ben
 *
 */
public class IntegerResult implements IResult {
	private int value;

	/**
	 * Create a new integer valued result
	 *
	 * @param val
	 *                The value of the result
	 */
	public IntegerResult(int val) {
		value = val;
	}

	@Override
	public ResultType getType() {
		return ResultType.INTEGER;
	}

	/**
	 * Get the value of this result
	 *
	 * @return The value of this result
	 */
	public int getValue() {
		return value;
	}

	@Override
	public String toString() {
		return Integer.toString(value);
	}
}
