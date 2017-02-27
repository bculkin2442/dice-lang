package bjc.dicelang.v2.dice;

import java.util.Random;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class DiceBox {
	static final Random rng = new Random();

	public static DieExpression parseExpression(String exp) {
		if(!isValidExpression(exp)) return null;

		if(scalarDiePattern.matcher(exp).matches()) {
			Die scal = new ScalarDie(Long.parseLong(exp.substring(0, exp.indexOf('s'))));

			return new DieExpression(scal);
		} else if(simpleDiePattern.matcher(exp).matches()) {
			String[] dieParts = exp.split("d");

			long right = Long.parseLong(dieParts[1]);
			if(dieParts[0].equals("")) {
				Die scal = new SimpleDie(1, right);
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

			Die scal = new CompoundingDie(left.scalar, right, dieParts[1]);
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
			return new DieExpression(lst);
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
