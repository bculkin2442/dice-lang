package bjc.dicelang.dice;

/**
 * A die whose rolls result from concatenating two other rolls together.
 *
 * @author Ben Culkin
 */
public class CompoundDie implements Die {
	/* The dice that form this die */
	private final Die left;
	private final Die right;

	/**
	 * Create a new compound die.
	 *
	 * @param lft
	 *            The left die
	 * @param rght
	 *            The right die
	 */
	public CompoundDie(final Die lft, final Die rght) {
		left = lft;
		right = rght;
	}

	@Override
	public boolean canOptimize() {
		return left.canOptimize() && right.canOptimize();
	}

	@Override
	public long optimize() {
		long leftOpt = left.optimize();
		long rightOpt = right.optimize();

		return Long.parseLong(String.format("%d%d", leftOpt, rightOpt));
	}

	@Override
	public long roll() {
		long leftRoll = left.optimize();
		long rightRoll = right.optimize();

		return Long.parseLong(String.format("%d%d", leftRoll, rightRoll));
	}

	@Override
	public long rollSingle() {
		/* Actually one dice built using two, can't be split. */
		return roll();
	}

	@Override
	public String toString() {
		String leftString = left.toString();
		String rightString = right.toString();

		return String.format("%sc%s", leftString, rightString);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((left == null) ? 0 : left.hashCode());
		result = prime * result + ((right == null) ? 0 : right.hashCode());
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
		CompoundDie other = (CompoundDie) obj;
		if (left == null) {
			if (other.left != null)
				return false;
		} else if (!left.equals(other.left))
			return false;
		if (right == null) {
			if (other.right != null)
				return false;
		} else if (!right.equals(other.right))
			return false;
		return true;
	}
}
