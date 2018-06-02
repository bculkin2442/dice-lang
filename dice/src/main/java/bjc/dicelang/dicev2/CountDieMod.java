package bjc.dicelang.dicev2;

import java.util.function.LongPredicate;

public class CountDieMod extends Die {
	public final Die[] dice;

	public final LongPredicate success;

	public LongPredicate failure;

	public CountDieMod(LongPredicate success, Die... dice) {
		this(success, null, dice);
	}

	public CountDieMod(LongPredicate success, LongPredicate failure, Die... dice) {
		super();

		this.success = success;
		this.failure = failure;

		this.dice    = dice;
	}

	public long[] roll() {
		return new long[] { rollSingle() };
	}

	public long rollSingle() {
		long count = 0;

		for(Die die : dice) {
			for(long val : die.roll()) {
				if(success.test(val)) count += 1;

				if(failure != null && failure.test(val)) count -= 1;
			}
		}

		return count;
	}

	/* :UnoptimizableDice */

	public boolean canOptimize() {
		return false;
	}

	public long optimize() {
		throw new UnsupportedOperationException("Counted dice can't be optimized");
	}
}
