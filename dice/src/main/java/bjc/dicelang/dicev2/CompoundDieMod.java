package bjc.dicelang.dicev2;

import bjc.utils.funcutils.ListUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongPredicate;

public class CompoundDieMod extends Die {
	public final Die[] dice;

	public final LongPredicate compound;

	public final boolean penetrate;

	public CompoundDieMod(LongPredicate compound, Die... dice) {
		this(compound, false, dice);
	}

	public CompoundDieMod(LongPredicate compound, boolean penetrate, Die... dice) {
		super();

		this.dice = dice;

		this.compound = compound;

		this.penetrate = penetrate;
	}

	public long[] roll() {
		List<Long> lst = new ArrayList<>(5);

		for(Die die : dice) {
			for(long val : die.roll()) {
				long res = val;
				
				long newVal = die.rollSingle();

				while(compound.test(newVal)) {
					if(penetrate) newVal -= 1;

					res += newVal;

					newVal = die.rollSingle();
				}

				lst.add(res);
			}
		}

		return ListUtils.toPrimitive(lst);
	}

	public long rollSingle() {
		Die die = dice[0];

		long newVal = die.rollSingle();

		long res = newVal;

		while(compound.test(newVal)) {
			newVal = die.rollSingle();

			if(penetrate) newVal -= 1;

			res += newVal;
		}

		return res;
	}

	/* :UnoptimizableDice */
	public boolean canOptimize() {
		return false;
	}

	public long optimize() {
		throw new UnsupportedOperationException("Exploding dice can't be optimized");
	}
}
