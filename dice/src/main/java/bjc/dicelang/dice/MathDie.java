package bjc.dicelang.dice;

/**
 * A die that represents two dice with an applied math operator.
 *
 * @author EVE
 *
 */
public class MathDie implements Die {
	/*
	 * @TODO 10/08/17 Ben Culkin :MathGeneralize Why do we have the operator types
	 * hardcoded, instead of just having a general thing for applying a binary
	 * operator to dice? Fix this by changing it to the more general form.
	 */
	/**
	 * The types of a math operator.
	 *
	 * @author EVE
	 *
	 */
	public static enum MathOp {
		/** Add two dice. */
		ADD,
		/** Subtract two dice. */
		SUBTRACT,
		/** Multiply two dice. */
		MULTIPLY;

		@Override
		public String toString() {
			switch (this) {
			case ADD:
				return "+";

			case SUBTRACT:
				return "-";

			case MULTIPLY:
				return "*";

			default:
				return this.name();
			}
		}
	}

	private final MathDie.MathOp type;

	private final Die left;
	private final Die right;

	/**
	 * Create a new math die.
	 *
	 * @param op
	 *            The operator to apply.
	 *
	 * @param lft
	 *            The left operand.
	 *
	 * @param rght
	 *            The right operand.
	 */
	public MathDie(final MathDie.MathOp op, final Die lft, final Die rght) {
		type = op;

		left = lft;
		right = rght;
	}

	@Override
	public boolean canOptimize() {
		return left.canOptimize() && right.canOptimize();
	}

	private long performOp(final long lft, final long rght) {
		switch (type) {
		case ADD:
			return lft + rght;

		case SUBTRACT:
			return lft - rght;

		case MULTIPLY:
			return lft * rght;

		default:
			return 0;
		}
	}

	@Override
	public long optimize() {
		final long lft = left.optimize();
		final long rght = right.optimize();

		return performOp(lft, rght);
	}

	@Override
	public long roll() {
		final long lft = left.roll();
		final long rght = right.roll();

		return performOp(lft, rght);
	}

	@Override
	public long rollSingle() {
		final long lft = left.rollSingle();
		final long rght = right.rollSingle();

		return performOp(lft, rght);
	}

	@Override
	public String toString() {
		return left.toString() + " " + type.toString() + " " + right.toString();
	}
}
