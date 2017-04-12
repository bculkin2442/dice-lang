package bjc.dicelang.dice;

import java.util.function.Predicate;

/**
 * Implements a compounding die.
 *
 * This means that the source will be rolled, and then more single rolls will be
 * added while it meets a qualification.
 *
 * @author Ben Culkin
 */
public class CompoundingDie implements Die {
	private final Die source;

	private final Predicate<Long>	compoundOn;
	private final String		compoundPattern;

	/**
	 * Create a new compounding die with no pattern.
	 *
	 * @param src
	 *                The die to compound from
	 * @param compound
	 *                The conditions to compound on
	 */
	public CompoundingDie(final Die src, final Predicate<Long> compound) {
		this(src, compound, null);
	}

	/**
	 * Create a new compounding die with a specified pattern.
	 *
	 * @param src
	 *                The die to compound from
	 * @param compound
	 *                The conditions to compound on
	 * @param patt
	 *                The string pattern the condition came from, for
	 *                printing
	 */
	public CompoundingDie(final Die src, final Predicate<Long> compound, final String patt) {
		source = src;

		compoundOn = compound;
		compoundPattern = patt;
	}

	@Override
	public boolean canOptimize() {
		return source.canOptimize() && source.optimize() == 0;
	}

	@Override
	public long optimize() {
		return 0;
	}

	@Override
	public long roll() {
		long res = source.roll();
		long oldRes = res;

		while (compoundOn.test(oldRes)) {
			oldRes = source.rollSingle();

			res += oldRes;
		}

		return res;
	}

	@Override
	public long rollSingle() {
		/*
		 * Just compound on a single roll
		 */
		long res = source.rollSingle();
		long oldRes = res;

		while (compoundOn.test(oldRes)) {
			oldRes = source.rollSingle();

			res += oldRes;
		}

		return res;
	}

	@Override
	public String toString() {
		if (compoundPattern == null) return source + "!!";

		return source + "!!" + compoundPattern;
	}
}
