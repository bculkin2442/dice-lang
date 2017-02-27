package bjc.dicelang.v2.dice;

import java.util.function.Predicate;

public class CompoundingDie implements Die {
	private Die source;

	private Predicate<Long> compoundOn;
	private String          compoundPattern;

	public CompoundingDie(Die src, Predicate<Long> compound) {
		this(src, compound, null);
	}

	public CompoundingDie(Die src, Predicate<Long> compound, String patt) {
		source = src;

		compoundOn      = compound;
		compoundPattern = patt;
	}

	public boolean canOptimize() {
		return source.canOptimize() && source.optimize() == 0;
	}

	public long optimize() {
		return 0;
	}

	public long roll() {
		long res = source.roll();
		long oldRes = res;

		while(compoundOn.test(oldRes)) {
			oldRes = source.rollSingle();

			res += oldRes;
		}

		return res;
	}

	public long rollSingle() {
		long res = source.rollSingle();
		long oldRes = res;

		while(compoundOn.test(oldRes)) {
			oldRes = source.rollSingle();

			res += oldRes;
		}

		return res;
	}

	public String toString() {
		if(compoundPattern == null) {
			return source + "!!";
		} else {
			return source + "!!" + compoundPattern;
		}
	}
}