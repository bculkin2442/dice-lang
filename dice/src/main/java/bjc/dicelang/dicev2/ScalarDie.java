package bjc.dicelang.dicev2;

import java.util.Random;

public class ScalarDie extends Die {
	public final long val;

	public ScalarDie(long val) {
		super();

		this.val = val;
	}

	public ScalarDie(Random rnd, long val) {
		super(rnd);

		this.val = val;
	}

	public long[] roll() {
		return new long[] { val };
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
