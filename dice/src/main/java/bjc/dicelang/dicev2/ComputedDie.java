package bjc.dicelang.dicev2;

import java.util.Random;
import java.util.function.IntSupplier;

public class ComputedDie extends Die {
	public final IntSupplier numDice;
	public final IntSupplier numSides;

	public final boolean rerollSides;

	public ComputedDie(IntSupplier numDice, IntSupplier numSides) {
		this(numDice, numSides, false);
	}

	public ComputedDie(IntSupplier numDice, IntSupplier numSides, boolean rerollSides) {
		super();

		this.numDice  = numDice;
		this.numSides = numSides;

		this.rerollSides = rerollSides;
	}

	public ComputedDie(Random rnd, IntSupplier numDice, IntSupplier numSides) {
		this(rnd, numDice, numSides, false);
	}

	public ComputedDie(Random rnd, IntSupplier numDice, IntSupplier numSides, boolean rerollSides) {
		super(rnd);

		this.numDice  = numDice;
		this.numSides = numSides;

		this.rerollSides = rerollSides;
	}

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

	public boolean canOptimize() {
		return false;
	}

	public long optimize() {
		throw new UnsupportedOperationException("ComputedDie cannot be optimized");
	}
}
