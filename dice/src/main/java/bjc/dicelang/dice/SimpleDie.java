package bjc.dicelang.dice;

/**
 * A simple group of dice.
 *
 * @author EVE
 *
 */
public class SimpleDie implements Die {
	/* The number of dice to roll. */
	private final Die numDice;
	/*
	 * The size of each dice to roll.
	 *
	 * Rolled once per role, not once for each dice rolled.
	 *
	 * @NOTE Would having some way to roll it once for each dice rolled be
	 * useful in any sort of case?
	 */
	private final Die diceSize;

	/**
	 * Create a new dice group.
	 *
	 * @param nDice
	 *        The number of dice.
	 *
	 * @param size
	 *        The size of the dice.
	 */
	public SimpleDie(final long nDice, final long size) {
		this(new ScalarDie(nDice), new ScalarDie(size));
	}

	/**
	 * Create a new dice group.
	 *
	 * @param nDice
	 *        The number of dice.
	 *
	 * @param size
	 *        The size of the dice.
	 */
	public SimpleDie(final Die nDice, final long size) {
		this(nDice, new ScalarDie(size));
	}

	/**
	 * Create a new dice group.
	 *
	 * @param nDice
	 *        The number of dice.
	 *
	 * @param size
	 *        The size of the dice.
	 */
	public SimpleDie(final long nDice, final Die size) {
		this(new ScalarDie(nDice), size);
	}

	/**
	 * Create a new dice group.
	 *
	 * @param nDice
	 *        The number of dice.
	 *
	 * @param size
	 *        The size of the dice.
	 */
	public SimpleDie(final Die nDice, final Die size) {
		numDice = nDice;
		diceSize = size;
	}

	@Override
	public boolean canOptimize() {
		if(diceSize.canOptimize() && diceSize.optimize() <= 1) {
			return numDice.canOptimize();
		}

		return false;
	}

	@Override
	public long optimize() {
		final long optSize = diceSize.optimize();

		if(optSize == 0) {
			return 0;
		}

		return numDice.optimize();
	}

	@Override
	public long roll() {
		long total = 0;

		final long nDice = numDice.roll();
		final long dSize = diceSize.roll();

		for(int i = 0; i < nDice; i++) {
			total += Math.abs(DiceBox.rng.nextLong()) % dSize + 1;
		}

		return total;
	}

	@Override
	public long rollSingle() {
		return Math.abs(DiceBox.rng.nextLong()) % diceSize.roll() + 1;
	}

	@Override
	public String toString() {
		return numDice + "d" + diceSize;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((diceSize == null) ? 0 : diceSize.hashCode());
		result = prime * result + ((numDice == null) ? 0 : numDice.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		SimpleDie other = (SimpleDie) obj;
		if(diceSize == null) {
			if(other.diceSize != null) return false;
		} else if(!diceSize.equals(other.diceSize)) return false;
		if(numDice == null) {
			if(other.numDice != null) return false;
		} else if(!numDice.equals(other.numDice)) return false;
		return true;
	}
}
