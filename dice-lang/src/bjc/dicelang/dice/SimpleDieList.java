package bjc.dicelang.dice;

/**
 * A simple list of dice.
 *
 * @author EVE
 *
 */
public class SimpleDieList implements DieList {
	private final Die       numDice;
	private final Die       size;

	/**
	 * Create a new list of dice.
	 *
	 * @param nDice
	 *                The number of dice in the list.
	 *
	 * @param sze
	 *                The size of dice in the list.
	 */
	public SimpleDieList(final Die nDice, final Die sze) {
		numDice = nDice;
		size = sze;
	}

	@Override
	public boolean canOptimize() {
		if (size.canOptimize() && size.optimize() <= 1) return numDice.canOptimize();

		return false;
	}

	@Override
	public long[] optimize() {
		final int sze = (int) numDice.optimize();
		final long res = size.optimize();

		final long[] ret = new long[sze];

		for (int i = 0; i < sze; i++) {
			ret[i] = res;
		}

		return ret;
	}

	@Override
	public long[] roll() {
		final int num = (int) numDice.roll();

		final long[] ret = new long[num];

		for (int i = 0; i < num; i++) {
			ret[i] = size.roll();
		}

		return ret;
	}

	@Override
	public String toString() {
		return numDice.toString() + "dl" + size.toString();
	}
}