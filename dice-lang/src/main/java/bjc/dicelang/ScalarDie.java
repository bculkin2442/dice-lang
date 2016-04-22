package bjc.dicelang;

/**
 * A die that represents a static number
 * 
 * @author ben
 *
 */
public class ScalarDie implements IDiceExpression {
	/**
	 * The represented number
	 */
	private int number;

	/**
	 * Create a dice with the specified number
	 * 
	 * @param num
	 *            The number used for the dice
	 */
	public ScalarDie(int num) {
		this.number = num;
	}

	@Override
	public boolean canOptimize() {
		return true;
	}

	@Override
	public int optimize() {
		return number;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see bjc.utils.dice.IDiceExpression#roll()
	 */
	@Override
	public int roll() {
		return number;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return Integer.toString(number);
	}
}