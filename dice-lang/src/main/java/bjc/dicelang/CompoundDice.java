package bjc.dicelang;

/**
 * Implements a "compound dice"
 * 
 * To explain, a compound dice is something like a d100 composed from two
 * d10s instead of a hundred sided die.
 * 
 * @author ben
 *
 */
public class CompoundDice implements IDiceExpression {
	/**
	 * The left die of the expression
	 */
	private IDiceExpression	leftDice;

	/**
	 * The right die of the expression
	 */
	private IDiceExpression	rightDice;

	/**
	 * Create a new compound dice using the specified dice
	 * 
	 * @param left
	 *            The die to use on the left
	 * @param right
	 *            The die to use on the right
	 */
	public CompoundDice(IDiceExpression left, IDiceExpression right) {
		this.leftDice = left;
		this.rightDice = right;
	}

	/**
	 * Create a new compound dice from two dice strings
	 * 
	 * @param leftExp
	 *            The left side dice as a string
	 * @param rightExp
	 *            The right side dice as a string
	 */
	public CompoundDice(String leftExp, String rightExp) {
		this(ComplexDice.fromString(leftExp),
				ComplexDice.fromString(rightExp));
	}

	/**
	 * Create a new compound dice from an array of dice strings
	 * 
	 * @param exps
	 *            An array of two dice strings
	 */
	public CompoundDice(String[] exps) {
		this(exps[0], exps[1]);
	}

	@Override
	public boolean canOptimize() {
		return leftDice.canOptimize() && rightDice.canOptimize();
	}

	@Override
	public int optimize() {
		if (!canOptimize()) {
			throw new UnsupportedOperationException(
					"Cannot optimize this compound dice");
		}

		return Integer
				.parseInt(leftDice.optimize() + "" + rightDice.optimize());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see bjc.utils.dice.IDiceExpression#roll()
	 */
	@Override
	public int roll() {
		/*
		 * Make the combination of the two dice
		 */
		return Integer.parseInt(leftDice.roll() + "" + rightDice.roll());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "compound[l=" + leftDice.toString() + ", r="
				+ rightDice.toString() + "]";
	}
}