package bjc.dicelang.eval;

/*
 * @TODO 10/09/17 Ben Culkin :EvalResultReorg
 * 
 * Again, split it into separate classes based off of the type.
 */
/**
 * The result from the evaluator.
 *
 * @author EVE
 *
 */
public class EvaluatorResult {
	/**
	 * The type of the result.
	 *
	 * @author EVE
	 *
	 */
	public static enum Type {
		/**
		 * The type of a failure.
		 */
		FAILURE,
		/**
		 * The type of an integer.
		 */
		INT,
		/**
		 * The type of a float.
		 */
		FLOAT,
		/**
		 * The type of a dice.
		 */
		DICE,
		/**
		 * The type of a string.
		 */
		STRING
	}

	/**
	 * The type of the result.
	 */
	public final EvaluatorResult.Type type;

	/**
	 * Create a new result.
	 *
	 * @param typ
	 *            The type of the result.
	 */
	protected EvaluatorResult(final EvaluatorResult.Type typ) {
		type = typ;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		EvaluatorResult other = (EvaluatorResult) obj;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "EvaluatorResult [type=" + type + "]";
	}
}
