package bjc.dicelang;

/**
 * Implements a class for combining two dice with an operator
 * 
 * @author ben
 *
 */
public class OperatorDiceExpression implements IDiceExpression {
	/**
	 * The operator to use for combining the dice
	 */
	private DiceExpressionType	det;

	/**
	 * The dice on the left side of the expression
	 */
	private IDiceExpression		left;

	/**
	 * The dice on the right side of the expression
	 */
	private IDiceExpression		right;

	/**
	 * Create a new compound expression using the specified parameters
	 * 
	 * @param right
	 *            The die on the right side of the expression
	 * @param left
	 *            The die on the left side of the expression
	 * @param det
	 *            The operator to use for combining the dices
	 */
	public OperatorDiceExpression(IDiceExpression right,
			IDiceExpression left, DiceExpressionType det) {
		this.right = right;
		this.left = left;
		this.det = det;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see bjc.utils.dice.IDiceExpression#roll()
	 */
	@Override
	public int roll() {
		/*
		 * Handle each operator
		 */
		switch (det) {
			case ADD:
				return right.roll() + left.roll();
			case SUBTRACT:
				return right.roll() - left.roll();
			case MULTIPLY:
				return right.roll() * left.roll();
			case DIVIDE:
				/*
				 * Round to keep results as integers. We don't really have
				 * any need for floating-point dice
				 */
				try {
					return right.roll() / left.roll();
				} catch (ArithmeticException aex) {
					UnsupportedOperationException usex =
							new UnsupportedOperationException(
									"Attempted to divide by zero."
											+ " Problematic expression is "
											+ left);

					usex.initCause(aex);

					throw usex;
				}
			default:
				throw new IllegalArgumentException(
						"Got passed  a invalid ScalarExpressionType " + det
								+ ". WAT");

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "dice-exp[type=" + det + ", l=" + left.toString() + ", r="
				+ right.toString() + "]";
	}
}