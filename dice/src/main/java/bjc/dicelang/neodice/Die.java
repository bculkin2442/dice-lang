package bjc.dicelang.neodice;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import bjc.esodata.*;

/**
 * Represents a single polyhedral die.
 * @author Ben Culkin
 *
 */
@FunctionalInterface
public interface Die {
	/**
	 * Rolls this die.
	 * 
	 * @param rng The source for random numbers
	 * 
	 * @return The result of rolling the die.
	 */
	public int roll(Random rng);
	
	/**
	 * Returns a die pool which rolls this die the specified number of times.
	 * 
	 * @param numTimes The number of times to roll this die.
	 * 
	 * @return A die pool that rolls this die the specified number of times.
	 */
	default DiePool times(int numTimes) {
		return new TimesDiePool(this, numTimes);
	};
	
	/**
	 * Returns a die which will reroll this die as long as the provided condition is true.
	 * 
	 * @param condition The condition to reroll the die on.
	 * 
	 * @return A die that rerolls when the given condition is met.
	 */
	default Die reroll(IntPredicate condition) {
		return new RerollDie(this, condition, (lst) -> lst.get(lst.size()));
	}
	
	/**
	 * Returns a die which will reroll this die up to a specified number of times,
	 * as long as the provided condition is true.
	 * 
	 * @param condition The condition to reroll the die on.
	 * @param limit The maximum number of times to reroll the die.
	 * 
	 * @return A die that rerolls when the given condition is met.
	 */
	default Die reroll(IntPredicate condition, int limit) {
		return new RerollDie(this, condition, (lst) -> lst.get(lst.size()), limit);
	}
	
	/**
	 * Create an iterator which gives rolls of this dice.
	 * 
	 * @param rng The source for random numbers.
	 * 
	 * @return An iterator which gives rolls of this dice.
	 */
	default Iterator<Integer> iterator(Random rng) {
		return IntStream.generate(() -> this.roll(rng)).iterator();
	}
}

final class TimesDiePool implements DiePool {
	private final Die contained;
	private final int numDice;

	public TimesDiePool(Die contained, int numDice) {
		this.contained = contained;
		this.numDice = numDice;
	}

	@Override
	public int[] roll(Random rng) {
		int[] results = new int[numDice];
		
		for (int index = 0; index < numDice; index++) {
			results[index] = contained.roll(rng);
		}
		
		return results;
	}
	
	@Override
	public Die[] contained() {
		Die[] results = new Die[numDice];
		
		for (int index = 0; index < numDice; index++) {
			results[index] = contained;
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
		
		TimesDiePool other = (TimesDiePool) obj;
	
		return Objects.equals(contained, other.contained) && numDice == other.numDice;
	}
}

final class RerollDie implements Die {
	private final Die          contained;
	
	private final IntPredicate                           condition;
	private final Function<MinMaxList<Integer>, Integer> chooser;
	
	private int limit = Integer.MAX_VALUE;
	
	
	public RerollDie(Die contained, IntPredicate condition,
			Function<MinMaxList<Integer>, Integer> chooser) {
		this.contained = contained;
		
		this.condition = condition;
		this.chooser   = chooser;
	}
	
	public RerollDie(Die contained, IntPredicate condition,
			Function<MinMaxList<Integer>, Integer> chooser, int limit) {
		this.contained = contained;
		
		this.condition = condition;
		this.chooser   = chooser;
		
		this.limit = limit;
	}
	
	@Override
	public int roll(Random rng) {
		int roll = contained.roll(rng);
		
		MinMaxList<Integer> newRolls = new MinMaxList<Integer>(
				Comparator.naturalOrder(), roll);
		
		int rerollCount = 0;
		while (condition.test(roll) && rerollCount < limit) {
			roll = contained.roll(rng);
			newRolls.add(roll);
			
			rerollCount += 1;
		}
		
		return chooser.apply(newRolls);
	}

	// No toString, because IntPredicate can't be converted to a string
}