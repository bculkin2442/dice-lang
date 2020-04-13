package bjc.dicelang.dicev2;

import bjc.data.GeneratingIterator;

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

/**
 * Utility constructor class for die mods.
 * 
 * @author Ben Culkin
 *
 */
public class DieMods {
	/**
	 * Return a die pool that performs a reduction over a given pool.
	 * 
	 * @param fold
	 *                The fold to use.
	 * @param initial
	 *                The initial value for the fold.
	 * @param dice
	 *                The die pool.
	 * @return A die that performs a reduction over the pool.
	 */
	public Die reduce(LongBinaryOperator fold, long initial, Die... dice) {
		return new ReduceDieMod(fold, initial, dice);
	}

	/**
	 * Create a concatenative die.
	 * 
	 * @param dice
	 *             The input die pool.
	 * @return A die that concatenates the provided pool.
	 */
	public Die concat(Die... dice) {
		return new ConcatDieMod(dice);
	}

	/**
	 * Create a die pool for tracking successes.
	 * 
	 * @param success
	 *                The threshold for success.
	 * @param dice
	 *                The input die pool.
	 * @return A die that counts the successes.
	 */
	public Die counted(LongPredicate success, Die... dice) {
		return new CountDieMod(success, dice);
	}

	/**
	 * Create a die pool for tracking successes/failures.
	 * 
	 * @param success
	 *                The threshold for success.
	 * @param failure
	 *                The threshold for failure.
	 * @param dice
	 *                The input die pool.
	 * @return A die that counts the successes.
	 */
	public Die counted(LongPredicate success, LongPredicate failure, Die... dice) {
		return new CountDieMod(success, failure, dice);
	}

	/**
	 * Create an exploding die pool.
	 * 
	 * @param explode
	 *                The criteria to explode on.
	 * @param dice
	 *                The input die pool.
	 * @return An exploding variant of the die pool.
	 */
	public Die explode(LongPredicate explode, Die... dice) {
		return new ExplodingDieMod(explode, dice);
	}

	/**
	 * Create an exploding die pool.
	 * 
	 * @param explode
	 *                  The criteria to explode on.
	 * @param penetrate
	 *                  Whether the explosions should penetrate.
	 * @param dice
	 *                  The input die pool.
	 * @return An exploding variant of the die pool.
	 */
	public Die explode(LongPredicate explode, boolean penetrate, Die... dice) {
		return new ExplodingDieMod(explode, penetrate, dice);
	}

	/**
	 * Create an compounding die pool.
	 * 
	 * @param compound
	 *                 The criteria to compound on.
	 * @param dice
	 *                 The input die pool.
	 * @return An compounding variant of the die pool.
	 */
	public Die compound(LongPredicate compound, Die... dice) {
		return new CompoundDieMod(compound, dice);
	}

	/**
	 * Create an compounding die pool.
	 * 
	 * @param compound
	 *                  The criteria to compound on.
	 * @param penetrate
	 *                  Whether the compounding should penetrate.
	 * @param dice
	 *                  The input die pool.
	 * @return An compounding variant of the die pool.
	 */
	public Die compound(LongPredicate compound, boolean penetrate, Die... dice) {
		return new CompoundDieMod(compound, penetrate, dice);
	}

	/**
	 * Gather a series of dice together into a pool.
	 * 
	 * @param dice
	 *             The die that will form the pool.
	 * @return A pooled variant of the dice.
	 */
	public Die pool(Die... dice) {
		return new PoolDiceMod(dice);
	}

	/**
	 * Filter out certain dice from the pool.
	 * 
	 * @param filter
	 *               The criteria to filter on.
	 * @param dice
	 *               The input die pool.
	 * @return The die pool with certain dice filtered out.
	 */
	public Die filter(LongPredicate filter, Die... dice) {
		return new FilterDieMod(filter, dice);
	}

	/**
	 * Create a sorted variant of a die pool.
	 * 
	 * @param sorter
	 *               A sorted variant of a die pool.
	 * @param die
	 *               The input die pool.
	 * @return A sorted die pool.
	 */
	public Die sort(Comparator<Long> sorter, Die die) {
		return new SortDieMod(sorter, die);
	}

