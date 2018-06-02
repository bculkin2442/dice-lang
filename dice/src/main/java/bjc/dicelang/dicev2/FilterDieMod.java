package bjc.dicelang.dicev2;

import bjc.utils.funcutils.ListUtils;

import java.util.ArrayList;
import java.util.List;

import java.util.function.LongPredicate;

public class FilterDieMod extends Die {
	public final Die[] dice;

	public final LongPredicate filter;

	public FilterDieMod(LongPredicate filter, Die[] dice) {
		super();

		this.filter = filter;

		this.dice = dice;
	}

	public long[] roll() {
		List<Long> lst = new ArrayList<>(dice.length);

		for(Die die : dice) {
			for(long val : die.roll()) {
				if(filter.test(val)) lst.add(val);
			}
		}

		return ListUtils.toPrimitive(lst);
	}

	public long rollSingle() {
		throw new UnsupportedOperationException("Filtered dice can't be rolled singly");
	}

	/* :UnoptimizableDice */

	public boolean canOptimize() {
		return false;
	}

	public long optimize() {
		throw new UnsupportedOperationException("Filtered dice can't be optimized");
	}
}
