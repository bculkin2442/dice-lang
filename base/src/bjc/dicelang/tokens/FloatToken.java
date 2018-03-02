package bjc.dicelang.tokens;

public class FloatToken extends Token {
	public double floatValue;

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
		if(this == obj) return true;
		if(!super.equals(obj)) return false;
		if(getClass() != obj.getClass()) return false;
		FloatToken other = (FloatToken) obj;
		if(Double.doubleToLongBits(floatValue) != Double.doubleToLongBits(other.floatValue)) return false;
		return true;
	}
}
