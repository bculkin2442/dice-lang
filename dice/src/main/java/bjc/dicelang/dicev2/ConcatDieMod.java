package bjc.dicelang.dicev2;

/**
 * Concatentate a series of dice together.
 * @author Ben Culkin
 *
 */
public class ConcatDieMod extends Die {
	/**
	 * The dice to concatenate together.
	 */
	public final Die[] dice;

	/**
	 * Create a new concatenative die pool.
	 * @param dice The pool of dice to concatenate.
	 */
	public ConcatDieMod(Die... dice) {
		super();

		this.dice = dice;
	}

	@Override
	public long[] roll() {
		return new long[] { rollSingle() };
	}

	@Override
	public long rollSingle() {
		StringBuilder sb = new StringBuilder();

		for(Die die : dice) {
			for(long val : die.roll()) {
				sb.append(val);
			}
		}

		return Long.parseLong(sb.toString());
	}

	@Override
	public boolean canOptimize() {
		for(Die die : dice) {
			if(!die.canOptimize()) return false;
		}

		return true;
	}
	
	@Override
	public long optimize() {
		StringBuilder sb = new StringBuilder();

		for(Die die : dice) {
			sb.append(die.optimize());
		}

		return Long.parseLong(sb.toString());
	}
}
