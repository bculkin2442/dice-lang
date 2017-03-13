package bjc.dicelang.v1;

/**
 * Implements a class for combining two dice with an operator
 * 
 * @author ben
 *
 */
public class OperatorDiceExpression implements IDiceExpression {
	/*
	 * The operator to use for combining the dice
	 */
	private DiceExpressionType type;

	/*
	 * The dice on the left side of the expression
	 */
	private IDiceExpression left;

	/*
	 * The dice on the right side of the expression
	 */
	private IDiceExpression right;

	/**
	 * Create a new compound expression using the specified parameters
	 * 
	 * @param rght
	 *                The die on the right side of the expression
	 * @param lft
	 *                The die on the left side of the expression
	 * @param type
	 *                The operator to use for combining the dices
	 */
	public OperatorDiceExpression(IDiceExpression rght, IDiceExpression lft, DiceExpressionType type) {
		this.right = rght;
		this.left = lft;
		this.type = type;
	}

	@Override
	public int roll() {
		/*
		 * Handle each operator
		 */
		switch (type) {
		case ADD:
			return right.roll() + left.roll();
		case SUBTRACT:
			return right.roll() - left.roll();
		case MULTIPLY:
			return right.roll() * left.roll();
		case DIVIDE:
			/*
			 * Round to keep results as integers. We don't really
			 * have any need for floating-point dice, and continuous
			 * probability is a pain
			 */
			try {
				return right.roll() / left.roll();
			} catch (ArithmeticException aex) {
				UnsupportedOperationException usex = new UnsupportedOperationException(
						"Attempted to divide by zero." + " Problematic expression is " + left);

				usex.initCause(aex);

				throw usex;
			}
		default:
			throw new IllegalArgumentException(
					"Got passed a invalid ScalarExpressionType (" + type + "). WAT");

		}
	}

	@Override
	public String toString() {
		return "dice-exp[type=" + type + ", l=" + left.toString() + ", r=" + right.toString() + "]";
	}
}
