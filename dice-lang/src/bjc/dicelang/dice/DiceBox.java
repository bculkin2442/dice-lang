package bjc.dicelang.dice;

import java.util.Random;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Contains static methods for producing dice from strings.
 *
 * @author Ben Culkin
 */
public class DiceBox {
	static final Random rng = new Random();

	/**
	 * Parse a die expression from a string.
	 *
	 * @return The die expression from the string, or null if it wasn't one
	 */
	public static DieExpression parseExpression(String exp) {
		/*
		 * Only bother will valid expressions
		 */
		if (!isValidExpression(exp))
			return null;

		if (scalarDiePattern.matcher(exp).matches()) {
			/*
			 * Parse scalar die
			 */
			Die scal = new ScalarDie(Long.parseLong(exp.substring(0, exp.indexOf('s'))));

			return new DieExpression(scal);
		} else if (simpleDiePattern.matcher(exp).matches()) {
			/*
			 * Parse simple die groups
			 */
			String[] dieParts = exp.split("d");

			long right = Long.parseLong(dieParts[1]);
			if (dieParts[0].equals("")) {
				/*
				 * Handle short-form expressions.
				 */
				Die scal = new SimpleDie(1, right);
				return new DieExpression(scal);
			} else {
				Die scal = new SimpleDie(Long.parseLong(dieParts[0]), right);
				return new DieExpression(scal);
			}
		} else if (fudgeDiePattern.matcher(exp).matches()) {
			/*
			 * Parse fudge dice
			 */
			String nDice = exp.substring(0, exp.indexOf('d'));

			return new DieExpression(new FudgeDie(Long.parseLong(nDice)));
		} else if (compoundDiePattern.matcher(exp).matches()) {
			/*
			 * Parse compound die expressions
			 */
			String[] dieParts = exp.split("c");

			DieExpression left = parseExpression(dieParts[0]);
			DieExpression right = parseExpression(dieParts[1]);

			return new DieExpression(new CompoundDie(left.scalar, right.scalar));
		} else if (compoundingDiePattern.matcher(exp).matches()) {
			/*
			 * Parse compounding die expressions
			 */
			String[] dieParts = exp.split("!!");

			DieExpression left = parseExpression(dieParts[0]);
			Predicate<Long> right = deriveCond(dieParts[1]);

			Die scal = new CompoundingDie(left.scalar, right, dieParts[1]);
			return new DieExpression(scal);
		} else if (explodingDiePattern.matcher(exp).matches()) {
			/*
			 * Parse exploding die expressions
			 */
			String[] dieParts = exp.split("!");

			DieExpression left = parseExpression(dieParts[0]);
			Predicate<Long> right = deriveCond(dieParts[1]);

			DieList lst = new ExplodingDice(left.scalar, right, dieParts[1], false);
			return new DieExpression(lst);
		} else if (penetratingDiePattern.matcher(exp).matches()) {
			/*
			 * Parse penetrating die expressions
			 */
			String[] dieParts = exp.split("p!");

			DieExpression left = parseExpression(dieParts[0]);
			Predicate<Long> right = deriveCond(dieParts[1]);

			DieList lst = new ExplodingDice(left.scalar, right, dieParts[1], true);
			return new DieExpression(lst);
		} else if (diceListPattern.matcher(exp).matches()) {
			/*
			 * Parse simple die lists
			 */
			String[] dieParts = exp.split("dl");

			DieExpression left = parseExpression(dieParts[0]);
			DieExpression right = parseExpression(dieParts[1]);

			DieList lst = new SimpleDieList(left.scalar, right.scalar);
			return new DieExpression(lst);
		}

		return null;
	}

	/*
	 * The strings and patterns used for matching
	 */

	/*
	 * Defines a comparison predicate
	 */
	private static final String comparePoint = "[<>=]\\d+";

	/*
	 * Defines a scalar die.
	 *
	 * This is just a number.
	 */
	private static final String scalarDie = "[\\+\\-]?\\d+sd";
	private static final Pattern scalarDiePattern = Pattern.compile("\\A" + scalarDie + "\\Z");

	/*
	 * Defines a simple die.
	 *
	 * This is a group of one or more dice of the same size.
	 */
	private static final String simpleDie = "(?:\\d+)?d\\d+";
	private static final Pattern simpleDiePattern = Pattern.compile("\\A" + simpleDie + "\\Z");

	/*
	 * Defines a fudge die.
	 *
	 * This is like a simple die, but all the die give -1, 0, or 1 as
	 * results.
	 */
	private static final String fudgeDie = "(?:\\d+)?dF";
	private static final Pattern fudgeDiePattern = Pattern.compile("\\A" + fudgeDie + "\\Z");

	/*
	 * Defines a compound die.
	 *
	 * This is like using two d10's to simulate a d100
	 */
	private static final String compoundDie = simpleDie + "c(?:(?:" + simpleDie + ")|(?:\\d+))";
	private static final Pattern compoundDiePattern = Pattern.compile("\\A" + compoundDie + "\\Z");

	/*
	 * Defines a compound group.
	 *
	 * This is used for forming die list type expressions.
	 */
	private static final String compoundGroup = "(?:(?:" + scalarDie + ")|(?:" + simpleDie + ")|(?:" + compoundDie
			+ ")|(?:" + fudgeDie + "))";

	/*
	 * Defines a compounding die.
	 *
	 * This is like an exploding die, but is a single die, not a group of
	 * them.
	 */
	private static final String compoundingDie = compoundGroup + "!!" + comparePoint;
	private static final Pattern compoundingDiePattern = Pattern.compile("\\A" + compoundingDie + "\\Z");

	/*
	 * Defines an exploding die.
	 *
	 * This is a die that you reroll the component of if it meets a certain
	 * condition.
	 */
	private static final String explodingDie = compoundGroup + "!" + comparePoint;
	private static final Pattern explodingDiePattern = Pattern.compile("\\A" + explodingDie + "\\Z");

	/*
	 * Defines a penetrating die.
	 *
	 * This is like an exploding die, but the exploded result gets a -1
	 * penalty.
	 */
	private static final String penetratingDie = compoundGroup + "!" + comparePoint;
	private static final Pattern penetratingDiePattern = Pattern.compile("\\A" + penetratingDie + "\\Z");

	/*
	 * Defines a die list.
	 *
	 * This is an array of dice of the specified size
	 */
	private static final String diceList = compoundGroup + "dl" + compoundGroup;
	private static final Pattern diceListPattern = Pattern.compile("\\A" + diceList + "\\Z");

	/**
	 * Check if a given string is a valid die expression.
	 *
	 * @param exp
	 *                The string to check validity of.
	 *
	 * @return Whether or not the string is a valid command
	 */
	public static boolean isValidExpression(String exp) {
		if (scalarDiePattern.matcher(exp).matches()) {
			return true;
		} else if (simpleDiePattern.matcher(exp).matches()) {
			return true;
		} else if (fudgeDiePattern.matcher(exp).matches()) {
			return true;
		} else if (compoundDiePattern.matcher(exp).matches()) {
			return true;
		} else if (compoundingDiePattern.matcher(exp).matches()) {
			return true;
		} else if (explodingDiePattern.matcher(exp).matches()) {
			return true;
		} else if (penetratingDiePattern.matcher(exp).matches()) {
			return true;
		} else if (diceListPattern.matcher(exp).matches()) {
			return true;
		} else {
			return false;
		}
	}

	/*
	 * Derive a predicate from a compare point
	 */
	private static Predicate<Long> deriveCond(String patt) {
		long num = Long.parseLong(patt.substring(1));

		switch (patt.charAt(0)) {
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
