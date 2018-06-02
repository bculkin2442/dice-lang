package bjc.dicelang.dicev2;

import java.util.Random;
import java.util.function.IntSupplier;

public class Dies {
	public static Die scalar(long val) {
		return new ScalarDie(val);
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

	public static Die composite(Die numDice, Die numSides) {
		return new CompositeDie(numDice, numSides);
	}

	public static Die composite(Die numDice, Die numSides, boolean rerollSides) {
		return new CompositeDie(numDice, numSides, rerollSides);
	}

	public static Die composite(Random rnd, Die numDice, Die numSides) {
		return new CompositeDie(rnd, numDice, numSides);
	}

	public static Die composite(Random rnd, Die numDice, Die numSides, boolean rerollSides) {
		return new CompositeDie(rnd, numDice, numSides, rerollSides);
	}

	public static Die computed(IntSupplier numDice, IntSupplier numSides) {
		return new ComputedDie(numDice, numSides);
	}

	public static Die computed(IntSupplier numDice, IntSupplier numSides, boolean rerollSides) {
		return new ComputedDie(numDice, numSides, rerollSides);
	}

	public static Die computed(Random rnd, IntSupplier numDice, IntSupplier numSides) {
		return new ComputedDie(rnd, numDice, numSides);
	}

	public static Die computed(Random rnd, IntSupplier numDice, IntSupplier numSides, boolean rerollSides) {
		return new ComputedDie(rnd, numDice, numSides, rerollSides);
	}
}
