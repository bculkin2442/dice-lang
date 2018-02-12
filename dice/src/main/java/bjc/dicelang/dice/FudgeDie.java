package bjc.dicelang.dice;

/**
 * A fudge die, one that has -1, 0 and 1 as its sides.
 *
 * @author EVE
 *
 */
public class FudgeDie implements Die {
	/* The number of dice to roll. */
	private final Die numDice;

	/**
	 * Create a new fudge die.
	 *
	 * @param nDice
	 *            The number of dice to roll.
	 */
	public FudgeDie(final long nDice) {
		numDice = new ScalarDie(nDice);
	}

	/**
	 * Create a new fudge die.
	 *
	 * @param nDice
	 *            The number of dice to roll.
	 */
	public FudgeDie(final Die nDice) {
		numDice = nDice;
	}

	@Override
	public boolean canOptimize() {
		return numDice.canOptimize() && numDice.optimize() == 0;
	}

	@Override
	public long optimize() {
		return 0;
	}

	@Override
	public long roll() {
		long res = 0;

		final long nDice = numDice.roll();

		for (int i = 0; i < nDice; i++) {
			res += rollSingle();
		}

		return res;
	}

	@Override
	public long rollSingle() {
		return DiceBox.rng.nextInt(3) - 1;
	}

	@Override
	public String toString() {
		String dieString = numDice.toString();

		return String.format("%sdF", dieString);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((numDice == null) ? 0 : numDice.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FudgeDie other = (FudgeDie) obj;
		if (numDice == null) {
			if (other.numDice != null)
				return false;
		} else if (!numDice.equals(other.numDice))
			return false;
		return true;
	}
}
