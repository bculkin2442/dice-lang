package bjc.dicelang.v2;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class DiceBox {
	private static final Random rng = new Random();

	public interface Die {
		boolean canOptimize();
		long    optimize();

		long roll();
		long rollSingle();
	}

	public interface DieList {
		boolean canOptimize();
		long[]  optimize();

		long[] roll();
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
			else       return Long.toString(scalar.roll());
		}
	}

	public static class ScalarDie implements Die {
		private long val;

		public ScalarDie(long vl) {
			val = vl;
		}

		public boolean canOptimize() {
			return true;
		}

		public long optimize() {
			return val;
		}

		public long roll() {
			return val;
		}

		public long rollSingle() {
			return val;
		}

		public String toString() {
			return Long.toString(val);
		}
	}

	public static class SimpleDie implements Die {
		private Die numDice;
		private Die diceSize;

		public SimpleDie(long nDice, long size) {
			numDice  = new ScalarDie(nDice);
			diceSize = new ScalarDie(size);
		}

		public SimpleDie(Die nDice, long size) {
			numDice  = nDice;
			diceSize = new ScalarDie(size);
		}

		public SimpleDie(long nDice, Die size) {
			numDice  = new ScalarDie(nDice);
			diceSize = size;
		}

		public SimpleDie(Die nDice, Die size) {
			numDice  = nDice;
			diceSize = size;
		}

		public boolean canOptimize() {
			if(diceSize.canOptimize() && (diceSize.optimize() <= 1) {
				return numDice.canOptimize();
			} else return false;
		}

		public long optimize() {
			long optSize = diceSize.optimize();

			if(optSize == 0) return 0;
			else             return numDice.optimize();
		}

		public long roll() {
			long total = 0;

			long nDice = numDice.roll();
			long dSize = diceSize.roll();

			for(int i = 0; i < nDice; i++) {
				total += (Math.abs(rng.nextLong()) % dSize) + 1;
			}

			return total;
		}

		public long rollSingle() {
			return (Math.abs(rng.nextLong()) % diceSize.roll()) + 1;
		}

		public String toString() {
			return numDice + "d" + diceSize;
		}
	}

	public static class FudgeDie implements Die {
		private Die numDice;

		public FudgeDie(long nDice) {
			numDice = new ScalarDie(nDice);
		}

		public boolean canOptimize() {
			return numDice.canOptimize() && numDice.optimize() == 0;
		}

		public long optimize() {
			return 0;
		}

		public long roll() {
			long res = 0;
			
			long nDice = numDice.roll();

			for(int i = 0; i < nDice; i++) {
				res += rollSingle();
			}

			return res;
		}

		public long rollSingle() {
			return rng.nextInt(3) - 1;
		}

		public String toString() {
			return numDice + "dF";
		}
	}

	public static class CompoundDie implements Die {
		private Die left;
		private Die right;

		public CompoundDie(Die lft, Die rght) {
			left = lft;
			right = rght;
		}

		public boolean canOptimize() {
			return left.canOptimize() && right.canOptimize();
		}

		public long optimize() {
			return Long.parseLong(left.optimize() + "" + right.optimize());
		}

		public long roll() {
			return Long.parseLong(left.roll() + "" + right.roll());
		}

		public long rollSingle() {
			return roll();
		}

		public String toString() {
			return left.toString() + "c" + right.toString();
		}
	}

	public static class CompoundingDie implements Die {
		private Die source;

		private Predicate<Long> compoundOn;
		private String          compoundPattern;

		public CompoundingDie(Die src, Predicate<Long> compound) {
			this(src, compound, null);
		}

		public CompoundingDie(Die src, Predicate<Long> compound, String patt) {
			source = src;

			compoundOn      = compound;
			compoundPattern = patt;
		}

		public boolean canOptimize() {
			return source.canOptimize() && source.optimize() == 0;
		}

		public long optimize() {
			return 0;
		}

		public long roll() {
			long res = source.roll();
			long oldRes = res;

			while(compoundOn.test(oldRes)) {
				oldRes = source.rollSingle();

				res += oldRes;
			}

			return res;
		}

		public long rollSingle() {
			long res = source.rollSingle();
			long oldRes = res;

			while(compoundOn.test(oldRes)) {
				oldRes = source.rollSingle();

				res += oldRes;
			}

			return res;
		}

		public String toString() {
			if(compoundPattern == null) {
				return source + "!!";
			} else {
				return source + "!!" + compoundPattern;
			}
		}
	}

	public static class MathDie implements Die {
		public static enum MathOp {
			ADD, SUBTRACT, MULTIPLY;

			public String toString() {
				switch(this) {
				case ADD:
					return "+";
				case SUBTRACT:
					return "-";
				case MULTIPLY:
					return "*";
				default:
					return this.name();
				}
			}
		}

		private MathOp type;

		private Die left;
		private Die right;

		public MathDie(MathOp op, Die lft, Die rght) {
			type = op;

			left  = lft;
			right = rght;
		}

		public boolean canOptimize() {
			return left.canOptimize() && right.canOptimize();
		}

		private long performOp(long lft, long rght) {
			switch(type) {
			case ADD:
				return lft + rght;
			case SUBTRACT:
				return lft - rght;
			case MULTIPLY:
				return lft * rght;
			default:
				return 0;
			}
		}

		public long optimize() {
			long lft  = left.optimize();
			long rght = right.optimize();

			return performOp(lft, rght);
		}

		public long roll() {
			long lft  = left.roll();
			long rght = right.roll();

			return performOp(lft, rght);
		}

		public long rollSingle() {
			long lft  = left.rollSingle();
			long rght = right.rollSingle();

			return performOp(lft, rght);
		}

		public String toString() {
			return left.toString() + " " + type.toString() + " " + right.toString();
		}
	}

	public static class SimpleDieList implements DieList {
		private Die numDice;
		private Die size;

		public SimpleDieList(Die nDice, Die sze) {
			numDice = nDice;
			size = sze;
		}

		public boolean canOptimize() {
			if(size.canOptimize() && size.optimize() <= 1) {
				return numDice.canOptimize();
			} else {
				return false;
			}
		}

		public long[] optimize() {
			int sze  = (int)numDice.optimize();
			long res = size.optimize();

			long[] ret = new long[sze];

			for(int i = 0; i < sze; i++) {
				ret[i] = res;
			}

			return ret;
		}

		public long[] roll() {
			int num = (int)numDice.roll();

			long[] ret = new long[num];

			for(int i = 0; i < num; i++) {
				ret[i] = size.roll();
			}

			return ret;
		}

		public String toString() {
			return numDice.toString() + "dl" + size.toString();
		}
	}

	public static class ExplodingDice implements DieList {
		private Die             source;

		private Predicate<Long> explodeOn;
		private String          explodePattern;
		private boolean         explodePenetrates;

		public ExplodingDice(Die src, Predicate<Long> explode) {
			this(src, explode, null, false);
		}

		public ExplodingDice(Die src, Predicate<Long> explode, boolean penetrate) {
			this(src, explode, null, penetrate);
		}

		public ExplodingDice(Die src, Predicate<Long> explode, String patt,
				boolean penetrate) {
			source            = src;
			explodeOn         = explode;
			explodePattern    = patt;
			explodePenetrates = penetrate;
		}

		public boolean canOptimize() {
			return false;
		}

		public long[] optimize() {
			return new long[0];
		}

		public long[] roll() {
			long res = source.roll();
			long oldRes = res;

			List<Long> resList = new LinkedList<>();

			while(explodeOn.test(oldRes)) {
				oldRes = source.rollSingle();

				if(explodePenetrates) oldRes -= 1;
				resList.add(oldRes);
			}

			long[] newRes = new long[resList.size() + 1];
			newRes[0] = res;

			int i = 1;
			for(long rll : resList) {
				newRes[i] = rll;
				i         += 1;
			}

			return newRes;
		}

		public String toString() {
			if(explodePattern == null) {
				return source + (explodePenetrates ? "p" : "") + "!";
			} else {
				return source + (explodePenetrates ? "p" : "") + "!" + explodePattern;
			}
		}
	}

	public static DieExpression parseExpression(String exp) {
		if(!isValidExpression(exp)) return null;

		if(scalarDiePattern.matcher(exp).matches()) {
			Die scal = new ScalarDie(Long.parseLong(exp.substring(0, exp.indexOf('s'))))

			return new DieExpression(scal);
		} else if(simpleDiePattern.matcher(exp).matches()) {
			String[] dieParts = exp.split("d");

			long right = Long.parseLong(dieParts[1]);
			if(dieParts[0].equals("")) {
				Die scal = new SimpleDie(1, right)
				return new DieExpression(scal);
			} else {
				Die scal = new SimpleDie(Long.parseLong(dieParts[0]), right);
				return new DieExpression(scal);
			}
		} else if(fudgeDiePattern.matcher(exp).matches()) {
			String nDice = exp.substring(0, exp.indexOf('d'));

			return new DieExpression(new FudgeDie(Long.parseLong(nDice)));
		} else if(compoundDiePattern.matcher(exp).matches()) {
			String[] dieParts = exp.split("c");

			DieExpression left  = parseExpression(dieParts[0]);
			DieExpression right = parseExpression(dieParts[1]);

			return new DieExpression(new CompoundDie(left.scalar, right.scalar));
		} else if(compoundingDiePattern.matcher(exp).matches()) {
			String[] dieParts = exp.split("!!");

			DieExpression   left  = parseExpression(dieParts[0]);
			Predicate<Long> right = deriveCond(dieParts[1]);

			Die scal = new CompoundingDie(left.scalar, right, dieParts[1])
			return new DieExpression(scal);
		} else if(explodingDiePattern.matcher(exp).matches()) {
			String[] dieParts = exp.split("!");

			DieExpression   left  = parseExpression(dieParts[0]);
			Predicate<Long> right = deriveCond(dieParts[1]);

			DieList lst = new ExplodingDice(left.scalar, right, dieParts[1], false);
			return new DieExpression(lst);
		} else if(penetratingDiePattern.matcher(exp).matches()) {
			String[] dieParts = exp.split("p!");

			DieExpression   left  = parseExpression(dieParts[0]);
			Predicate<Long> right = deriveCond(dieParts[1]);

			DieList lst = new ExplodingDice(left.scalar, right, dieParts[1], true);
			return new DieExpression(lst);
		} else if(diceListPattern.matcher(exp).matches()) {
			String[] dieParts = exp.split("dl");

			DieExpression left  = parseExpression(dieParts[0]);
			DieExpression right = parseExpression(dieParts[1]);
			
			DieList lst = new SimpleDieList(left.scalar, right.scalar);
			return new DieExpression();
		}

		// @TODO give a specific error message
		return null;
	}

	private static final String comparePoint         = "[<>=]\\d+";

	private static final String  scalarDie          = "[\\+\\-]?\\d+sd";
	private static final Pattern scalarDiePattern   = Pattern.compile("\\A" + scalarDie + "\\Z");

	private static final String  simpleDie           = "(?:\\d+)?d\\d+";
	private static final Pattern simpleDiePattern    = Pattern.compile("\\A" + simpleDie + "\\Z");

	private static final String  fudgeDie            = "(?:\\d+)?dF";
	private static final Pattern fudgeDiePattern     = Pattern.compile("\\A" + fudgeDie + "\\Z");

	private static final String  compoundDie         = simpleDie + "c(?:(?:" + simpleDie + ")|(?:\\d+))";
	private static final Pattern compoundDiePattern  = Pattern.compile("\\A" + compoundDie + "\\Z");

	private static final String  compoundGroup       = "(?:(?:" + scalarDie + ")|(?:" + simpleDie + ")|(?:"
		+ compoundDie + ")|(?:" + fudgeDie +"))";

	private static final String  compoundingDie        = compoundGroup + "!!" + comparePoint;
	private static final Pattern compoundingDiePattern = Pattern.compile("\\A" + compoundingDie + "\\Z");

	private static final String  explodingDie        = compoundGroup + "!" + comparePoint;
	private static final Pattern explodingDiePattern = Pattern.compile("\\A" + explodingDie + "\\Z");

	private static final String  penetratingDie        = compoundGroup + "!" + comparePoint;
	private static final Pattern penetratingDiePattern = Pattern.compile("\\A" + penetratingDie + "\\Z");

	private static final String  diceList            = compoundGroup + "dl" + compoundGroup;
	private static final Pattern diceListPattern     = Pattern.compile("\\A" + diceList + "\\Z");

	public static boolean isValidExpression(String exp) {
		if(scalarDiePattern.matcher(exp).matches()) {
			return true;
		} else if(simpleDiePattern.matcher(exp).matches()) {
			return true;
		} else if(fudgeDiePattern.matcher(exp).matches()) {
			return true;
		} else if(compoundDiePattern.matcher(exp).matches()) {
			return true;
		} else if(compoundingDiePattern.matcher(exp).matches()) {
			return true;
		} else if(explodingDiePattern.matcher(exp).matches()) {
			return true;
		} else if(penetratingDiePattern.matcher(exp).matches()) {
			return true;
		} else if (diceListPattern.matcher(exp).matches()) {
			return true;
		} else {
			return false;
		}
	}

	private static Predicate<Long> deriveCond(String patt) {
		long            num  = Long.parseLong(patt.substring(1));

		switch(patt.charAt(0)) {
		case '<':
			return (roll) -> roll < num;
		case '=':
			return (roll) -> roll == num;
		case '>':
			return (roll) -> roll > num;
		default:
			return (roll) -> false;
		}
	}
}
