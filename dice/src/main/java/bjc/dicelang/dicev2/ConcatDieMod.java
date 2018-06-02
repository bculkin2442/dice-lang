package bjc.dicelang.dicev2;

public class ConcatDieMod extends Die {
	public final Die[] dice;

	public ConcatDieMod(Die... dice) {
		super();

		this.dice = dice;
	}

	public long[] roll() {
		return new long[] { rollSingle() };
	}

	public long rollSingle() {
		StringBuilder sb = new StringBuilder();

		for(Die die : dice) {
			for(long val : die.roll()) {
				sb.append(val);
			}
		}

		return Long.parseLong(sb.toString());
	}

	public boolean canOptimize() {
		for(Die die : dice) {
			if(!die.canOptimize()) return false;
		}

		return true;
	}
	
	public long optimize() {
		StringBuilder sb = new StringBuilder();

		for(Die die : dice) {
			sb.append(die.optimize());
		}

		return Long.parseLong(sb.toString());
	}
}
