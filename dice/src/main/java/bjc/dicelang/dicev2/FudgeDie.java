package bjc.dicelang.dicev2;

import java.util.Random;

/**
 * Create a set of Fudge dice.
 * 
 * Fudge dice are dice which can roll -1, 0 or 1.
 * 
 * @author Ben Culkin
 *
 */
public class FudgeDie extends Die {
	/**
	 * The number of dice to roll.
	 */
	public final int numDice;

	/**
	 * Create a new pool for fudge dice.
	 * 
	 * @param numDice The number of dice in the pool.
	 */
	public FudgeDie(int numDice) {
		super();

		this.numDice = numDice;
	}

	/**
	 * Create a new pool for fudge dice.
	 * 
	 * @param rnd The random number generator to use.
	 * @param numDice The number of dice in the pool.
	 */
	public FudgeDie(Random rnd, int numDice) {
		super(rnd);

		this.numDice = numDice;
	}

	@Override
	public long[] roll() {
		long[] res = new long[numDice];

		for(int i = 0; i < numDice; i++) {
			res[i] = rollSingle();
		}

		return res;
	}

	@Override
	public long rollSingle() {
		/* Return an int in the range [-1, 1] */
		return rng.nextInt(3) - 1;
	}

	@Override
	public boolean canOptimize() {
		return numDice == 0;
	}

	@Override
	public long optimize() {
		return 0;
	}
}
