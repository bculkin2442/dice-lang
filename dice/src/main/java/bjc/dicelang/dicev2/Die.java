package bjc.dicelang.dicev2;

import java.util.Random;

/**
 * An abstract class that represents a single pool of dice.
 * 
 * @author Ben Culkin
 *
 */
public abstract class Die {
	private static final Random BASE = new Random();

	/**
	 * The RNG to use.
	 */
	protected Random rng;

	/**
	 * Create a new basic die.
	 */
	protected Die() {
		rng = BASE;
	}

	/**
	 * Create a new basic die.
	 * 
	 * @param rnd
	 *            The RNG to use.
	 */
	protected Die(Random rnd) {
		rng = rnd;
	}

	/**
	 * Set the RNG this die pool uses.
	 * 
	 * @param rnd
	 *            The RNG used by the die pool.
	 */
	public void setRandom(Random rnd) {
		rng = rnd;
	}

	/**
	 * Roll the entire die pool.
	 * 
	 * @return The results from rolling the dice.
	 */
	public abstract long[] roll();

	/**
	 * Roll a single die in the pool.
	 * 
	 * For pools with multiple die, this may be somewhat arbitrary.
	 * 
	 * @return Result from rolling a single die in the pool.
	 */
	public abstract long rollSingle();

	/**
	 * Can this pool be optimized?
	 * 
	 * @return Is the pool optimizable?
	 */
	public abstract boolean canOptimize();

	/**
	 * Optimize the die pool.
	 * 
	 * Is undefined if called while canOptimize is false.
	 * 
	 * @return The optimized version of the pool.
	 */
	public abstract long optimize();
}
