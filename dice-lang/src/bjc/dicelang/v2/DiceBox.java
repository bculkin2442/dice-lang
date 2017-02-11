package bjc.dicelang.v2;

import java.util.Random;

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

	public static Die parseExpression(String exp) {
		if(!isValidExpression(exp)) return null;

		if(exp.matches(scalarDiePattern)) {
			return new ScalarDie(Integer.parseInt(exp));
		} else if(exp.matches(simpleDiePattern)) {
			String[] dieParts = exp.split("d");

			if(dieParts[0].equals("")) {
				return new SimpleDie(1, Integer.parseInt(dieParts[1]));
			} else {
				return new SimpleDie(Integer.parseInt(dieParts[0]), Integer.parseInt(dieParts[1]));
			}
		}

		return null;
	}

	private static final String scalarDiePattern = "[\\+\\-]?\\d+";
	private static final String simpleDiePattern = "(?:\\d+)?d\\d+";

	public static boolean isValidExpression(String exp) {
		if(exp.matches(scalarDiePattern)) {
			return true;
		} else if(exp.matches(simpleDiePattern)) {
			return true;
		} else {
			return false;
		}
	}
}
