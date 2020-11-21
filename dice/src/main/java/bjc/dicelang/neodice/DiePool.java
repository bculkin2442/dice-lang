package bjc.dicelang.neodice;

import java.util.*;
import java.util.function.*;

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
}

final class SortedDiePool implements DiePool {
	private final boolean isDescending;
	private final DiePool pool;

	public SortedDiePool(DiePool pool, boolean isDescending) {
		this.pool = pool;
		this.isDescending = isDescending;
	}

	@Override
	public int[] roll(Random rng) {
		int[] rolls = pool.roll(rng);
		
		Arrays.sort(rolls);
	
		if (isDescending) {
			int[] newRolls = new int[rolls.length];
			
			int newIndex = newRolls.length;
			for (int index = 0; index < rolls.length; index++) {
				newRolls[newIndex--] = rolls[index];
			}
			
			return newRolls;
		} else {
			return rolls;
		}
	}
	
	@Override
	public Die[] contained() {
		return pool.contained();
	}

	@Override
	public String toString() {
		return String.format("%s (sorted %s)", pool, 
				isDescending ? " descending" : "ascending");
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(isDescending, pool);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)                  return true;
		if (obj == null)                  return false;
		if (getClass() != obj.getClass()) return false;
		
		SortedDiePool other = (SortedDiePool) obj;
	
		return isDescending == other.isDescending
			   && Objects.equals(pool, other.pool);
	}
}

final class FilteredDiePool implements DiePool {
	private final DiePool pool;
	private final IntPredicate filter;
	
	public FilteredDiePool(DiePool pool, IntPredicate filter) {
		this.pool = pool;
		this.filter = filter;
	}
	
	@Override
	public int[] roll(Random rng) {
		int[] rolls = pool.roll(rng);
		
		return Arrays.stream(rolls).filter(filter).toArray();
	}

	@Override
	public Die[] contained() {
		return pool.contained();
	}
	
	// No toString, since there isn't any sensible to output the filter
	
	@Override
	public int hashCode() {
		return Objects.hash(filter, pool);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)                  return true;
		if (obj == null)                  return false;
		if (getClass() != obj.getClass()) return false;
		
		FilteredDiePool other = (FilteredDiePool) obj;
		
		return Objects.equals(filter, other.filter) 
			   && Objects.equals(pool, other.pool);
	}
}

final class DropFirstPool implements DiePool {
	private final int     number;
	private final DiePool pool;
	
	public DropFirstPool(DiePool pool, int number) {
		this.pool   = pool;
		this.number = number;
	}

	@Override
	public int[] roll(Random rng) {
		int[] rolls = pool.roll(rng);
		
		if (number >= rolls.length) {
			return new int[0];
		} else {
			int[] newRolls = new int[rolls.length - number];
			
			for (int index = number - 1; index < rolls.length; index++) {
				newRolls[index - number] = rolls[index];
			}
			
			return newRolls;
		}
	}
	
	@Override
	public Die[] contained() {
		return pool.contained();
	}

	@Override
	public String toString() {
		return String.format("%sdF%d", pool, number);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(number, pool);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)                  return true;
		if (obj == null)                  return false;
		if (getClass() != obj.getClass()) return false;
		
		DropFirstPool other = (DropFirstPool) obj;
	
		return number == other.number && Objects.equals(pool, other.pool);
	}
}

final class DropLastPool implements DiePool {
	private final int     number;
	private final DiePool pool;
	
	public DropLastPool(DiePool pool, int number) {
		this.pool   = pool;
		this.number = number;
	}

	@Override
	public int[] roll(Random rng) {
		int[] rolls = pool.roll(rng);
		
		if (number >= rolls.length) {
			return new int[0];
		} else {
			int[] newRolls = new int[rolls.length - number];
			
			for (int index = 0; index < rolls.length - number; index++) {
				newRolls[index] = rolls[index];
			}
			
			return newRolls;
		}
	}
	
	@Override
	public Die[] contained() {
		return pool.contained();
	}

	@Override
	public String toString() {
		return String.format("%sdL%d", pool, number);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(number, pool);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)                  return true;
		if (obj == null)                  return false;
		if (getClass() != obj.getClass()) return false;
		
		DropLastPool other = (DropLastPool) obj;
	
		return number == other.number && Objects.equals(pool, other.pool);
	}
}

final class KeepFirstDiePool implements DiePool {
	private final int     number;
	private final DiePool pool;
	
	public KeepFirstDiePool(DiePool pool, int number) {
		this.pool   = pool;
		this.number = number;
	}

	@Override
	public int[] roll(Random rng) {
		int[] rolls = pool.roll(rng);
		
		if (rolls.length >= number) {
			return rolls;
		} else {
			int[] newRolls = new int[number];
			
			for (int index = 0; index < number; index++) {
				newRolls[index] = rolls[index];
			}
			
			return newRolls;
		}
	}
	
	@Override
	public Die[] contained() {
		return pool.contained();
	}
	
	@Override
	public String toString() {
		return String.format("%skF%d", pool, number);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(number, pool);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)                  return true;
		if (obj == null)                  return false;
		if (getClass() != obj.getClass()) return false;
		
		KeepFirstDiePool other = (KeepFirstDiePool) obj;
		
		return number == other.number && Objects.equals(pool, other.pool);
	}
}

final class KeepLastDiePool implements DiePool {
	private final int     number;
	private final DiePool pool;
	
	public KeepLastDiePool(DiePool pool, int number) {
		this.pool   = pool;
		this.number = number;
	}

	@Override
	public int[] roll(Random rng) {
		int[] rolls = pool.roll(rng);
		
		if (rolls.length >= number) {
			return rolls;
		} else {
			int[] newRolls = new int[number];
			
			for (int index = number; index > index; index--) {
				newRolls[index] = rolls[rolls.length - index];
			}
			
			return newRolls;
		}
	}
	
	@Override
	public Die[] contained() {
		return pool.contained();
	}
	
	@Override
	public String toString() {
		return String.format("%skL%d", pool, number);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(number, pool);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)                  return true;
		if (obj == null)                  return false;
		if (getClass() != obj.getClass()) return false;
		
		KeepLastDiePool other = (KeepLastDiePool) obj;
		
		return number == other.number && Objects.equals(pool, other.pool);
	}
}