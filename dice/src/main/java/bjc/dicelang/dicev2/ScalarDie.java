package bjc.dicelang.dicev2;

/**
 * Die which represents a single number.
 * @author Ben Culkin
 *
 */
public class ScalarDie extends Die {
	/**
	 * The value this die represents.
	 */
	public final long val;

	/**
	 * Create a new die representing a single number.
	 * 
	 * @param val The number for the die.
	 */
	public ScalarDie(long val) {
		super();

		this.val = val;
	}

	@Override
	public long[] roll() {
		return new long[] { rollSingle() };
	}

	@Override
	public long rollSingle() {
		return val;
	}

	@Override
	public boolean canOptimize() {
		return true;
	}

	@Override
	public long optimize() {
		return val;
	}
}
