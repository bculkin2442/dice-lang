package bjc.dicelang.dice;

/**
 * A simple list of dice.
 *
 * @author EVE
 *
 */

/*
 * @TODO 10/08/17 Ben Culkin :DieListGeneralize
 * 
 * DieList in general should be changed to be able to be constructed from an
 * arbitrary die using rollSingle and things like that.
 */
public class SimpleDieList implements DieList {
	/* The number of dice to roll. */
	private final Die numDice;
	/*
	 * The size of each die to roll.
	 *
	 * Checked once per roll, not once per dice rolled.
	 */
	private final Die size;

	/**
	 * Create a new list of dice.
	 *
	 * @param nDice
	 *        The number of dice in the list.
	 *
	 * @param sze
	 *        The size of dice in the list.
	 */
	public SimpleDieList(final Die nDice, final Die sze) {
		numDice = nDice;
		size = sze;
	}

	@Override
	public boolean canOptimize() {
		if(size.canOptimize() && size.optimize() <= 1) {
			return numDice.canOptimize();
		}

		return false;
	}

	@Override
	public long[] optimize() {
		final int sze = (int) numDice.optimize();
		final long res = size.optimize();

		final long[] ret = new long[sze];

		for(int i = 0; i < sze; i++) {
			ret[i] = res;
		}

		return ret;
	}

	@Override
	public long[] roll() {
		final int num = (int) numDice.roll();

		final long[] ret = new long[num];

		for(int i = 0; i < num; i++) {
			ret[i] = size.roll();
		}

		return ret;
	}

	@Override
	public String toString() {
		return numDice.toString() + "dl" + size.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((numDice == null) ? 0 : numDice.hashCode());
		result = prime * result + ((size == null) ? 0 : size.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		SimpleDieList other = (SimpleDieList) obj;
		if(numDice == null) {
			if(other.numDice != null) return false;
		} else if(!numDice.equals(other.numDice)) return false;
		if(size == null) {
			if(other.size != null) return false;
		} else if(!size.equals(other.size)) return false;
		return true;
	}
}
