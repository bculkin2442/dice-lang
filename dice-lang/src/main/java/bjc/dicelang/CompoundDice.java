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
	private IDiceExpression	l;

	/**
	 * The right die of the expression
	 */
	private IDiceExpression	r;

	/**
	 * Create a new compound dice using the specified dice
	 * 
	 * @param l
	 *            The die to use on the left
	 * @param r
	 *            The die to use on the right
	 */
	public CompoundDice(IDiceExpression l, IDiceExpression r) {
		this.l = l;
		this.r = r;
	}

	/**
	 * Create a new compound dice from two dice strings
	 * 
	 * @param l
	 *            The left side dice
	 * @param r
	 *            The right side dice
	 */
	public CompoundDice(String l, String r) {
		this(ComplexDice.fromString(l), ComplexDice.fromString(r));
	}

	/**
	 * Create a new compound dice from an array of dice strings
	 * 
	 * @param exps
	 *            An array of dice strings
	 */
	public CompoundDice(String[] exps) {
		this(exps[0], exps[1]);
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
		return Integer.parseInt(l.roll() + "" + r.roll());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "compound[l=" + l.toString() + ", r=" + r.toString() + "]";
	}

	@Override
	public int optimize() {
		return Integer.parseInt(l.optimize() + "" + r.optimize());
	}

	@Override
	public boolean canOptimize() {
		return l.canOptimize() && r.canOptimize();
	}
}