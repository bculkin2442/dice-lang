package bjc.dicelang.neodice;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import bjc.esodata.*;

/**
 * Represents a single polyhedral die.
 * @author Ben Culkin
 * 
 * @param <SideType> The type of value represented by this die.
 *
 */
@FunctionalInterface
public interface Die<SideType> {
	/**
	 * Rolls this die.
	 * 
	 * @param rng The source for random numbers
	 * 
	 * @return The result of rolling the die.
	 */
	public SideType roll(Random rng);
	
	/**
	 * Returns a die pool which rolls this die the specified number of times.
	 * 
	 * @param numTimes The number of times to roll this die.
	 * 
	 * @return A die pool that rolls this die the specified number of times.
	 */
	default DiePool<SideType> times(int numTimes) {
		return new ExpandDiePool<>(this, (die, rng) -> {
			return Stream.generate(() -> die.roll(rng))
					.limit(numTimes);
		});
	};
	
	/**
	 * Returns a die which will reroll this die as long as the provided condition is true.
	 * 
     * @param comparer The thing to use to compare die values.
	 * @param condition The condition to reroll the die on.
	 * 
	 * @return A die that rerolls when the given condition is met.
	 */
	default Die<SideType> reroll(
			Comparator<SideType> comparer,
			Predicate<SideType> condition) {
		return RerollDie.create(comparer, this, condition,
				(list) -> list.get(list.size()));
	}
	
	/**
	 * Returns a die which will reroll this die up to a specified number of times,
	 * as long as the provided condition is true.
	 * 
	 * @param comparer The thing to use to compare die values.
	 * @param condition The condition to reroll the die on.
	 * @param limit The maximum number of times to reroll the die.
	 * 
	 * @return A die that rerolls when the given condition is met.
	 */
	default Die<SideType> reroll(
			Comparator<SideType> comparer,
			Predicate<SideType> condition,
			int limit) {
		return RerollDie.create(comparer, this, condition,
				(list) -> list.get(list.size()), limit);
	}
	
	/**
	 * Create an stream which gives rolls of this dice.
	 * 
	 * @param rng The source for random numbers.
	 * 
	 * @return An iterator which gives rolls of this dice.
	 */
	default Stream<SideType> stream(Random rng) {
		return Stream.generate(() -> this.roll(rng));
	}
	
	/**
	 * Create a new transforming die.
	 * 
	 * @param <NewType> The new type of the die.
	 * 
	 * @param mapper The function to use for mapping.
	 * 
	 * @return A die that transforms with the given mapping.
	 */
	default <NewType> Die<NewType> transform(Function<SideType, NewType> mapper) {
		return (rng) -> mapper.apply(this.roll(rng));
	}
	
	/**
	 * Create a simple polyhedral die with a fixed number of sides.
	 * 
	 * @param sides The number of sides for the die.
	 * 
	 * @return A die which returns a result from 1 to sides.
	 */
	static Die<Integer> polyhedral(int sides) {
		return new PolyhedralDie(sides);
	}
}

class PolyhedralDie implements Die<Integer> {
    private final int sides;

    public PolyhedralDie(int sides) {
        this.sides = sides;
    }

    @Override
    public Integer roll(Random rng) {
        // Dice are one-based, not zero-based.
        return rng.nextInt(sides) + 1;
    }

    @Override
    public String toString() {
        return String.format("d%d", sides);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + sides;        
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)                  return true;
        if (obj == null)                  return false;
        if (getClass() != obj.getClass()) return false;
        
        PolyhedralDie other = (PolyhedralDie) obj;
        
        if (sides != other.sides) return false;
        else                      return true;
    }
}

class RerollDie<SideType> implements Die<SideType> {
    private final Die<SideType> contained;
    
    private final Predicate<SideType>                    condition;
    private final Function<MinMaxList<SideType>, SideType> chooser;
    
    private final Comparator<SideType> comparer;
    
    private int limit = Integer.MAX_VALUE;
    
    private RerollDie(
            Comparator<SideType> comparer,
            Die<SideType> contained,
            Predicate<SideType> condition,
            Function<MinMaxList<SideType>, SideType> chooser) {
        this.comparer = comparer;
        
        this.contained = contained;
        
        this.condition = condition;
        this.chooser   = chooser;
    }
    
    private RerollDie(
            Comparator<SideType> comparer,
            Die<SideType> contained,
            Predicate<SideType> condition,
            Function<MinMaxList<SideType>, SideType> chooser,
            int limit) {
        this(comparer, contained, condition, chooser);
        
        this.limit = limit;
    }
    
    @Override
    public SideType roll(Random rng) {
        SideType roll = contained.roll(rng);

        MinMaxList<SideType> newRolls = new MinMaxList<>(comparer, roll);
        
        int rerollCount = 0;
        while (condition.test(roll) && rerollCount < limit) {
            roll = contained.roll(rng);
            newRolls.add(roll);
            
            rerollCount += 1;
        }
        
        return chooser.apply(newRolls);
    }

    public static <Side extends Comparable<Side>> Die<Side> create(
            Die<Side> contained,
            Predicate<Side> condition,
            Function<MinMaxList<Side>, Side> chooser) {
        return new RerollDie<>(Comparator.naturalOrder(), contained, condition, chooser);
    }
    
    public static <Side extends Comparable<Side>> Die<Side> create(
            Die<Side> contained,
            Predicate<Side> condition,
            Function<MinMaxList<Side>, Side> chooser,
            int limit) {
        return new RerollDie<>(Comparator.naturalOrder(), contained, condition, chooser, limit);
    }
    

    public static <Side> Die<Side> create(
            Comparator<Side> comparer,
            Die<Side> contained,
            Predicate<Side> condition,
            Function<MinMaxList<Side>, Side> chooser) {
        return new RerollDie<>(comparer, contained, condition, chooser);
    }
    
    public static <Side> Die<Side> create(
            Comparator<Side> comparer,
            Die<Side> contained,
            Predicate<Side> condition,
            Function<MinMaxList<Side>, Side> chooser,
            int limit) {
        return new RerollDie<>(comparer, contained, condition, chooser, limit);
    }
}