	/**
	 * Create a die pool that applies a given transform.
	 * 
	 * @param map
	 *            The transform to apply.
	 * @param die
	 *            The input die pool.
	 * @return The die pool with the transform applied.
	 */
	public Die map(LongUnaryOperator map, Die die) {
		return new MapDieMod(map, die);
	}

	/* :SyntheticMod */
	/**
	 * Perform a summation over a die pool.
	 * 
	 * @param dice
	 *             The input die pool.
	 * @return A die that sums the input dice.
	 */
	public Die sum(Die... dice) {
		return reduce((l, r) -> l + r, 0, dice);
	}

	/* :SyntheticMod */
	/**
	 * Perform a subtraction over a die pool.
	 * 
	 * @param dice
	 *             The input die pool.
	 * @return A die that subtracts the input dice.
	 */
	public Die subtract(Die... dice) {
		return reduce((l, r) -> l - r, 0, dice);
	}

	/* :SyntheticMod */
	/**
	 * Perform a multiplication over a die pool.
	 * 
	 * @param dice
	 *             The input die pool.
	 * @return A die that multiplies the input dice.
	 */
	public Die multiply(Die... dice) {
		return reduce((l, r) -> l * r, 1, dice);
	}

	/* :SyntheticMod */
	/**
	 * Perform a division over a die pool.
	 * 
	 * @param dice
	 *             The input die pool.
	 * @return A die that divides the input dice.
	 */
	public Die divide(Die... dice) {
		return reduce((l, r) -> l / r, 1, dice);
	}

	/* :SyntheticMod */
	/**
	 * Create a die pool that sorts the dice in ascending order.
	 * @param die The input die pool. 
	 * @return A die pool that sorts in ascending order.
	 */
	public Die ascending(Die die) {
		return new SortDieMod(Comparator.naturalOrder(), die);
	}

	/* :SyntheticMod */
	/**
	 * Create a die pool that sorts the dice in descending order.
	 * @param die The input die pool. 
	 * @return A die pool that sorts in descending order.
	 */
	public Die descending(Die die) {
		return new SortDieMod((v1, v2) -> {
			return Long.compare(v1, v2);
		}, die);
	}

	/* :SyntheticMod */
	/**
	 * Create a die pool that takes the first n dice.
	 * @param num The number of dice to take.
	 * @param die The input die pool.
	 * @return The die pool that takes the first n dice.
	 */
	public Die take(int num, Die die) {
		GeneratingIterator<Integer> itr = new GeneratingIterator<>(num, (val) -> {
			return val - 1;
		}, (val) -> val == 0);

		return filter((val) -> {
			if (itr.hasNext()) {
				itr.next();
				return true;
			}

			return false;
		}, die);
	}

	/* :SyntheticMod */
	/**
	 * Create a die pool that drops the first n dice.
	 * @param num The number of dice to drops.
	 * @param die The input die pool.
	 * @return The die pool that drops the first n dice.
	 */
	public Die drop(int num, Die die) {
		GeneratingIterator<Integer> itr = new GeneratingIterator<>(num, (val) -> {
			return val - 1;
		}, (val) -> val == 0);

		return filter((val) -> {
			if (itr.hasNext()) {
				itr.next();
				return false;
			}

			return true;
		}, die);
	}

	/* :SyntheticMod */
	/**
	 * Create a die pool that rerolls dice once if they are below a given threshold.
	 * @param reroll The point at which to reload.
	 * @param die The input die pool.
	 * @return A die pool that rerolls certain dice once.
	 */
	public Die rerollOnce(LongPredicate reroll, Die die) {
		return map((val) -> {
			if (reroll.test(val))
				return die.rollSingle();

			return val;
		}, die);
	}

	/* :SyntheticMod */
	/**
	 * Create a die pool that rerolls dice if they are below a given threshold.
	 * @param reroll The point at which to reload.
	 * @param die The input die pool.
	 * @return A die pool that rerolls certain dice.
	 */
	public Die reroll(LongPredicate reroll, Die die) {
		return map((val) -> {
			long nVal = val;

			while (reroll.test(nVal)) {
				nVal = die.rollSingle();
			}

			return nVal;
		}, die);
	}
}
