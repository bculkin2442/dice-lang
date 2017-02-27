package bjc.dicelang.v2.dice;

public class CompoundDie implements Die {
	private Die left;
	private Die right;

	public CompoundDie(Die lft, Die rght) {
		left = lft;
		right = rght;
	}

	public boolean canOptimize() {
		return left.canOptimize() && right.canOptimize();
	}

	public long optimize() {
		return Long.parseLong(left.optimize() + "" + right.optimize());
	}

	public long roll() {
		return Long.parseLong(left.roll() + "" + right.roll());
	}

	public long rollSingle() {
		return roll();
	}

	public String toString() {
		return left.toString() + "c" + right.toString();
	}
}