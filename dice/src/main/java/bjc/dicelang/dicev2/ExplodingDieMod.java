package bjc.dicelang.dicev2;

import bjc.utils.funcutils.ListUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongPredicate;

public class ExplodingDieMod extends Die {
	public final Die[] dice;

	public final LongPredicate explode;

	public final boolean penetrate;

	public ExplodingDieMod(LongPredicate explode, Die... dice) {
		this(explode, false, dice);
	}

	public ExplodingDieMod(LongPredicate explode, boolean penetrate, Die... dice) {
		super();

		this.dice = dice;

		this.explode = explode;

		this.penetrate = penetrate;
	}

	public long[] roll() {
		List<Long> lst = new ArrayList<>(dice.length);

		for(Die die : dice) {
			for(long val : die.roll()) {
				lst.add(val);

				long newVal = val;

				while(explode.test(newVal)) {
					newVal = die.rollSingle();

					if(penetrate) newVal -= 1;

					lst.add(newVal);
				}
			}
		}

		return ListUtils.toPrimitive(lst);
	}

	/*
	 * @NOTE
	 *
	 * :NoSingleRolls
	 *
	 * It makes no sense to roll a 'single' exploding dice, since it
	 * exploding adds another die. Use compounding dice for that case.
	 */
	public long rollSingle() {
		throw new UnsupportedOperationException("Exploding dice can't be rolled singly");	
	}

	/* :UnoptimizableDice */
	public boolean canOptimize() {
		return false;
	}

	public long optimize() {
		throw new UnsupportedOperationException("Exploding dice can't be optimized");
	}
}
