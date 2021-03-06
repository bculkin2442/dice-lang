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
	/* The source die to use. */
	private final Die source;

	/* The conditions for exploding. */
	private final Predicate<Long> explodeOn;
	private final String explodePattern;
	/* Whether or not to apply a -1 penalty to explosions. */
	private final boolean explodePenetrates;

	/**
	 * Create a new exploding die.
	 *
	 * @param src
	 *        The source die for exploding.
	 * @param explode
	 *        The condition to explode on.
	 */
	public ExplodingDice(final Die src, final Predicate<Long> explode) {
		this(src, explode, null, false);
	}

	/**
	 * Create a new exploding die that may penetrate.
	 *
	 * @param src
	 *        The source die for exploding.
	 * @param explode
	 *        The condition to explode on.
	 * @param penetrate
	 *        Whether or not for explosions to penetrate (-1 to exploded
	 *        die).
	 */
	public ExplodingDice(final Die src, final Predicate<Long> explode, final boolean penetrate) {
		this(src, explode, null, penetrate);
	}

	/**
	 * Create a new exploding die that may penetrate.
	 *
	 * @param src
	 *        The source die for exploding.
	 * @param explode
	 *        The condition to explode on.
	 * @param penetrate
	 *        Whether or not for explosions to penetrate (-1 to exploded
	 *        die).
	 * @param patt
	 *        The string the condition came from, for printing.
	 */
	public ExplodingDice(final Die src, final Predicate<Long> explode, final String patt, final boolean penetrate) {
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
		final long res = source.roll();
		long oldRes = res;

		final List<Long> resList = new LinkedList<>();
		resList.add(res);

		while(explodeOn.test(oldRes)) {
			oldRes = source.rollSingle();

			if(explodePenetrates) {
				oldRes -= 1;
			}

			resList.add(oldRes);
		}

		final long resArr[] = new long[resList.size()];

		int i = 0;
		for(long rll : resList) {
			resArr[i] = rll;
			i += 1;
		}

		return resArr;
	}

	@Override
	public String toString() {
		String penString = explodePenetrates ? "p" : "";
		String sourceString = source.toString();

		if(explodePattern == null) {
			return String.format("%s%s!<complex-pred>", sourceString, penString);
		}

		return String.format("%s%s!%s", sourceString, penString, explodePattern);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((explodePattern == null) ? 0 : explodePattern.hashCode());
		result = prime * result + (explodePenetrates ? 1231 : 1237);
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		ExplodingDice other = (ExplodingDice) obj;
		if(explodePattern == null) {
			if(other.explodePattern != null) return false;
		} else if(!explodePattern.equals(other.explodePattern)) return false;
		if(explodePenetrates != other.explodePenetrates) return false;
		if(source == null) {
			if(other.source != null) return false;
		} else if(!source.equals(other.source)) return false;
		return true;
	}
}
