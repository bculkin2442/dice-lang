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
	private DiceExpressionType	det;

	/**
	 * The expression to be combined
	 */
	private IDiceExpression		exp;

	/**
	 * The scalar to be combined
	 */
	private int					scalar;

	/**
	 * Create a dice expression with a scalar
	 * 
	 * @param dex
	 *            The dice to use
	 * @param scalr
	 *            The scalar to use
	 * @param dt
	 *            The operation to combine with
	 */
	public ScalarDiceExpression(IDiceExpression dex, int scalr,
			DiceExpressionType dt) {
		exp = dex;
		scalar = scalr;
		det = dt;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see bjc.utils.dice.IDiceExpression#roll()
	 */
	@Override
	public int roll() {
		switch (det) {
			case ADD:
				return exp.roll() + scalar;
			case SUBTRACT:
				return exp.roll() - scalar;
			case MULTIPLY:
				return exp.roll() * scalar;
			case DIVIDE:
				try {
					return exp.roll() / scalar;
				} catch (ArithmeticException aex) {
					throw new UnsupportedOperationException(
							"Attempted to divide by zero.");
				}
			default:
				throw new IllegalStateException(
						"Got passed  a invalid ScalarExpressionType "
								+ det);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "scalar-exp[type=" + det + ", l=" + scalar + ", r="
				+ exp.toString() + "]";
	}
}