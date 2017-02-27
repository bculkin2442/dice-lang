package bjc.dicelang.v1;

/**
 * Implements a collection of one or more of a particular die, where the
 * number of dice in the group is variable.
 * 
 * @author ben
 *
 */
public class ComplexDice implements IDiceExpression {
	/**
	 * Create a dice from a string expression
	 * 
	 * @param expression
	 *            The string to parse the dice from
	 * @return A dice group parsed from the string
	 */
	public static IDiceExpression fromString(String expression) {
		// Handle the case where someone passes us a simple expression
		// containing a single die
		if (!expression.contains("d")) {
			return new Die(Integer.parseInt(expression));
		}

		// Split it on the dice type marker

		String[] strangs = expression.split("d");

		try {
			// Create the actual group of dice
			return new ComplexDice(
					new ScalarDie(Integer.parseInt(strangs[0])),
					new Die(Integer.parseInt(strangs[1])));
		} catch (NumberFormatException nfex) {
			// We don't care about details

			// Tell the user the expression is invalid
			throw new IllegalArgumentException(
					"Attempted to create a set of dice using invalid arguments."
							+ " They must be integers. " + strangs[0]
							+ " and " + strangs[1]
							+ " are likely culprits.");
		}
	}

	/*
	 * The die being rolled
	 */
	private IDiceExpression	die;

	/*
	 * The number of the particular die to roll
	 */
	private IDiceExpression	nDice;

	/**
	 * Create a new collection of dice
	 * 
	 * @param nDce
	 *            The number of dice in the collection
	 * @param de
	 *            The type of dice the collection is composed of
	 */
	public ComplexDice(IDiceExpression nDce, IDiceExpression de) {
		nDice = nDce;
		die = de;
	}

	/**
	 * Create a new collection of dice
	 * 
	 * @param nSides
	 *            The number of dice in the collection
	 * @param de
	 *            The type of dice the collection is composed of
	 */
	public ComplexDice(int nSides, int de) {
		nDice = new ScalarDie(nSides);
		die = new Die(de);
	}

	@Override
	public boolean canOptimize() {
		// Can only optimize this dice group if both components can be
		// optimized and the die itself has only one value
		if (nDice.canOptimize() && die.canOptimize()) {
			return die.optimize() == 1;
		}

		return false;
	}

	@Override
	public int optimize() {
		if (!canOptimize()) {
			throw new UnsupportedOperationException(
					"This complex dice cannot be optimized. "
							+ "Both the dice to be rolled and the number of"
							+ " dice must be optimizable.");
		}

		return nDice.optimize();
	}

	@Override
	public int roll() {
		int res = 0;

		/*
		 * Add the results of rolling each die
		 */
		int nRoll = nDice.roll();

		if (nRoll < 0) {
			throw new UnsupportedOperationException(
					"Attempted to roll a negative number of dice. "
							+ "The problematic expression is " + nDice);
		}

		// Roll all the dice and combine them
		for (int i = 0; i < nRoll; i++) {
			res += die.roll();
		}

		return res;
	}

	@Override
	public String toString() {
		// Print simple dice groups in a much clearer manner
		if (nDice instanceof ScalarDie && die instanceof Die) {
			return nDice.toString() + die.toString();
		}

		return "complex[n=" + nDice.toString() + ", d=" + die.toString()
				+ "]";
	}
}
