package bjc.dicelang.dice;

/**
 * A die that represents two dice with an applied math operator.
 *
 * @author EVE
 *
 */
public class MathDie implements Die {
	/**
	 * The types of a math operator.
	 *
	 * @author EVE
	 *
	 */
	public static enum MathOp {
		/**
		 * Add two dice.
		 */
		ADD,
		/**
		 * Subtract two dice.
		 */
		SUBTRACT,
		/**
		 * Multiply two dice.
		 */
		MULTIPLY;

		@Override
		public String toString() {
			switch(this) {
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

	private MathDie.MathOp type;

	private Die	left;
	private Die	right;

	/**
	 * Create a new math die.
	 *
	 * @param op
	 *                The operator to apply.
	 *
	 * @param lft
	 *                The left operand.
	 *
	 * @param rght
	 *                The right operand.
	 */
	public MathDie(MathDie.MathOp op, Die lft, Die rght) {
		type = op;

		left = lft;
		right = rght;
	}

	@Override
	public boolean canOptimize() {
		return left.canOptimize() && right.canOptimize();
	}

	private long performOp(long lft, long rght) {
		switch(type) {
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
		long lft = left.optimize();
		long rght = right.optimize();

		return performOp(lft, rght);
	}

	@Override
	public long roll() {
		long lft = left.roll();
		long rght = right.roll();

		return performOp(lft, rght);
	}

	@Override
	public long rollSingle() {
		long lft = left.rollSingle();
		long rght = right.rollSingle();

		return performOp(lft, rght);
	}

	@Override
	public String toString() {
		return left.toString() + " " + type.toString() + " " + right.toString();
	}
}