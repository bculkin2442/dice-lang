package bjc.dicelang.dice;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

/**
 * An exploding die.
 *
 * Represents a die list that keeps getting another added die as long as a
 * condition is met.
 *
 * @author Ben Culkin
 */
public class ExplodingDice implements DieList {
	/*
	 * The source die to use.
	 */
	private Die source;

	/*
	 * The conditions for exploding.
	 */
	private Predicate<Long>	explodeOn;
	private String		explodePattern;
	private boolean		explodePenetrates;

	/**
	 * Create a new exploding die.
	 *
	 * @param src
	 *                The source die for exploding.
	 * @param explode
	 *                The condition to explode on.
	 */
	public ExplodingDice(Die src, Predicate<Long> explode) {
		this(src, explode, null, false);
	}

	/**
	 * Create a new exploding die that may penetrate.
	 *
	 * @param src
	 *                The source die for exploding.
	 * @param explode
	 *                The condition to explode on.
	 * @param penetrate
	 *                Whether or not for explosions to penetrate (-1 to
	 *                exploded die).
	 */
	public ExplodingDice(Die src, Predicate<Long> explode, boolean penetrate) {
		this(src, explode, null, penetrate);
	}

	/**
	 * Create a new exploding die that may penetrate.
	 *
	 * @param src
	 *                The source die for exploding.
	 * @param explode
	 *                The condition to explode on.
	 * @param penetrate
	 *                Whether or not for explosions to penetrate (-1 to
	 *                exploded die).
	 * @param patt
	 *                The string the condition came from, for printing.
	 */
	public ExplodingDice(Die src, Predicate<Long> explode, String patt, boolean penetrate) {
		source = src;
		explodeOn = explode;
		explodePattern = patt;
		explodePenetrates = penetrate;
	}

	@Override
	public boolean canOptimize() {
		return false;
	}

	@Override
	public long[] optimize() {
		return new long[0];
	}

	@Override
	public long[] roll() {
		long res = source.roll();
		long oldRes = res;

		List<Long> resList = new LinkedList<>();

		while(explodeOn.test(oldRes)) {
			oldRes = source.rollSingle();

			if(explodePenetrates) {
				oldRes -= 1;
			}
			resList.add(oldRes);
		}

		long[] newRes = new long[resList.size() + 1];
		newRes[0] = res;

		int i = 1;
		for(long rll : resList) {
			newRes[i] = rll;
			i += 1;
		}

		return newRes;
	}

	@Override
	public String toString() {
		if(explodePattern == null)
			return source + (explodePenetrates ? "p" : "") + "!";
		else
			return source + (explodePenetrates ? "p" : "") + "!" + explodePattern;
	}
}
