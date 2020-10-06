package bjc.dicelang.dicev2;

import bjc.utils.funcutils.ListUtils;

import java.util.ArrayList;
import java.util.List;

import java.util.function.LongPredicate;

/**
 * A filtered die pool. 
 * @author Ben Culkin
 *
 */
public class FilterDieMod extends Die {
	/**
	 * The dice for this die pool.
	 */
	public final Die[] dice;

	/**
	 * The filter for the die pool.
	 */
	public final LongPredicate filter;

	/**
	 * Create a new filtered die pool.
	 * 
	 * @param filter The filter for the die pool.
	 * @param dice The die pool.
	 */
	public FilterDieMod(LongPredicate filter, Die[] dice) {
		super();

		this.filter = filter;

		this.dice = dice;
	}

	@Override
	public long[] roll() {
		List<Long> lst = new ArrayList<>(dice.length);

		for(Die die : dice) {
			for(long val : die.roll()) {
				if(filter.test(val)) lst.add(val);
			}
		}

		return ListUtils.toPrimitive(lst);
	}

	@Override
	public long rollSingle() {
		throw new UnsupportedOperationException("Filtered dice can't be rolled singly");
	}

	/* :UnoptimizableDice */

	@Override
	public boolean canOptimize() {
		return false;
	}

	@Override
	public long optimize() {
		throw new UnsupportedOperationException("Filtered dice can't be optimized");
	}
}
