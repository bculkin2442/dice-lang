package bjc.dicelang.dice;

/**
 * A die whose rolls result from concatenating two other rolls together.
 *
 * @author Ben Culkin
 */
public class CompoundDie implements Die {
	/*
	 * The dice that form this die
	 */
	private Die left;
	private Die right;

	/**
	 * Create a new compound die.
	 *
	 * @param lft The left die
	 * @param rght The right die
	 */
	public CompoundDie(Die lft, Die rght) {
		left = lft;
		right = rght;
	}

	@Override
	public boolean canOptimize() {
		return left.canOptimize() && right.canOptimize();
	}

	@Override
	public long optimize() {
		return Long.parseLong(left.optimize() + "" + right.optimize());
	}

	@Override
	public long roll() {
		return Long.parseLong(left.roll() + "" + right.roll());
	}

	@Override
	public long rollSingle() {
		/*
		 * We're only one die, we can't be split
		 */
		return roll();
	}

	@Override
	public String toString() {
		return left.toString() + "c" + right.toString();
	}
}
