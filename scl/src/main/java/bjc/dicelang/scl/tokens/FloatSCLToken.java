package bjc.dicelang.scl.tokens;

/**
 * Represents a floating-point token.
 * 
 * @author student
 *
 */
public class FloatSCLToken extends SCLToken {
	/**
	 * The value of the token.
	 */
	public double floatVal;

	/**
	 * Create a new floating-point token.
	 * 
	 * @param val
	 *            The value of the token.
	 */
	public FloatSCLToken(double val) {
		super(Type.FLIT);

		floatVal = val;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		long temp;
		temp = Double.doubleToLongBits(floatVal);
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
		FloatSCLToken other = (FloatSCLToken) obj;
		if (Double.doubleToLongBits(floatVal) != Double.doubleToLongBits(other.floatVal))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FloatSCLToken [floatVal=" + floatVal + "]";
	}
}
