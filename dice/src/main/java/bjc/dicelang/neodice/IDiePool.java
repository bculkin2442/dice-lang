package bjc.dicelang.neodice;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import bjc.dicelang.neodice.diepool.*;

/**
 * Represents a pool of dice.
 * 
 * @author Ben Culkin
 *
 */
@FunctionalInterface
public interface IDiePool<SideType> {
	/**
	 * Roll each die in the pool, and return the results.
	 * 
	 * Note that this list is not guaranteed to be the same size every time it
	 * is rolled, because there are some pool types that could add/remove dice.
	 * 
	 * @param rng The source for random numbers
	 * 
	 * @return The result of rolling each die in the pool.
	 */
	public Stream<SideType> roll(Random rng);
	
	/**
	 * Gets the dice contained in this pool.
	 * 
	 * Note that the length of this list may not be the same as the length of
	 * the list returned by roll, because certain pool types may add additional
	 * dice.
	 * 
	 * Also note that this list (and the Die instances contained in it) should
	 * not be modified. That may work for certain pool types, but it isn't
	 * guaranteed to work, and can lead to unintuitive behavior. For instances,
	 * certain pool types may return an list where multiple elements of it refer
	 * to the same Die instance.
	 * 
	 * The default implementation throws an UnsupportedOperationException.
	 * 
	 * @return The dice contained in this pool.
	 * 
	 * @throws UnsupportedOperationException If the composite dice can't be retrieved.
	 */
	default List<IDie<SideType>> contained() {
		throw new UnsupportedOperationException("Can't get composite dice");
	}
	
	/*
	 * These die pool operations transform this pool in some way.
	 */
	
	/**
	 * Returns a version of this die pool which returns its results in sorted
	 * order.
	 * 
	 * @param isDescending True to sort in descending order, false to sort in ascending order.
	 * 
	 * @return The die pool, which returns its results in sorted order.
	 */
	default IDiePool<SideType> sorted(
			Comparator<SideType> comparer,
			boolean isDescending) {
		return new TransformDiePool<>(this,
				(pool) -> pool.sorted(
						isDescending 
						? comparer.reversed()
						: comparer));
	}
	
	/**
	 * Return a die pool which rolls this one, then filters out any results that
	 * don't match the provided predicate.
	 * 
	 * @param matcher The predicate that determines 
	 * 
	 * @return A die pool which contains only entries that pass the predicate.
	 */
	default IDiePool<SideType> filtered(Predicate<SideType> matcher) {
		return new TransformDiePool<>(this,
				(pool) -> pool.filter(matcher));
	}
	
	/**
	 * Return a die pool which rolls this one, then drops a number of the first values.
	 * 
	 * @param number The number of first values to drop.
	 * 
	 * @return A die pool which has the first entries dropped.
	 */
	default IDiePool<SideType> dropFirst(int number) {
		return new TransformDiePool<>(this,
				(pool) -> pool.skip(number));
	}

	/**
	 * Return a die pool which rolls this one, then drops a number of the last values.
	 * 
	 * @param number The number of last values to drop.
	 * 
	 * @return A die pool which has the last entries dropped.
	 */
	default IDiePool<SideType> dropLast(int number) {
		return new TransformDiePool<>(this, (pool) -> {
			Deque<SideType> temp = new ArrayDeque<>();
			
			pool.forEachOrdered((die) -> temp.add(die));
			
			for (int i = 0; i < number; i++) temp.pollLast();
			
			return temp.stream();
		});
	}

	/**
	 * Return a die pool which rolls this one, then keeps a number of the first values.
	 * 
	 * @param number The number of first values to keep.
	 * 
	 * @return A die pool which has the first entries kept.
	 */
	default IDiePool<SideType> keepFirst(int number) {
		return new TransformDiePool<>(this,
				(pool) -> pool.limit(number));
	}
	
	/**
	 * Return a die pool which rolls this one, then keeps a number of the last values.
	 * 
	 * @param number The number of last values to keep.
	 * 
	 * @return A die pool which has the last entries kept.
	 */
	default IDiePool<SideType> keepLast(int number) {
		return new TransformDiePool<>(this, (pool) -> {
			Deque<SideType> temp = new ArrayDeque<>();
			
			pool.forEachOrdered((die) -> temp.add(die));
			
			while (temp.size() > number) temp.pollFirst();
			
			return temp.stream();
		});
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
	default IDiePool<SideType> dropLowest(Comparator<SideType> comparer, int number) {
		return this.sorted(comparer, false).dropFirst(number);
	}
	
	/**
	 * Return a die pool which rolls this one, then drops a number of the lowest values.
	 * 
	 * @param number The number of lowest values to drop.
	 * 
	 * @return A die pool which has the lowest entries dropped.
	 */
	default IDiePool<SideType> dropHighest(Comparator<SideType> comparer,int number) {
		return this.sorted(comparer, false).dropLast(number);
	}
	
	/**
	 * Return a die pool which rolls this one, then keeps a number of the lowest values.
	 * 
	 * @param number The number of lowest values to keep.
	 * 
	 * @return A die pool which has the lowest entries kept.
	 */
	default IDiePool<SideType> keepLowest(Comparator<SideType> comparer,int number) {
		return this.sorted(comparer, false).keepFirst(number);
	}
	
	/**
	 * Return a die pool which rolls this one, then keeps a number of the highest values.
	 * 
	 * @param number The number of highest values to keep.
	 * 
	 * @return A die pool which has the highest entries kept.
	 */
	default IDiePool<SideType> keepHighest(Comparator<SideType> comparer,int number) {
		return this.sorted(comparer, false).keepLast(number);
	}
	
	/* These are misc. operations that don't form new dice pools. */
	
	/**
	 * Get an iterator which iterates over a single roll of this die pool.
	 * 
	 * @param rng The source of random numbers.
	 * 
	 * @return An iterator over a single roll of this die pool.
	 */
	default Iterator<SideType> iterator(Random rng) {
		return this.roll(rng).iterator();
	}

	/**
	 * Create a die pool containing the provided dice.
	 * 
	 * @param dice The dice to put into the pool.
	 * 
	 * @return A pool which contains the provided dice.
	 */
	@SafeVarargs
	static <Side> IDiePool<Side> containing(IDie<Side>... dice) {
		return new FixedDiePool<>(dice);
	}
}