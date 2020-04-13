package bjc.dicelang.dicev2;

import java.util.function.LongPredicate;

/**
 * Create a die pool that will count successes/failures.
 * @author Ben Culkin
 *
 */
public class CountDieMod extends Die {
	/**
	 * The pool of dice that will be rolled.
	 */
	public final Die[] dice;

	/**
	 * The predicate for counting successes.
	 */
	public final LongPredicate success;

	/**
	 * The predicate for counting failures.
	 */
	public LongPredicate failure;

	/**
	 * Create a new counted die mod with a specified success criteria.
	 * 
	 * @param success The predicate for determining a success.
	 * @param dice The pool of dice to roll.
	 */
	public CountDieMod(LongPredicate success, Die... dice) {
		this(success, null, dice);
	}

	/**
	 * Create a new counted die mod with a specified success criteria.
	 * 
	 * @param success The predicate for determining a success.
	 * @param failure The predicate for determining a failure.
	 * @param dice The pool of dice to roll.
	 */
	public CountDieMod(LongPredicate success, LongPredicate failure, Die... dice) {
		super();

		this.success = success;
		this.failure = failure;

		this.dice    = dice;
	}

	@Override
	public long[] roll() {
		return new long[] { rollSingle() };
	}

	@Override
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

	@Override
	public boolean canOptimize() {
		return false;
	}

	@Override
	public long optimize() {
		throw new UnsupportedOperationException("Counted dice can't be optimized");
	}
}
