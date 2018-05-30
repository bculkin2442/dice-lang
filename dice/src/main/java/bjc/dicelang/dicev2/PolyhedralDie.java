package bjc.dicelang.dicev2;

import java.util.Random;

public class PolyhedralDie extends Die {
	public final int numDice;
	public final int numSides;

	public PolyhedralDie(int numDice, int numSides) {
		super();

		this.numDice  = numDice;
		this.numSides = numSides;
	}

	public PolyhedralDie(Random rnd, int numDice, int numSides) {
		super(rnd);

		this.numDice  = numDice;
		this.numSides = numSides;
	}

	public long[] roll() {
		long[] res = new long[numDice];

		for(int i = 0; i < numDice; i++) {
			res[i] = rollSingle();
		}

		return res;
	}

	public long rollSingle() {
		/* nextInt is from 0 (inclusive) to numSides (exclusive) */
		return rng.nextInt(numSides) + 1;
	}

	public boolean canOptimize() {
		return numSides <= 1 || numDice == 0;
	}

	public long optimize() {
		if(numDice == 0 || numSides == 0) return 0;

		return numDice;
	}
}
