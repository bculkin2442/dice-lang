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
	/* The source die to compound. */
	private final Die source;

	/* The predicate that marks when to compound. */
	private final Predicate<Long>   compoundOn;
	/* The string version of the predicate, if one exists. */
	private final String            compoundPattern;

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
		if (source.canOptimize()) {
			/* We can only be optimized for a result of zero. */
			return source.optimize() == 0;
		}

		return false;
	}

	@Override
	public long optimize() {
		/* If we can be optimized, its to zero. */
		return 0;
	}

	@Override
	public long roll() {
		/* The current result. */
		long res = source.roll();
		/* The last result. */
		long oldRes = res;

		while (compoundOn.test(oldRes)) {
			/* Compound while the result should be compounded. */
			oldRes = source.rollSingle();

			/* Accumulate. */
			res += oldRes;
		}

		return res;
	}

	@Override
	public long rollSingle() {
		/* Just compound on an initial single role. */
		long res = source.rollSingle();
		/* The last result. */
		long oldRes = res;

		while (compoundOn.test(oldRes)) {
			/* Compound while the result should be compounded. */
			oldRes = source.rollSingle();

			/* Accumulate. */
			res += oldRes;
		}

		return res;
	}

	@Override
	public String toString() {
		String sourceString = source.toString();

		/* Can't print a parseable version. */
		if (compoundPattern == null) {
			return String.format("%s!!<complex-pattern>", sourceString);
		}

		return String.format("%s!!%s", sourceString, compoundPattern);
	}
}
