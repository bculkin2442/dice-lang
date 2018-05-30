package bjc.dicelang.dicev2;

public class SumDieMod extends Die {
	public final Die[] dice;

	public SumDieMod(Die... dice) {
		super();

		this.dice = dice;
	}

	public long[] roll() {
		return new long[] { rollSingle() };
	}

	public long rollSingle() {
		long res = 0;

		for(Die die : dice) {
			for(long val : die.roll()) {
				res += val;
			}
		}

		return res;
	}

	public boolean canOptimize() {
		for(Die die : dice) {
			if(!die.canOptimize()) return false;
		}

		return true;
	}

	public long optimize() {
		long res = 0;

		for(Die die : dice) {
			res += die.optimize();
		}

		return res;
	}
}
