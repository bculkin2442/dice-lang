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