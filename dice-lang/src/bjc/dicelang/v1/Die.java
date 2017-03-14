package bjc.dicelang.v1;

import java.util.Random;

/**
 * A single polyhedral dice
 *
 * @author ben
 *
 */
public class Die implements IDiceExpression {
	/*
	 * Random # gen to use for dice
	 */
	private static Random rng = new Random();

	/*
	 * Number of sides this die has
	 */
	private int nSides;

	/**
	 * Create a die with the specified number of sides
	 *
	 * @param nSides
	 *                The number of sides this dice has
	 */
	public Die(int nSides) {
		if(nSides < 1) throw new UnsupportedOperationException("Dice with less than 1 side are not supported");

		this.nSides = nSides;
	}

	@Override
	public boolean canOptimize() {
		return nSides == 1;
	}

	@Override
	public int optimize() {
		if(nSides != 1) throw new UnsupportedOperationException("Can't optimize " + nSides + "-sided dice");

		return 1;
	}

	@Override
	public int roll() {
		return rng.nextInt(nSides) + 1;
	}

	@Override
	public String toString() {
		return "d" + nSides;
	}
}
