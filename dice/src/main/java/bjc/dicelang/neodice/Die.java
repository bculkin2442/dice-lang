package bjc.dicelang.neodice;

import java.util.*;
import java.util.stream.*;

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