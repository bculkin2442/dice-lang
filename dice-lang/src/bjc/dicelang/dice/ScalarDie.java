package bjc.dicelang.dice;

/**
 * A scalar die, that always returns a given number.
 * 
 * @author EVE
 *
 */
public class ScalarDie implements Die {
	private long val;

	/**
	 * Create a new scalar die with a set value.
	 * 
	 * @param vl
	 *                The value to use.
	 */
	public ScalarDie(long vl) {
		val = vl;
	}

	@Override
	public boolean canOptimize() {
		return true;
	}

	@Override
	public long optimize() {
		return val;
	}

	@Override
	public long roll() {
		return val;
	}

	@Override
	public long rollSingle() {
		return val;
	}

	@Override
	public String toString() {
		return Long.toString(val);
	}
}