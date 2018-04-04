package bjc.dicelang.eval;

public class IntegerEvaluatorResult extends EvaluatorResult {
	public final long value;
	
	public IntegerEvaluatorResult(long val) {
		super(Type.INT);
		
		value = val;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (value ^ (value >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IntegerEvaluatorResult other = (IntegerEvaluatorResult) obj;
		if (value != other.value)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "IntegerEvaluatorResult [value=" + value + "]";
	}
}
