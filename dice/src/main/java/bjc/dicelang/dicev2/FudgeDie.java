package bjc.dicelang.dicev2;

import java.util.Random;

public class FudgeDie extends Die {
	public final int numDice;

	public FudgeDie(int numDice) {
		super();

		this.numDice = numDice;
	}

	public FudgeDie(Random rnd, int numDice) {
		super(rnd);

		this.numDice = numDice;
	}

	public long[] roll() {
		long[] res = new long[numDice];

		for(int i = 0; i < numDice; i++) {
			res[i] = rollSingle();
		}

		return res;
	}

	public long rollSingle() {
		/* Return an int in the range [-1, 1] */
		return rng.nextInt(3) - 1;
	}

	public boolean canOptimize() {
		return numDice == 0;
	}

	public long optimize() {
		return 0;
	}
}
