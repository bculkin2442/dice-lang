package bjc.dicelang;

/**
 * A dice expression that combines a scalar and a dice
 * 
 * @author ben
 *
 */
public class ScalarDiceExpression implements IDiceExpression {
	/**
	 * The operation to combine with
	 */
	private DiceExpressionType	expressionType;

	/**
	 * The expression to be combined
	 */
	private IDiceExpression		expression;

	/**
	 * The scalar to be combined
	 */
	private int					scalar;

	/**
	 * Create a dice expression with a scalar
	 * 
	 * @param expr
	 *            The dice to use
	 * @param scalr
	 *            The scalar to use
	 * @param type
	 *            The operation to combine with
	 */
	public ScalarDiceExpression(IDiceExpression expr, int scalr,
			DiceExpressionType type) {
		expression = expr;
		scalar = scalr;
		expressionType = type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see bjc.utils.dice.IDiceExpression#roll()
	 */
	@Override
	public int roll() {
		switch (expressionType) {
			case ADD:
				return expression.roll() + scalar;
			case SUBTRACT:
				return expression.roll() - scalar;
			case MULTIPLY:
				return expression.roll() * scalar;
			case DIVIDE:
				try {
					return expression.roll() / scalar;
				} catch (ArithmeticException aex) {
					UnsupportedOperationException usex =
							new UnsupportedOperationException(
									"Attempted to divide by zero.");

					usex.initCause(aex);

					throw usex;
				}
			default:
				throw new IllegalStateException(
						"Got passed  a invalid ScalarExpressionType "
								+ expressionType);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "scalar-exp[type=" + expressionType + ", l=" + scalar
				+ ", r=" + expression.toString() + "]";
	}
}