package bjc.dicelang.dicev2;

public class ScalarDie extends Die {
	public final long val;

	public ScalarDie(long val) {
		super();

		this.val = val;
	}

	public long[] roll() {
		return new long[] { rollSingle() };
	}

	public long rollSingle() {
		return val;
	}

	public boolean canOptimize() {
		return true;
	}

	public long optimize() {
		return val;
	}
}
