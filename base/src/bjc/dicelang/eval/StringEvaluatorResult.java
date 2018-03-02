package bjc.dicelang.eval;

public class StringEvaluatorResult extends EvaluatorResult {
	/**
	 * The string value of the result.
	 */
	public String stringVal;

	public StringEvaluatorResult(String strang) {
		super(Type.STRING);

		stringVal = strang;
	}

	@Override
	public String toString() {
		return super.toString() + "(" + stringVal + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((stringVal == null) ? 0 : stringVal.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		StringEvaluatorResult other = (StringEvaluatorResult) obj;
		if(stringVal == null) {
			if(other.stringVal != null) return false;
		} else if(!stringVal.equals(other.stringVal)) return false;
		return true;
	}
}
