package bjc.dicelang.dicev2;

import bjc.utils.funcutils.ListUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongPredicate;

/**
 * An exploding die pool.
 * @author Ben Culkin
 *
 */
public class ExplodingDieMod extends Die {
	/**
	 * The pool of dice.
	 */
	public final Die[] dice;

	/**
	 * The predicate which says when to explode.
	 */
	public final LongPredicate explode;

	/**
	 * Should the explosion penetrate.
	 */
	public final boolean penetrate;

	/**
	 * Create an exploding die pool.
	 * @param explode The predicate for determining an explosion.
	 * @param dice The die pool.
	 */
	public ExplodingDieMod(LongPredicate explode, Die... dice) {
		this(explode, false, dice);
	}

	/**
	 * Create an exploding die pool.
	 * @param explode The predicate for determining an explosion.
	 * @param penetrate Whether the explosion should penetrate.
	 * @param dice The die pool.
	 */
	public ExplodingDieMod(LongPredicate explode, boolean penetrate, Die... dice) {
		super();

		this.dice = dice;

		this.explode = explode;

		this.penetrate = penetrate;
	}

	@Override
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
	@Override
	public long rollSingle() {
		throw new UnsupportedOperationException("Exploding dice can't be rolled singly");	
	}

	/* :UnoptimizableDice */
	@Override
	public boolean canOptimize() {
		return false;
	}

	@Override
	public long optimize() {
		throw new UnsupportedOperationException("Exploding dice can't be optimized");
	}
}
