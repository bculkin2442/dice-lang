package bjc.dicelang.dice;

/**
 * A simple list of dice.
 *
 * @author EVE
 *
 */
public class SimpleDieList implements DieList {
	private Die	numDice;
	private Die	size;

	/**
	 * Create a new list of dice.
	 *
	 * @param nDice
	 *                The number of dice in the list.
	 *
	 * @param sze
	 *                The size of dice in the list.
	 */
	public SimpleDieList(Die nDice, Die sze) {
		numDice = nDice;
		size = sze;
	}

	@Override
	public boolean canOptimize() {
		if(size.canOptimize() && size.optimize() <= 1)
			return numDice.canOptimize();
		else
			return false;
	}

	@Override
	public long[] optimize() {
		int sze = (int) numDice.optimize();
		long res = size.optimize();

		long[] ret = new long[sze];

		for(int i = 0; i < sze; i++) {
			ret[i] = res;
		}

		return ret;
	}

	@Override
	public long[] roll() {
		int num = (int) numDice.roll();

		long[] ret = new long[num];

		for(int i = 0; i < num; i++) {
			ret[i] = size.roll();
		}

		return ret;
	}

	@Override
	public String toString() {
		return numDice.toString() + "dl" + size.toString();
	}
}