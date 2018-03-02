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

	// These may or may not have values based
	// off of the result type
	/**
	 * The integer value of the result.
	 */
	public long intVal;

	/**
	 * Create a new result.
	 *
	 * @param typ
	 *        The type of the result.
	 */
	protected EvaluatorResult(final EvaluatorResult.Type typ) {
		type = typ;
	}

	/**
	 * Create a new result.
	 *
	 * @param typ
	 * @param iVal
	 */
	public EvaluatorResult(final EvaluatorResult.Type typ, final long iVal) {
		this(typ);

		intVal = iVal;
	}

	@Override
	public String toString() {
		return type.toString();
	}
}
