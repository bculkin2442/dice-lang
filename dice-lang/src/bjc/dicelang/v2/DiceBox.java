package bjc.dicelang.v2;

import java.util.Random;
import java.util.regex.Pattern;

public class DiceBox {
	private static final Random rng = new Random();

	public interface Die {
		boolean canOptimize();
		int optimize();

		int roll();
	}

	private static class ScalarDie implements Die {
		private int val;

		public ScalarDie(int vl) {
			val = vl;
		}

		public boolean canOptimize() {
			return true;
		}

		public int optimize() {
			return val;
		}

		public int roll() {
			return val;
		}

		public String toString() {
			return Integer.toString(val);
		}
	}

	private static class SimpleDie implements Die {
		private int numDice;
		private int diceSize;

		public SimpleDie(int nDice, int size) {
			numDice = nDice;
			diceSize = size;
		}

		public boolean canOptimize() {
			if(diceSize == 1) return true;
			else return false;
		}

		public int optimize() {
			return numDice;
		}

		public int roll() {
			int total = 0;

			for(int i = 0; i < numDice; i++) {
				total += rng.nextInt(i) + 1;
			}

			return total;
		}

		public String toString() {
			return numDice + "d" + diceSize;
		}
	}

	private static class CompoundDie implements Die {
		private Die left;
		private Die right;

		public CompoundDie(Die lft, Die rght) {
			left = lft;
			right = rght;
		}

		public boolean canOptimize() {
			return left.canOptimize() && right.canOptimize();
		}

		public int optimize() {
			return left.optimize() + "" + right.optimize();
		}

		public int roll() {
			return Integer.parseInt(left.roll() + "" + right.roll());
		}
	}

	public static Die parseExpression(String exp) {
		if(!isValidExpression(exp)) return null;

		if(scalarDiePattern.matcher(exp).matches()) {
			return new ScalarDie(Integer.parseInt(exp));
		} else if(simpleDiePattern.matcher(exp).matches()) {
			String[] dieParts = exp.split("d");

			if(dieParts[0].equals("")) {
				return new SimpleDie(1, Integer.parseInt(dieParts[1]));
			} else {
				return new SimpleDie(Integer.parseInt(dieParts[0]), Integer.parseInt(dieParts[1]));
			}
		} else if(compoundDiePattern.matcher(exp).matches()) {
			String[] dieParts = exp.split("c");

			return new CompoundDie(parseExpression(dieParts[0]), parseExpression(dieParts[1]));
		}

		return null;
	}

	private static final Pattern scalarDiePattern   = Pattern.compile(
			"[\\+\\-]?\\d+");
	private static final Pattern simpleDiePattern   = Pattern.compile(
			"(?:\\d+)?d\\d+");
	private static final Pattern compoundDiePattern = Pattern.compile(
			simpleDiePattern + "c(?:(?:" + simpleDiePattern + ")|(?:\\d+))";

	public static boolean isValidExpression(String exp) {
		if(scalarDiePattern.matcher(exp).matches()) {
			return true;
		} else if(simpleDiePattern.matcher(exp).matches()) {
			return true;
		} else if (compoundDiePattern.matcher(exp).matches()) {
			return true;
		} else {
			return false;
		}
	}
}
