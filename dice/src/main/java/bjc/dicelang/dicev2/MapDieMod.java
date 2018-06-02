package bjc.dicelang.dicev2;

import java.util.function.LongUnaryOperator;

public class MapDieMod extends Die {
	public final Die die;

	public final LongUnaryOperator map;

	public MapDieMod(LongUnaryOperator map, Die die) {
		super();

		this.die = die;
		this.map = map;
	}

	public long[] roll() {
		long[] res = die.roll();

		for(int i = 0; i < res.length; i++) {
			res[i] = map.applyAsLong(res[i]);
		}

		return res;
	}

	public long rollSingle() {
		return map.applyAsLong(die.rollSingle());
	}

	/* :UnoptimizableDice */
	public boolean canOptimize() {
		return false;
	}

	public long optimize() {
		throw new UnsupportedOperationException("Mapped dice can't be optimized");
	}
}
