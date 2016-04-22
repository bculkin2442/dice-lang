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
	private DiceExpressionType	expressionType;

	/**
	 * The dice on the left side of the expression
	 */
	private IDiceExpression		leftExpression;

	/**
	 * The dice on the right side of the expression
	 */
	private IDiceExpression		rightExpression;

	/**
	 * Create a new compound expression using the specified parameters
	 * 
	 * @param right
	 *            The die on the right side of the expression
	 * @param left
	 *            The die on the left side of the expression
	 * @param type
	 *            The operator to use for combining the dices
	 */
	public OperatorDiceExpression(IDiceExpression right,
			IDiceExpression left, DiceExpressionType type) {
		this.rightExpression = right;
		this.leftExpression = left;
		this.expressionType = type;
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
		switch (expressionType) {
			case ADD:
				return rightExpression.roll() + leftExpression.roll();
			case SUBTRACT:
				return rightExpression.roll() - leftExpression.roll();
			case MULTIPLY:
				return rightExpression.roll() * leftExpression.roll();
			case DIVIDE:
				/*
				 * Round to keep results as integers. We don't really have
				 * any need for floating-point dice, and continuous
				 * probability is a pain
				 */
				try {
					return rightExpression.roll() / leftExpression.roll();
				} catch (ArithmeticException aex) {
					UnsupportedOperationException usex = new UnsupportedOperationException(
							"Attempted to divide by zero."
									+ " Problematic expression is "
									+ leftExpression);

					usex.initCause(aex);

					throw usex;
				}
			default:
				throw new IllegalArgumentException(
						"Got passed  a invalid ScalarExpressionType "
								+ expressionType + ". WAT");

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "dice-exp[type=" + expressionType + ", l="
				+ leftExpression.toString() + ", r="
				+ rightExpression.toString() + "]";
	}
}