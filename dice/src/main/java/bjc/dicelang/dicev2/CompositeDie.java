package bjc.dicelang.dicev2;

import java.util.Random;

public class CompositeDie extends Die {
	public final Die numDice;
	public final Die numSides;

	public final boolean rerollSides;

	public CompositeDie(Die numDice, Die numSides) {
		this(numDice, numSides, false);
	}

	public CompositeDie(Die numDice, Die numSides, boolean rerollSides) {
		super();

		this.numDice  = numDice;
		this.numSides = numSides;

		this.rerollSides = rerollSides;
	}

	public CompositeDie(Random rnd, Die numDice, Die numSides) {
		this(rnd, numDice, numSides, false);
	}

	public CompositeDie(Random rnd, Die numDice, Die numSides, boolean rerollSides) {
		super(rnd);

		this.numDice  = numDice;
		this.numSides = numSides;

		this.rerollSides = rerollSides;
	}

	public long[] roll() {
		int target = (int)numDice.rollSingle();
		int sides  = (int)numSides.rollSingle();

		long[] res = new long[target];

		for(int i = 0; i < target; i++) {
			res[i] = rng.nextInt(sides) + 1;

			if(rerollSides) sides = (int)numSides.rollSingle();
		}

		return res;
	}

	public long rollSingle() {
		return rng.nextInt((int)numSides.rollSingle());
	}

	public boolean canOptimize() {
		if(numSides.canOptimize()) {
			if(numSides.optimize() <= 1) {
				return true;
			}
		}

		if(numDice.canOptimize()) {
			if(numDice.optimize() == 0) {
				return true;
			}
		}

		return false;
	}

	public long optimize() {
		if(numDice.canOptimize()) return 0;
		if(numSides.canOptimize() && numSides.optimize() == 0) return 0;

		return numDice.rollSingle();
	}
}
