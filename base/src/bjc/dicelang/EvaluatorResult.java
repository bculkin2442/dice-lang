package bjc.dicelang;

import bjc.dicelang.dice.Die;
import bjc.dicelang.dice.DieExpression;
import bjc.dicelang.dice.DieList;
import bjc.utils.data.ITree;
import bjc.utils.data.Tree;

/*
 * @TODO 10/09/17 Ben Culkin :EvalResultReorg
 * 	Again, split it into seperate classes based off of the type.
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
	 * The float value of the result.
	 */
	public double floatVal;
	/**
	 * The dice value of the result.
	 */
	public DieExpression diceVal;
	/**
	 * The string value of the result.
	 */
	public String stringVal;

	/**
	 * Original node data
	 */
	public ITree<Node> origVal;

	/**
	 * Create a new result.
	 *
	 * @param typ
	 *            The type of the result.
	 */
	public EvaluatorResult(final EvaluatorResult.Type typ) {
		type = typ;
	}

	/**
	 * Create a new result.
	 *
	 * @param typ
	 *            The type of the result.
	 *
	 * @param orig
	 *            The original value of the result.
	 */
	public EvaluatorResult(final EvaluatorResult.Type typ, final ITree<Node> orig) {
		this(typ);

		origVal = orig;
	}

	/**
	 * Create a new result.
	 *
	 * @param typ
	 *            The type of the result.
	 *
	 * @param orig
	 *            The original value of the result.
	 */
	public EvaluatorResult(final EvaluatorResult.Type typ, final Node orig) {
		this(typ, new Tree<>(orig));
	}

	/**
	 * Create a new result.
	 *
	 * @param typ
	 * @param orig
	 */
	public EvaluatorResult(final EvaluatorResult.Type typ, final EvaluatorResult orig) {
		this(typ, new Node(Node.Type.RESULT, orig));
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

	/**
	 * Create a new result.
	 *
	 * @param typ
	 * @param dVal
	 */
	public EvaluatorResult(final EvaluatorResult.Type typ, final double dVal) {
		this(typ);

		floatVal = dVal;
	}

	/**
	 * Create a new result.
	 *
	 * @param typ
	 * @param dVal
	 */
	public EvaluatorResult(final EvaluatorResult.Type typ, final DieExpression dVal) {
		this(typ);

		diceVal = dVal;
	}

	/**
	 * Create a new result.
	 *
	 * @param typ
	 * @param dVal
	 */
	public EvaluatorResult(final EvaluatorResult.Type typ, final Die dVal) {
		this(typ);

		diceVal = new DieExpression(dVal);
	}

	/**
	 * Create a new result.
	 *
	 * @param typ
	 * @param dVal
	 */
	public EvaluatorResult(final EvaluatorResult.Type typ, final DieList dVal) {
		this(typ);

		diceVal = new DieExpression(dVal);
	}

	/**
	 * Create a new result.
	 *
	 * @param typ
	 * @param strang
	 */
	public EvaluatorResult(final EvaluatorResult.Type typ, final String strang) {
		this(typ);

		stringVal = strang;
	}

	@Override
	public String toString() {
		switch (type) {
		case INT:
			return type.toString() + "(" + intVal + ")";

		case FLOAT:
			return type.toString() + "(" + floatVal + ")";

		case DICE:
			return type.toString() + "(" + diceVal + ")";

		case STRING:
			return type.toString() + "(" + stringVal + ")";

		case FAILURE:
			return type.toString();

		default:
			return "Unknown result type " + type.toString();
		}
	}
}
