package bjc.dicelang;

import java.util.Random;

/**
 * A single polyhedral dice
 * 
 * @author ben
 *
 */
public class Die implements IDiceExpression {
	/**
	 * Random # gen to use for dice
	 */
	private static Random	rng	= new Random();

	/**
	 * Number of sides this die has
	 */
	private int				nSides;

	/**
	 * Create a die with the specified number of sides
	 * 
	 * @param nSides
	 *            The number of sides this dice has
	 */
	public Die(int nSides) {
		this.nSides = nSides;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see bjc.utils.dice.IDiceExpression#roll()
	 */
	@Override
	public int roll() {
		return rng.nextInt(nSides) + 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "d" + nSides;
	}
}
