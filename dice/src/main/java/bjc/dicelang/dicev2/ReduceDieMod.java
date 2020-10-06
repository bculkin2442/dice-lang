package bjc.dicelang.dicev2;

import java.util.function.LongBinaryOperator;

/**
 * Die pool which performs a reduction.
 * 
 * @author Ben Culkin
 *
 */
public class ReduceDieMod extends Die {
	/**
	 * The die pool.
	 */
	public final Die[] dice;

	/**
	 * The reduction operation.
	 */
	public final LongBinaryOperator fold;
	
	/**
	 * The initial value for the reduction.
	 */
	public final long initial;

	/**
	 * Create a new reducing die pool.
	 * 
	 * @param fold The reduction operation.
	 * @param initial The initial value for the reduction.
	 * @param dice The die pool.
	 */
	public ReduceDieMod(LongBinaryOperator fold, long initial, Die... dice) {
		super();

		this.dice = dice;

		this.fold    = fold;
		this.initial = initial;
	}

	@Override
	public long[] roll() {
		return new long[] { rollSingle() };
	}

	@Override
	public long rollSingle() {
		long res = initial;

		for(Die die : dice) {
			for(long val : die.roll()) {
				res = fold.applyAsLong(res, val);
			}
		}

		return res;
	}

	@Override
	public boolean canOptimize() {
		for(Die die : dice) {
			if(!die.canOptimize()) return false;
		}

		return true;
	}

	@Override
	public long optimize() {
		long res = 0;

		for(Die die : dice) {
			res = fold.applyAsLong(res, die.optimize());
		}

		return res;
	}
}
