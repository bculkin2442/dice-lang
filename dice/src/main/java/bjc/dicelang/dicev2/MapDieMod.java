package bjc.dicelang.dicev2;

import java.util.function.LongUnaryOperator;

/**
 * Die pool which executes a mapping on the result.
 * 
 * @author Ben Culkin
 *
 */
public class MapDieMod extends Die {
	/**
	 * The die pool.
	 */
	public final Die die;

	/**
	 * The operator on the result.
	 */
	public final LongUnaryOperator map;

	/**
	 * Create a new mapping die pool.
	 * 
	 * @param map The operation to do on the result.
	 * @param die The die pool.
	 */
	public MapDieMod(LongUnaryOperator map, Die die) {
		super();

		this.die = die;
		this.map = map;
	}

	@Override
	public long[] roll() {
		long[] res = die.roll();

		for(int i = 0; i < res.length; i++) {
			res[i] = map.applyAsLong(res[i]);
		}

		return res;
	}

	@Override
	public long rollSingle() {
		return map.applyAsLong(die.rollSingle());
	}

	/* :UnoptimizableDice */
	@Override
	public boolean canOptimize() {
		return false;
	}

	@Override
	public long optimize() {
		throw new UnsupportedOperationException("Mapped dice can't be optimized");
	}
}
