package bjc.dicelang.v2;

import java.util.Arrays;
import java.util.Random;
import java.util.regex.Pattern;

public class DiceBox {
	private static final Random rng = new Random();

	public interface Die {
		boolean canOptimize();
		int optimize();

		int roll();
	}

	public interface DieList {
		boolean canOptimize();
		int[] optimize();

		int[] roll();
	}

	public static class DieExpression {
		public final boolean isList;

		public Die     scalar;
		public DieList list;

		public DieExpression(Die scal) {
			isList = false;

			scalar = scal;
		}

		public DieExpression(DieList lst) {
			isList = true;

			list   = lst;
		}

		public String toString() {
			if(isList) return list.toString();
			else       return scalar.toString();
		}

		public String value() {
			if(isList) return Arrays.toString(list.roll());
			else       return Integer.toString(scalar.roll());
		}
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
				total += rng.nextInt(diceSize) + 1;
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
			return Integer.parseInt(left.optimize() + "" + right.optimize());
		}

		public int roll() {
			return Integer.parseInt(left.roll() + "" + right.roll());
		}

		public String toString() {
			return left.toString() + "c" + right.toString();
		}
	}

	private static class SimpleDieList implements DieList {
		private Die numDice;
		private Die size;

		public SimpleDieList(Die nDice, Die sze) {
			numDice = nDice;
			size = sze;
		}

		public boolean canOptimize() {
			if(size.canOptimize() && size.optimize() == 1) {
				return numDice.canOptimize();
			} else {
				return false;
			}
		}

		public int[] optimize() {
			int[] ret = new int[numDice.optimize()];

			int optSize = size.optimize();

			for(int i = 0; i < optSize; i++) {
				ret[i] = 1;
			}

			return ret;
		}

		public int[] roll() {
			int num = numDice.roll();

			int[] ret = new int[num];

			for(int i = 0; i < num; i++) {
				ret[i] = size.roll();
			}

			return ret;
		}

		public String toString() {
			return numDice.toString() + "dl" + size.toString();
		}
	}

	public static DieExpression parseExpression(String exp) {
		if(!isValidExpression(exp)) return null;

		if(scalarDiePattern.matcher(exp).matches()) {
			return new DieExpression(new ScalarDie(Integer.parseInt(exp)));
		} else if(simpleDiePattern.matcher(exp).matches()) {
			String[] dieParts = exp.split("d");

			if(dieParts[0].equals("")) {
				return new DieExpression(new SimpleDie(1, Integer.parseInt(dieParts[1])));
			} else {
				return new DieExpression(new SimpleDie(Integer.parseInt(dieParts[0]), Integer.parseInt(dieParts[1])));
			}
		} else if(compoundDiePattern.matcher(exp).matches()) {
			String[] dieParts = exp.split("c");

			DieExpression left  = parseExpression(dieParts[0]);
			DieExpression right = parseExpression(dieParts[1]);

			if(left.isList || right.isList) {
				// @TODO give a specific error message
				return null;
			}

			return new DieExpression(new CompoundDie(left.scalar, right.scalar));
		} else if(diceListPattern.matcher(exp).matches()) {
			String[] dieParts = exp.split("dl");

			DieExpression left  = parseExpression(dieParts[0]);
			DieExpression right = parseExpression(dieParts[1]);

			if(left.isList || right.isList) {
				return null;
			}

			return new DieExpression(new SimpleDieList(left.scalar, right.scalar));
		}

		// @TODO give a specific error message
		return null;
	}

	private static final String  scalarDie          = "[\\+\\-]?\\d+";
	private static final Pattern scalarDiePattern   = Pattern.compile("\\A" + scalarDie + "\\Z");

	private static final String  simpleDie          = "(?:\\d+)?d\\d+";
	private static final Pattern simpleDiePattern   = Pattern.compile("\\A" + simpleDie + "\\Z");

	private static final String  compoundDie        = simpleDie + "c(?:(?:" + simpleDie + ")|(?:\\d+))";
	private static final Pattern compoundDiePattern = Pattern.compile("\\A" + compoundDie + "\\Z");

	private static final String  compoundGroup      = "(?:(?:" + scalarDie + ")|(?:" + simpleDie + ")|(?:"
		+ compoundDie + "))";

	private static final String  diceList           = compoundGroup + "dl" + compoundGroup;
	private static final Pattern diceListPattern    = Pattern.compile("\\A" + diceList + "\\Z");

	public static boolean isValidExpression(String exp) {
		if(scalarDiePattern.matcher(exp).matches()) {
			return true;
		} else if(simpleDiePattern.matcher(exp).matches()) {
			return true;
		} else if (compoundDiePattern.matcher(exp).matches()) {
			return true;
		} else if (diceListPattern.matcher(exp).matches()) {
			return true;
		} else {
			return false;
		}
	}
}
