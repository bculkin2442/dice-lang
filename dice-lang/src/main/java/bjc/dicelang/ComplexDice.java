package bjc.dicelang;

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
	 * @param dice
	 *            The string to parse the dice from
	 * @return A dice group parsed from the string
	 */
	public static IDiceExpression fromString(String dice) {
		/*
		 * Split it on the dice type marker
		 */
		String[] strangs = dice.split("d");

		try {
			/*
			 * Create the actual dice
			 */
			return new ComplexDice(
					new ScalarDie(Integer.parseInt(strangs[0])),
					new Die(Integer.parseInt(strangs[1])));
		} catch (@SuppressWarnings("unused") NumberFormatException nfex) {
			// We don't care about details
			/*
			 * Tell the user the expression is invalid
			 */
			throw new IllegalArgumentException(
					"Attempted to create a dice using something that's not"
							+ " an integer: " + strangs[0] + " and "
							+ strangs[1] + " are likely culprits.");
		}
	}

	/**
	 * The die being rolled
	 */
	private IDiceExpression	die;

	/**
	 * The number of the specified die to roll
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see bjc.utils.dice.IDiceExpression#roll()
	 */
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

		for (int i = 0; i < nRoll; i++) {
			res += die.roll();
		}

		return res;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (nDice instanceof ScalarDie && die instanceof Die) {
			return nDice.toString() + die.toString();
		}

		return "complex[n=" + nDice.toString() + ", d=" + die.toString()
				+ "]";
	}

	@Override
	public boolean canOptimize() {
		if (nDice.canOptimize() && die.canOptimize()) {
			return die.optimize() == 1;
		}

		return false;
	}

	@Override
	public int optimize() {
		return nDice.optimize();
	}
}