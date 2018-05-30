package bjc.dicelang.dicev2;

import java.util.Random;

public abstract class Die {
	private static final Random BASE = new Random();

	protected Random rng;

	protected Die() {
		rng = BASE;
	}

	protected Die(Random rnd) {
		rng = rnd;
	}

	public void setRandom(Random rnd) {
		rng = rnd;
	}

	public abstract long[] roll();
	public abstract long   rollSingle();

	public abstract boolean canOptimize();
	public abstract long    optimize();
}
