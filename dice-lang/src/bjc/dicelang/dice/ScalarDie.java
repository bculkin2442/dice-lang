package bjc.dicelang.dice;

public class ScalarDie implements Die {
	private long val;

	public ScalarDie(long vl) {
		val = vl;
	}

	@Override
	public boolean canOptimize() {
		return true;
	}

	@Override
	public long optimize() {
		return val;
	}

	@Override
	public long roll() {
		return val;
	}

	@Override
	public long rollSingle() {
		return val;
	}

	@Override
	public String toString() {
		return Long.toString(val);
	}
}