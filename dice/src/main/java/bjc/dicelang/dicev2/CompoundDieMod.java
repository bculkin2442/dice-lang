package bjc.dicelang.dicev2;

import bjc.utils.funcutils.ListUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongPredicate;

/**
 * Create a compounding dice.
 * 
 * Compounding dice are rolled more than once, if they pass a given predicate.
 * @author Ben Culkin
 *
 */
public class CompoundDieMod extends Die {
	/**
	 * The pool of dice that make up this compound die mod.
	 */
	public final Die[] dice;

	/**
	 * The compare point to determine when to compound.
	 */
	public final LongPredicate compound;

	/**
	 * Whether or not the compounding should 'penetrate' (subtract 1 before exploding)
	 */
	public final boolean penetrate;

	/**
	 * Create a new compound die.
	 * @param compound The predicate to compound on.
	 * @param dice The pool of dice to roll.
	 */
	public CompoundDieMod(LongPredicate compound, Die... dice) {
		this(compound, false, dice);
	}
	
	/**
	 * Create a new compound die.
	 * @param compound The predicate to compound on.
	 * @param penetrate Whether or not compounding should penetrate.
	 * @param dice The pool of dice to roll.
	 */
	public CompoundDieMod(LongPredicate compound, boolean penetrate, Die... dice) {
		super();

		this.dice = dice;

		this.compound = compound;

		this.penetrate = penetrate;
	}

	@Override
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

	@Override
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
	@Override
	public boolean canOptimize() {
		return false;
	}

	@Override
	public long optimize() {
		throw new UnsupportedOperationException("Compounding dice can't be optimized");
	}
}
