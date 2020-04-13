package bjc.dicelang.dicev2;

import java.util.Random;

/**
 * A dice where both the number of dice to roll and the number of sides of the
 * dice are specified by other dice.
 * 
 * @author Ben Culkin
 *
 */
public class CompositeDie extends Die {
	/**
	 * The die that provides the number of dice to roll.
	 */
	public final Die numDice;
	/**
	 * The die that provides the number of sides of the dice to roll.
	 */
	public final Die numSides;

	/**
	 * Should the number of sides per dice be rerolled after every dice is rolled?
	 * 
	 * By default, the number of sides will be rolled once per roll of this die.
	 */
	public final boolean rerollSides;

	/**
	 * Create a new composite dice that rolls the number of sides once.
	 * 
	 * @param numDice
	 *                 The number of dice to roll.
	 * @param numSides
	 *                 The number of sides on the dice.
	 */
	public CompositeDie(Die numDice, Die numSides) {
		this(numDice, numSides, false);
	}

	/**
	 * Create a new composite dice with the specified side-rolling behavior.
	 * 
	 * @param numDice
	 *                    The number of dice to roll.
	 * @param numSides
	 *                    The number of sides on the dice.
	 * @param rerollSides
	 *                    Whether to rolls the sides once per roll, or once per
	 *                    dice.
	 */
	public CompositeDie(Die numDice, Die numSides, boolean rerollSides) {
		super();

		this.numDice = numDice;
		this.numSides = numSides;

		this.rerollSides = rerollSides;
	}

	/**
	 * Create a new composite dice using a specific RNG, rolling dice one per side.
	 * 
	 * @param rnd
	 *                 The RNG to use.
	 * @param numDice
	 *                 The number of dice to use.
	 * @param numSides
	 *                 The number of sides for the dice.
	 */
	public CompositeDie(Random rnd, Die numDice, Die numSides) {
		this(rnd, numDice, numSides, false);
	}

	/**
	 * Create a new composite dice using a specific RNG and side-rolling behavior.
	 * 
	 * @param rnd
	 *                    The RNG to use.
	 * @param numDice
	 *                    The number of dice to use.
	 * @param numSides
	 *                    The number of sides on the dice.
	 * @param rerollSides
	 *                    Whether to rolls the sides once per roll, or once per
	 *                    dice.
	 */
	public CompositeDie(Random rnd, Die numDice, Die numSides, boolean rerollSides) {
		super(rnd);

		this.numDice = numDice;
		this.numSides = numSides;

		this.rerollSides = rerollSides;
	}

	@Override
	public long[] roll() {
		int target = (int) numDice.rollSingle();
		int sides = (int) numSides.rollSingle();

		long[] res = new long[target];

		for (int i = 0; i < target; i++) {
			res[i] = rng.nextInt(sides) + 1;

			if (rerollSides)
				sides = (int) numSides.rollSingle();
		}

		return res;
	}

	@Override
	public long rollSingle() {
		return rng.nextInt((int) numSides.rollSingle());
	}

	@Override
	public boolean canOptimize() {
		if (numSides.canOptimize()) {
			if (numSides.optimize() <= 1) {
				return true;
			}
		}

		if (numDice.canOptimize()) {
			if (numDice.optimize() == 0) {
				return true;
			}
		}

		return false;
	}

	@Override
	public long optimize() {
		if (numDice.canOptimize())
			return 0;
		if (numSides.canOptimize() && numSides.optimize() == 0)
			return 0;

		return numDice.rollSingle();
	}
}
