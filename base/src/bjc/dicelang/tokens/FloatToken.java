package bjc.dicelang.tokens;

/**
 * Represents a floating point token.
 * 
 * @author student
 *
 */
public class FloatToken extends Token {
	/**
	 * The value of the token.
	 */
	public double floatValue;

	/**
	 * Create a new floating-point token.
	 * 
	 * @param val
	 *            The value of the token.
	 */
	public FloatToken(double val) {
		super(Type.FLOAT_LIT);

		floatValue = val;
	}

	@Override
	public String toString() {
		return super.toString() + "(" + floatValue + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(floatValue);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		FloatToken other = (FloatToken) obj;
		if (Double.doubleToLongBits(floatValue) != Double.doubleToLongBits(other.floatValue))
			return false;
		return true;
	}
}
