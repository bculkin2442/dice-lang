package bjc.dicelang.neodice;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import bjc.dicelang.neodice.die.*;
import bjc.dicelang.neodice.diepool.*;

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

	/**
	 * Create a simple polyhedral die with a fixed number of sides.
	 * 
	 * @param sides The number of sides for the die.
	 * 
	 * @return A die which returns a result from 1 to sides.
	 */
	static Die polyhedral(int sides) {
		return new PolyhedralDie(sides);
	}
}