package bjc.dicelang.dicev2;

import java.util.function.LongBinaryOperator;

public class ReduceDieMod extends Die {
	public final Die[] dice;

	public final LongBinaryOperator fold;
	public final long initial;

	public ReduceDieMod(LongBinaryOperator fold, long initial, Die... dice) {
		super();

		this.dice = dice;

		this.fold    = fold;
		this.initial = initial;
	}

	public long[] roll() {
		return new long[] { rollSingle() };
	}

	public long rollSingle() {
		long res = initial;

		for(Die die : dice) {
			for(long val : die.roll()) {
				res = fold.applyAsLong(res, val);
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
			res = fold.applyAsLong(res, die.optimize());
		}

		return res;
	}
}
