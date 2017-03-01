package bjc.dicelang.dice;

public class ScalarDie implements Die {
	private long val;

	public ScalarDie(long vl) {
		val = vl;
	}

	public boolean canOptimize() {
		return true;
	}

	public long optimize() {
		return val;
	}

	public long roll() {
		return val;
	}

	public long rollSingle() {
		return val;
	}

	public String toString() {
		return Long.toString(val);
	}
}