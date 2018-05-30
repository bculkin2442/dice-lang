package bjc.dicelang.dicev2;

import java.util.Random;

public class Dies {
	public static Die scalar(int val) {
		return new ScalarDie(val);
	}

	public static Die scalar(Random rnd, int val) {
		return new ScalarDie(rnd, val);
	}

	public static Die polyhedral(int dice, int sides) {
		return new PolyhedralDie(dice, sides);
	}

	public static Die polyhedral(Random rnd, int dice, int sides) {
		return new PolyhedralDie(rnd, dice, sides);
	}

	public static Die fudge(int dice) {
		return new FudgeDie(dice);
	}

	public static Die fudge(Random rnd, int dice) {
		return new FudgeDie(rnd, dice);
	}
}
