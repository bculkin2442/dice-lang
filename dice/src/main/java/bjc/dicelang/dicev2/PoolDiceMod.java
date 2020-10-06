package bjc.dicelang.dicev2;

import bjc.utils.funcutils.ListUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts a die pool into a single die.
 * 
 * @TODO Oct 5, 2020 - Ben Culkin - :CustomPool
 * Should there be a custom pool reduction operator?
 * 
 * @author Ben Culkin
 *
 */
public class PoolDiceMod extends Die {
	/**
	 * The die pool to roll.
	 */
	public Die[] dice;

	/**
	 * Create a new die pool converter.
	 * 
	 * @param dice The pool of dice.
	 */
	public PoolDiceMod(Die... dice) {
		super();

		this.dice = dice;
	}

	@Override
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
	@Override
	public long rollSingle() {
		throw new UnsupportedOperationException("Pooled dice can't be rolled singly");
	}

	/* :UnoptimizableDice */
	@Override
	public boolean canOptimize() {
		return false;
	}

	@Override
	public long optimize() {
		throw new UnsupportedOperationException("Pooled dice can't be optimized");
	}
}
