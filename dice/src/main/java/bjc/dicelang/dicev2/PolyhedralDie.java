package bjc.dicelang.dicev2;

import java.util.Random;

/**
 * Polyhedral die pool
 * @author Ben Culkin
 *
 */
public class PolyhedralDie extends Die {
	/**
	 * The number of dice in the pool.
	 */
	public final int numDice;
	
	/**
	 * The number of sides on each die.
	 */
	public final int numSides;

	/**
	 * Create a new polyhedral die pool.
	 * 
	 * @param numDice The number of dice in the pool.
	 * @param numSides The number of side on each die.
	 */
	public PolyhedralDie(int numDice, int numSides) {
		super();

		this.numDice  = numDice;
		this.numSides = numSides;
	}

	/**
	 * Create a new polyhedral die pool.
	 * 
	 * @param rnd The random number generator to use. 
	 * @param numDice The number of dice in the pool.
	 * @param numSides The number of side on each die.
	 */
	public PolyhedralDie(Random rnd, int numDice, int numSides) {
		super(rnd);

		this.numDice  = numDice;
		this.numSides = numSides;
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
		/* nextInt is from 0 (inclusive) to numSides (exclusive) */
		return rng.nextInt(numSides) + 1;
	}

	@Override
	public boolean canOptimize() {
		return numSides <= 1 || numDice == 0;
	}

	@Override
	public long optimize() {
		if(numDice == 0 || numSides == 0) return 0;

		return numDice;
	}
}
