package bjc.dicelang.eval;

public class FloatEvaluatorResult extends EvaluatorResult {
	/**
	 * The float value of the result.
	 */
	public double floatVal;

	public FloatEvaluatorResult(double val) {
		super(Type.FLOAT);

		floatVal = val;
	}

	@Override
	public String toString() {
		return super.toString() + "(" + floatVal + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(floatVal);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		FloatEvaluatorResult other = (FloatEvaluatorResult) obj;
		if(Double.doubleToLongBits(floatVal) != Double.doubleToLongBits(other.floatVal)) return false;
		return true;
	}
}
