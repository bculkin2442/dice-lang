package bjc.dicelang.neodice;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * Represents a pool of dice.
 * 
 * @author Ben Culkin
 * 
 * @param <SideType> The type of the sides of the contained dice.
 */
@FunctionalInterface
public interface DiePool<SideType> {
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
	default List<Die<SideType>> contained() {
		throw new UnsupportedOperationException("Can't get composite dice");
	}
	
	/*
	 * These die pool operations transform this pool in some way.
	 */
	
	/**
	 * Returns a version of this die pool which returns its results in sorted
	 * order.
	 * 
	 * @param comparer The comparator to use for the dice.
	 * @param isDescending True to sort in descending order, false to sort in ascending order.
	 * 
	 * @return The die pool, which returns its results in sorted order.
	 */
	default DiePool<SideType> sorted(
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
	default DiePool<SideType> filtered(Predicate<SideType> matcher) {
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
	default DiePool<SideType> dropFirst(int number) {
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
	default DiePool<SideType> dropLast(int number) {
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
	default DiePool<SideType> keepFirst(int number) {
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
	default DiePool<SideType> keepLast(int number) {
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
	 * @param comparer The comparer to use for the sides.
	 * @param number The number of lowest values to drop.
	 * 
	 * @return A die pool which has the lowest entries dropped.
	 */
	default DiePool<SideType> dropLowest(Comparator<SideType> comparer, int number) {
		return this.sorted(comparer, false).dropFirst(number);
	}
	
	/**
	 * Return a die pool which rolls this one, then drops a number of the lowest values.
	 * 
	 * @param comparer The comparer to use for the sides.
	 * @param number The number of lowest values to drop.
	 * 
	 * @return A die pool which has the lowest entries dropped.
	 */
	default DiePool<SideType> dropHighest(Comparator<SideType> comparer,int number) {
		return this.sorted(comparer, false).dropLast(number);
	}
	
	/**
	 * Return a die pool which rolls this one, then keeps a number of the lowest values.
	 * 
	 * @param comparer The comparer to use for the sides.
	 * @param number The number of lowest values to keep.
	 * 
	 * @return A die pool which has the lowest entries kept.
	 */
	default DiePool<SideType> keepLowest(Comparator<SideType> comparer,int number) {
		return this.sorted(comparer, false).keepFirst(number);
	}
	
	/**
	 * Return a die pool which rolls this one, then keeps a number of the highest values.
	 * 
	 * @param comparer The comparer to use for the sides.
	 * @param number The number of highest values to keep.
	 * 
	 * @return A die pool which has the highest entries kept.
	 */
	default DiePool<SideType> keepHighest(Comparator<SideType> comparer,int number) {
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
	 * @param <Side> The type of the sides.
	 * 
	 * @param dice The dice to put into the pool.
	 * 
	 * @return A pool which contains the provided dice.
	 */
	@SafeVarargs
	static <Side> DiePool<Side> containing(Die<Side>... dice) {
		return new FixedDiePool<>(dice);
	}
	
	/**
	 * Create an expanding die pool
	 * 
	 * @param <Side> The type of the sides.
	 * 
	 * @param contained The contained die.
	 * @param expander The expanding function.
	 * 
	 * @return A die pool that expands the result given the provided function.
	 */
	static <Side> DiePool<Side> expanding(Die<Side> contained,
	        BiFunction<Die<Side>, Random, Stream<Side>> expander)
	{
	    return new ExpandDiePool<>(contained, expander);
	}
}

/**
 * A die pool that can expand dice.
 * @author Ben Culkin
 *
 * @param <SideType> The type the die uses.
 */
class ExpandDiePool<SideType> implements DiePool<SideType> {
    private final Die<SideType> contained;
    
    private final BiFunction<Die<SideType>, Random, Stream<SideType>> expander;

    /**
     * Create a new expanding die pool.
     * 
     * @param contained The die to expand.
     * @param expander The function to use for expanding.
     */
    public ExpandDiePool(Die<SideType> contained,
            BiFunction<Die<SideType>, Random, Stream<SideType>> expander) {
        this.contained = contained;
        this.expander = expander;
    }

    @Override
    public Stream<SideType> roll(Random rng) {
        return expander.apply(contained, rng);
    }
    
    @Override
    public List<Die<SideType>> contained()
    {
        return Arrays.asList(contained);
    }
}

/**
 * A die pool that has a fixed size.
 * 
 * @author Ben Culkin
 *
 * @param <SideType> The type of the sides of the dice.
 */
class FixedDiePool<SideType> implements DiePool<SideType> {
    private final List<Die<SideType>> dice;

    /**
     * Create a new fixed dice pool.
     * @param dice The dice to put into the pool.
     */
    public FixedDiePool(List<Die<SideType>> dice) {
        this.dice = dice;
    }
    
    /**
     * Create a new fixed dice pool from an array of dice.
     * @param dice The dice to put into the pool.
     */
    @SafeVarargs
    public FixedDiePool(Die<SideType>...dice) {
        this.dice = new ArrayList<>(dice.length);
        for (Die<SideType> die : dice) {
            this.dice.add(die);
        }
    }

    @Override
    public Stream<SideType> roll(Random rng) {
        return dice.stream().map((die) -> die.roll(rng));
    }

    @Override
    public List<Die<SideType>> contained() {
        return dice;
    }

    
    @Override
    public String toString() {
        return dice.stream()
            .map(Die<SideType>::toString)
            .collect(Collectors.joining(", "));
    }

    @Override
    public int hashCode() {
        return Objects.hash(dice);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)                  return true;
        if (obj == null)                  return false;
        if (getClass() != obj.getClass()) return false;
    
        FixedDiePool<?> other = (FixedDiePool<?>) obj;
        
        return Objects.equals(dice, other.dice);
    }
}

class TimesDiePool<SideType> implements DiePool<SideType> {
    private final Die<SideType> contained;
    private final int numDice;

    public TimesDiePool(Die<SideType> contained, int numDice) {
        this.contained = contained;
        this.numDice = numDice;
    }

    @Override
    public Stream<SideType> roll(Random rng) {
        return Stream.generate(() -> contained.roll(rng))
            .limit(numDice);
    }
    
    @Override
    public List<Die<SideType>> contained() {
        List<Die<SideType>> results = new ArrayList<>(numDice);
        
        for (int index = 0; index < numDice; index++) {
            results.add(contained);
        }
        
        return results;
    }

    @Override
    public String toString() {
        return String.format("%d%s", numDice, contained);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(contained, numDice);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)                  return true;
        if (obj == null)                  return false;
        if (getClass() != obj.getClass()) return false;
        
        TimesDiePool<?> other = (TimesDiePool<?>) obj;
    
        return Objects.equals(contained, other.contained) && numDice == other.numDice;
    }
}

class TransformDiePool<SideType> implements DiePool<SideType> {
    private final DiePool<SideType> contained;
    
    private UnaryOperator<Stream<SideType>> transform;

    public TransformDiePool(DiePool<SideType> contained,
            UnaryOperator<Stream<SideType>> transform) {
        super();
        this.contained = contained;
        this.transform = transform;
    }

    @Override
    public Stream<SideType> roll(Random rng) {
        return transform.apply(contained.roll(rng));
    }
    
    @Override
    public List<Die<SideType>> contained() {
        return contained.contained();
    }

    @Override
    public int hashCode() {
        return Objects.hash(contained, transform);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)                  return true;
        if (obj == null)                  return false;
        if (getClass() != obj.getClass()) return false;
    
        TransformDiePool<?> other = (TransformDiePool<?>) obj;
        
        return Objects.equals(contained, other.contained)
                && Objects.equals(transform, other.transform);
    }
    
    @Override
    public String toString() {
        return contained.toString() + "(transformed)";
    }
}