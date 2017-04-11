package bjc.dicelang.dice;

/**
 * A simple group of dice.
 *
 * @author EVE
 *
 */
public class SimpleDie implements Die {
	private Die	numDice;
	private Die	diceSize;

	/**
	 * Create a new dice group.
	 *
	 * @param nDice
	 *                The number of dice.
	 *
	 * @param size
	 *                The size of the dice.
	 */
	public SimpleDie(long nDice, long size) {
		this(new ScalarDie(nDice), new ScalarDie(size));
	}

	/**
	 * Create a new dice group.
	 *
	 * @param nDice
	 *                The number of dice.
	 *
	 * @param size
	 *                The size of the dice.
	 */
	public SimpleDie(Die nDice, long size) {
		this(nDice, new ScalarDie(size));
	}

	/**
	 * Create a new dice group.
	 *
	 * @param nDice
	 *                The number of dice.
	 *
	 * @param size
	 *                The size of the dice.
	 */
	public SimpleDie(long nDice, Die size) {
		this(new ScalarDie(nDice), size);
	}

	/**
	 * Create a new dice group.
	 *
	 * @param nDice
	 *                The number of dice.
	 *
	 * @param size
	 *                The size of the dice.
	 */
	public SimpleDie(Die nDice, Die size) {
		numDice = nDice;
		diceSize = size;
	}

	@Override
	public boolean canOptimize() {
		if(diceSize.canOptimize() && diceSize.optimize() <= 1)
			return numDice.canOptimize();
		else
			return false;
	}

	@Override
	public long optimize() {
		long optSize = diceSize.optimize();

		if(optSize == 0)
			return 0;
		else
			return numDice.optimize();
	}

	@Override
	public long roll() {
		long total = 0;

		long nDice = numDice.roll();
		long dSize = diceSize.roll();

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
}