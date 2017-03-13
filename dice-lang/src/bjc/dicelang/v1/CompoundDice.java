package bjc.dicelang.v1;

/**
 * Implements a "compound dice"
 * 
 * To explain, a compound dice is something like a d100 composed from two d10s
 * instead of a hundred sided die.
 * 
 * @author ben
 *
 */
public class CompoundDice implements IDiceExpression {
	/*
	 * The left die of the expression
	 */
	private IDiceExpression left;

	/*
	 * The right die of the expression
	 */
	private IDiceExpression right;

	/**
	 * Create a new compound dice using the specified dice
	 * 
	 * @param lft
	 *                The die to use on the left
	 * @param rght
	 *                The die to use on the right
	 */
	public CompoundDice(IDiceExpression lft, IDiceExpression rght) {
		this.left = lft;
		this.right = rght;
	}

	/**
	 * Create a new compound dice from two dice strings
	 * 
	 * @param lft
	 *                The left side dice as a string
	 * @param rght
	 *                The right side dice as a string
	 */
	public CompoundDice(String lft, String rght) {
		this(ComplexDice.fromString(lft), ComplexDice.fromString(rght));
	}

	/**
	 * Create a new compound dice from an array of dice strings
	 * 
	 * @param exps
	 *                An array of two dice strings
	 */
	public CompoundDice(String[] exps) {
		this(exps[0], exps[1]);
	}

	@Override
	public boolean canOptimize() {
		return left.canOptimize() && right.canOptimize();
	}

	@Override
	public int optimize() {
		if (!canOptimize()) {
			throw new UnsupportedOperationException("Cannot optimize this compound dice. "
					+ "Both component dice must be optimizable" + " to optimize a compound dice");
		}

		return Integer.parseInt(left.optimize() + "" + right.optimize());
	}

	@Override
	public int roll() {
		/*
		 * Make the combination of the two dice
		 */
		return Integer.parseInt(left.roll() + "" + right.roll());
	}

	@Override
	public String toString() {
		return "compound[l=" + left.toString() + ", r=" + right.toString() + "]";
	}
}
