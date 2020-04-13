package bjc.dicelang.dicev2;

import java.util.Random;
import java.util.function.IntSupplier;

/**
 * Utility class for creating die pools.
 * @author Ben Culkin
 *
 */
public class Dies {
	/**
	 * Create a die that always returns a given value.
	 * @param val The value.
	 * @return A die that always returns the specified value.
	 */
	public static Die scalar(long val) {
		return new ScalarDie(val);
	}

	/**
	 * Create a random polyhedral dice.
	 * @param dice The number of dice in the pool.
	 * @param sides The nuumber of sides on the dice.
	 * @return A polyhedral dice, in the given configuration.
	 */
	public static Die polyhedral(int dice, int sides) {
		return new PolyhedralDie(dice, sides);
	}

	/**
	 * Create a random polyhedral dice.
	 * @param rnd The RNG to use.
	 * @param dice The number of dice in the pool.
	 * @param sides The nuumber of sides on the dice.
	 * @return A polyhedral dice, in the given configuration.
	 */
	public static Die polyhedral(Random rnd, int dice, int sides) {
		return new PolyhedralDie(rnd, dice, sides);
	}

	/**
	 * Create a number of fudge dice.
	 * @param dice The number of dice.
	 * @return A die pool with the fudge dice to use.
	 */
	public static Die fudge(int dice) {
		return new FudgeDie(dice);
	}

	/**
	 * Create a number of fudge dice.
	 * @param rnd The RNG to use.
	 * @param dice The number of dice.
	 * @return A die pool with the fudge dice to use.
	 */
	public static Die fudge(Random rnd, int dice) {
		return new FudgeDie(rnd, dice);
	}

	/**
	 * Create a composite series of dice.
	 * @param numDice The number of dice to roll.
	 * @param numSides The number of sides on the dice.
	 * @return A composite dice with the given parameters.
	 */
	public static Die composite(Die numDice, Die numSides) {
		return new CompositeDie(numDice, numSides);
	}

	/**
	 * Create a composite series of dice.
	 * @param numDice The number of dice to roll.
	 * @param numSides The number of sides on the dice.
	 * @param rerollSides Should the number of dice be rolled more than once per pool?
	 * @return A composite dice with the given parameters.
	 */
	public static Die composite(Die numDice, Die numSides, boolean rerollSides) {
		return new CompositeDie(numDice, numSides, rerollSides);
	}

	/**
	 * Create a composite series of dice.
	 * @param rnd The RNG to use.
	 * @param numDice The number of dice to roll.
	 * @param numSides The number of sides on the dice.
	 * @return A composite dice with the given parameters.
	 */
	public static Die composite(Random rnd, Die numDice, Die numSides) {
		return new CompositeDie(rnd, numDice, numSides);
	}

	/**
	 * Create a composite series of dice.
	 * @param rnd The RNG to use.
	 * @param numDice The number of dice to roll.
	 * @param numSides The number of sides on the dice.
	 * @param rerollSides Should the number of dice be rolled more than once per pool?
	 * @return A composite dice with the given parameters.
	 */
	public static Die composite(Random rnd, Die numDice, Die numSides, boolean rerollSides) {
		return new CompositeDie(rnd, numDice, numSides, rerollSides);
	}

	/**
	 * A computed die pool.
	 * @param numDice The provider for the number of dice.
	 * @param numSides The provider for the number of sides.
	 * @return The computed die.
	 */
	public static Die computed(IntSupplier numDice, IntSupplier numSides) {
		return new ComputedDie(numDice, numSides);
	}
	
	/**
	 * A computed die pool.
	 * @param numDice The provider for the number of dice.
	 * @param numSides The provider for the number of sides.
	 * @param rerollSides Should the number of dice be rolled more than once per pool?
	 * @return The computed die.
	 */
	public static Die computed(IntSupplier numDice, IntSupplier numSides, boolean rerollSides) {
		return new ComputedDie(numDice, numSides, rerollSides);
	}
	
	/**
	 * A computed die pool.
	 * @param rnd The RNG to use.
	 * @param numDice The provider for the number of dice.
	 * @param numSides The provider for the number of sides.
	 * @return The computed die.
	 */
	public static Die computed(Random rnd, IntSupplier numDice, IntSupplier numSides) {
		return new ComputedDie(rnd, numDice, numSides);
	}
	
	/**
	 * A computed die pool.
	 * @param rnd The RNG to use.
	 * @param numDice The provider for the number of dice.
	 * @param numSides The provider for the number of sides.
	 * @param rerollSides Should the number of dice be rolled more than once per pool?
	 * @return The computed die.
	 */
	public static Die computed(Random rnd, IntSupplier numDice, IntSupplier numSides, boolean rerollSides) {
		return new ComputedDie(rnd, numDice, numSides, rerollSides);
	}
}
