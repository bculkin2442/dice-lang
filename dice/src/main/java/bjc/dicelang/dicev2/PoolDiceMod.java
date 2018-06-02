package bjc.dicelang.dicev2;

import bjc.utils.funcutils.ListUtils;

import java.util.ArrayList;
import java.util.List;

public class PoolDiceMod extends Die {
	public Die[] dice;

	public PoolDiceMod(Die... dice) {
		super();

		this.dice = dice;
	}

	public long[] roll() {
		List<Long> lst = new ArrayList<>(dice.length);

		for(Die die : dice) {
			for(long val : die.roll()) {
				lst.add(val);
			}
		}

		return ListUtils.toPrimitive(lst);
	}

	/* :NoSingleRolls */
	public long rollSingle() {
		throw new UnsupportedOperationException("Pooled dice can't be rolled singly");
	}

	/* :UnoptimizableDice */
	public boolean canOptimize() {
		return false;
	}

	public long optimize() {
		throw new UnsupportedOperationException("Pooled dice can't be optimized");
	}
}
