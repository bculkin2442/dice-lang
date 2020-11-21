package bjc.dicelang.neodice;

import java.util.*;
import java.util.function.*;

import bjc.dicelang.neodice.diepool.*;

/**
 * Represents a pool of dice.
 * 
 * @author Ben Culkin
 *
 */
@FunctionalInterface
public interface DiePool {
	/**
	 * Roll each die in the pool, and return the results.
	 * 
	 * Note that this array is not guaranteed to be the same size every time it
	 * is rolled, because there are some pool types that could add/remove dice.
	 * 
	 * @param rng The source for random numbers
	 * 
	 * @return The result of rolling each die in the pool.
	 */
	public int[] roll(Random rng);
	
	/**
	 * Gets the dice contained in this pool.
	 * 
	 * Note that the length of this array may not be the same as the length of
	 * the array returned by roll, because certain pool types may add additional
	 * dice.
	 * 
	 * Also note that this array (and the Die instances contained in it) should
	 * not be modified. That may work for certain pool types, but it isn't
	 * guaranteed to work, and can lead to unintuitive behavior. For instances,
	 * certain pool types may return an array where multiple elements of it refer
	 * to the same Die instance.
	 * 
	 * The default implementation throws an UnsupportedOperationException.
	 * 
	 * @return The dice contained in this pool.
	 * 
	 * @throws UnsupportedOperationException If the composite dice can't be retrieved.
	 */
	default Die[] contained() {
		throw new UnsupportedOperationException("Can't get composite dice");
	}
	
	/*
	 * These die pool operations transform this pool in some way.
	 */
	
	/**
	 * Returns a version of this die pool which returns its results in sorted
	 * order.
	 * 
	 * At the moment, sorting in descending order is somewhat less efficent than
	 * sorting in ascending order, because Java doesn't provide a built-in
	 * descending sort for primitive arrays.
	 * 
	 * @param isDescending True to sort in descending order, false to sort in ascending order.
	 * 
	 * @return The die pool, which returns its results in sorted order.
	 */
	default DiePool sorted(boolean isDescending) {
		return new SortedDiePool(this, isDescending);
	}
	
	/**
	 * Return a die pool which rolls this one, then filters out any results that
	 * don't match the provided predicate.
	 * 
	 * @param matcher The predicate that determines 
	 * 
	 * @return A die pool which contains only entries that pass the predicate.
	 */
	default DiePool filtered(IntPredicate matcher) {
		return new FilteredDiePool(this, matcher);
	}
	
	/**
	 * Return a die pool which rolls this one, then drops a number of the first values.
	 * 
	 * @param number The number of first values to drop.
	 * 
	 * @return A die pool which has the first entries dropped.
	 */
	default DiePool dropFirst(int number) {
		return new DropFirstPool(this, number);
	}

	/**
	 * Return a die pool which rolls this one, then drops a number of the last values.
	 * 
	 * @param number The number of last values to drop.
	 * 
	 * @return A die pool which has the last entries dropped.
	 */
	default DiePool dropLast(int number) {
		return new DropLastPool(this, number);
	}

	/**
	 * Return a die pool which rolls this one, then keeps a number of the first values.
	 * 
	 * @param number The number of first values to keep.
	 * 
	 * @return A die pool which has the first entries kept.
	 */
	default DiePool keepFirst(int number) {
		return new KeepFirstDiePool(this, number);
	}
	
	/**
	 * Return a die pool which rolls this one, then keeps a number of the last values.
	 * 
	 * @param number The number of last values to keep.
	 * 
	 * @return A die pool which has the last entries kept.
	 */
	default DiePool keepLast(int number) {
		return new KeepLastDiePool(this, number);
	}
	
	/* 
	 * These die-pool operations are formed exclusively through other die pool
	 * operations.
	 */
	
	/**
	 * Return a die pool which rolls this one, then drops a number of the lowest values.
	 * 
	 * @param number The number of lowest values to drop.
	 * 
	 * @return A die pool which has the lowest entries dropped.
	 */
	default DiePool dropLowest(int number) {
		return this.sorted(false).dropFirst(number);
	}
	
	/**
	 * Return a die pool which rolls this one, then drops a number of the lowest values.
	 * 
	 * @param number The number of lowest values to drop.
	 * 
	 * @return A die pool which has the lowest entries dropped.
	 */
	default DiePool dropHighest(int number) {
		return this.sorted(false).dropLast(number);
	}
	
	/**
	 * Return a die pool which rolls this one, then keeps a number of the lowest values.
	 * 
	 * @param number The number of lowest values to keep.
	 * 
	 * @return A die pool which has the lowest entries kept.
	 */
	default DiePool keepLowest(int number) {
		return this.sorted(false).keepFirst(number);
	}
	
	/**
	 * Return a die pool which rolls this one, then keeps a number of the highest values.
	 * 
	 * @param number The number of highest values to keep.
	 * 
	 * @return A die pool which has the highest entries kept.
	 */
	default DiePool keepHighest(int number) {
		return this.sorted(false).keepLast(number);
	}
	
	/* These are misc. operations that don't form new dice pools. */
	
	/**
	 * Get an iterator which iterates over a single roll of this die pool.
	 * 
	 * @param rng The source of random numbers.
	 * 
	 * @return An iterator over a single roll of this die pool.
	 */
	default Iterator<Integer> iterator(Random rng) {
		return Arrays.stream(this.roll(rng)).iterator();
	}

	/**
	 * Create a die pool containing the provided dice.
	 * 
	 * @param dice The dice to put into the pool.
	 * 
	 * @return A pool which contains the provided dice.
	 */
	static DiePool containing(Die... dice) {
		return new FixedDiePool(dice);
	}
}