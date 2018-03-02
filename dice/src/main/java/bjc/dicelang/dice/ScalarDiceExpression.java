package bjc.dicelang.dice;

public class ScalarDiceExpression implements DiceExpression {
	/**
	 * The scalar value in this expression, if there is one.
	 */
	public Die scalar;

	public ScalarDiceExpression(Die scal) {
		scalar = scal;
	}

	@Override
	public boolean isList() {
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((scalar == null) ? 0 : scalar.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		ScalarDiceExpression other = (ScalarDiceExpression) obj;
		if(scalar == null) {
			if(other.scalar != null) return false;
		} else if(!scalar.equals(other.scalar)) return false;
		return true;
	}

	@Override
	public String toString() {
		return scalar.toString();
	}

	@Override
	public String value() {
		return Long.toString(scalar.roll());
	}
}
