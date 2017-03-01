package bjc.dicelang.dice;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class ExplodingDice implements DieList {
	private Die             source;

	private Predicate<Long> explodeOn;
	private String          explodePattern;
	private boolean         explodePenetrates;

	public ExplodingDice(Die src, Predicate<Long> explode) {
		this(src, explode, null, false);
	}

	public ExplodingDice(Die src, Predicate<Long> explode, boolean penetrate) {
		this(src, explode, null, penetrate);
	}

	public ExplodingDice(Die src, Predicate<Long> explode, String patt,
			boolean penetrate) {
		source            = src;
		explodeOn         = explode;
		explodePattern    = patt;
		explodePenetrates = penetrate;
	}

	public boolean canOptimize() {
		return false;
	}

	public long[] optimize() {
		return new long[0];
	}

	public long[] roll() {
		long res = source.roll();
		long oldRes = res;

		List<Long> resList = new LinkedList<>();

		while(explodeOn.test(oldRes)) {
			oldRes = source.rollSingle();

			if(explodePenetrates) oldRes -= 1;
			resList.add(oldRes);
		}

		long[] newRes = new long[resList.size() + 1];
		newRes[0] = res;

		int i = 1;
		for(long rll : resList) {
			newRes[i] = rll;
			i         += 1;
		}

		return newRes;
	}

	public String toString() {
		if(explodePattern == null) {
			return source + (explodePenetrates ? "p" : "") + "!";
		} else {
			return source + (explodePenetrates ? "p" : "") + "!" + explodePattern;
		}
	}
}