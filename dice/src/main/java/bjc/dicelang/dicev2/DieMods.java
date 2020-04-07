package bjc.dicelang.dicev2;

import bjc.utils.data.GeneratingIterator;

import java.util.Comparator;
import java.util.function.LongPredicate;
import java.util.function.LongBinaryOperator;
import java.util.function.LongUnaryOperator;

/*
 * @NOTE
 *
 * :SyntheticMod
 *
 * These mods are less efficent than if they were hard-coded, involving
 * additional function calls and object allocations. If this ends up causing
 * performance issues, replace these with custom classes.
 */

public class DieMods {
	public Die reduce(LongBinaryOperator fold, long initial, Die... dice) {
		return new ReduceDieMod(fold, initial, dice);
	}

	public Die concat(Die... dice) {
		return new ConcatDieMod(dice);
	}

	public Die counted(LongPredicate success, Die... dice) {
		return new CountDieMod(success, dice);
	}

	public Die counted(LongPredicate success, LongPredicate failure, Die... dice) {
		return new CountDieMod(success, failure, dice);
	}

	public Die explode(LongPredicate explode, Die... dice) {
		return new ExplodingDieMod(explode, dice);
	}

	public Die explode(LongPredicate explode, boolean penetrate, Die... dice) {
		return new ExplodingDieMod(explode, penetrate, dice);
	}

	public Die compound(LongPredicate compound, Die... dice) {
		return new CompoundDieMod(compound, dice);
	}

	public Die compound(LongPredicate compound, boolean penetrate, Die... dice) {
		return new CompoundDieMod(compound, penetrate, dice);
	}

	public Die pool(Die... dice) {
		return new PoolDiceMod(dice);
	}

	public Die filter(LongPredicate filter, Die... dice) {
		return new FilterDieMod(filter, dice);
	}

	public Die sort(Comparator<Long> sorter, Die die) {
		return new SortDieMod(sorter, die);
	}

	public Die map(LongUnaryOperator map, Die die) {
		return new MapDieMod(map, die);
	}

	/* :SyntheticMod */
	public Die sum(Die... dice) {
		return reduce((l, r) -> l + r, 0, dice);
	}

	/* :SyntheticMod */
	public Die subtract(Die... dice) {
		return reduce((l, r) -> l - r, 0, dice);
	}

	/* :SyntheticMod */
	public Die multiply(Die... dice) {
		return reduce((l, r) -> l * r, 1, dice);
	}

	/* :SyntheticMod */
	public Die divide(Die... dice) {
		return reduce((l, r) -> l / r, 1, dice);
	}

	/* :SyntheticMod */
	public Die ascending(Die die) {
		return new SortDieMod(Comparator.naturalOrder(), die);
	}

	/* :SyntheticMod */
	public Die descending(Die die) {
		return new SortDieMod((v1, v2) -> {
			return Long.compare(v1, v2);
		}, die);
	}

	/* :SyntheticMod */
	public Die take(int num, Die die) {
		GeneratingIterator<Integer> itr = new GeneratingIterator<>(num, (val) -> {
			return val - 1;
		}, (val) -> val == 0); 

		return filter((val) -> {
			if(itr.hasNext()) {
				itr.next();
				return true;
			}

			return false;
		}, die);
	}

	/* :SyntheticMod */
	public Die drop(int num, Die die) {
		GeneratingIterator<Integer> itr = new GeneratingIterator<>(num, (val) -> {
			return val - 1;
		}, (val) -> val == 0); 

		return filter((val) -> {
			if(itr.hasNext()) {
				itr.next();
				return false;
			}

			return true;
		}, die);
	}

	/* :SyntheticMod */
	public Die rerollOnce(LongPredicate reroll, Die die) {
		return map((val) -> {
			if(reroll.test(val)) return die.rollSingle();

			return val;
		}, die);
	}

	/* :SyntheticMod */
	public Die reroll(LongPredicate reroll, Die die) {
		return map((val) -> {
			long nVal = val;

			while(reroll.test(nVal)) {
				nVal = die.rollSingle();
			}

			return nVal;
		}, die);
	}
}
