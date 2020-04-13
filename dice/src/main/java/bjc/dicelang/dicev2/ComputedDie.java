package bjc.dicelang.dicev2;

import java.util.Random;
import java.util.function.IntSupplier;

/**
 * Create a computed die, which gets its values from arbitrary functions.
 * @author Ben Culkin
 *
 */
public class ComputedDie extends Die {
	/**
	 * The function that provides the number of dice to roll.
	 */
	public final IntSupplier numDice;
	/**
	 * The function that provides the number of sides for the dice.
	 */
	public final IntSupplier numSides;

	/**
	 * Whether or not the number of sides should be rolled once per die.
	 */
	public final boolean rerollSides;

	/**
	 * Create a new computed die.
	 * @param numDice The number of dice to roll.
	 * @param numSides The number of sides on the dice.
	 */
	public ComputedDie(IntSupplier numDice, IntSupplier numSides) {
		this(numDice, numSides, false);
	}

	/**
	 * Create a new computed die with specified side-roll behavior.
	 * @param numDice The number of dice to roll.
	 * @param numSides The number of sides on the dice.
	 * @param rerollSides Controls whether the number of sides should be rerolled once per die.
	 */
	public ComputedDie(IntSupplier numDice, IntSupplier numSides, boolean rerollSides) {
		super();

		this.numDice  = numDice;
		this.numSides = numSides;

		this.rerollSides = rerollSides;
	}
	
	/**
	 * Create a new computed die using a specified number of RNGs.
	 * @param rnd The RNG to use.
	 * @param numDice The number of dice to roll.
	 * @param numSides The number of sides on the dice.
	 */
	public ComputedDie(Random rnd, IntSupplier numDice, IntSupplier numSides) {
		this(rnd, numDice, numSides, false);
	}
	
	/**
	 * Create a new computed die using a specified number of RNGs and side-roll behavior.
	 * @param rnd The RNG to use.
	 * @param numDice The number of dice to roll.
	 * @param numSides The number of sides on the dice.
	 * @param rerollSides Controls whether the number of sides should be rerolled once per die.
	 */
	public ComputedDie(Random rnd, IntSupplier numDice, IntSupplier numSides, boolean rerollSides) {
		super(rnd);

		this.numDice  = numDice;
		this.numSides = numSides;

		this.rerollSides = rerollSides;
	}

	@Override
	public long[] roll() {
		int target = numDice.getAsInt();
		int sides  = numSides.getAsInt();

		long[] res = new long[target];

		for(int i = 0; i < target; i++) {
			res[i] = rng.nextInt(sides) + 1;

			if(rerollSides) sides = numSides.getAsInt();
		}

		return res;
	}

	@Override
	public long rollSingle() {
		return rng.nextInt(numSides.getAsInt());
	}

	/*
	 * @NOTE
	 *
	 * :UnoptimizableDice
	 *
	 * Here, we assume that we can't optimize because we have no way
	 * of knowing that our suppliers will always return the same
	 * thing. As a matter of fact, they almost always will have that
	 * behavior, otherwise you wouldn't need ComputedDie.
	 */

	@Override
	public boolean canOptimize() {
		return false;
	}

	@Override
	public long optimize() {
		throw new UnsupportedOperationException("ComputedDie cannot be optimized");
	}
}
