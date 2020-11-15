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