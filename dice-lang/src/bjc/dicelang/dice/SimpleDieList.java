package bjc.dicelang.dice;

public class SimpleDieList implements DieList {
	private Die numDice;
	private Die size;

	public SimpleDieList(Die nDice, Die sze) {
		numDice = nDice;
		size = sze;
	}

	public boolean canOptimize() {
		if (size.canOptimize() && size.optimize() <= 1) {
			return numDice.canOptimize();
		} else {
			return false;
		}
	}

	public long[] optimize() {
		int sze = (int) numDice.optimize();
		long res = size.optimize();

		long[] ret = new long[sze];

		for (int i = 0; i < sze; i++) {
			ret[i] = res;
		}

		return ret;
	}

	public long[] roll() {
		int num = (int) numDice.roll();

		long[] ret = new long[num];

		for (int i = 0; i < num; i++) {
			ret[i] = size.roll();
		}

		return ret;
	}

	public String toString() {
		return numDice.toString() + "dl" + size.toString();
	}
}