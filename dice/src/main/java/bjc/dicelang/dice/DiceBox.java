package bjc.dicelang.dice;

import bjc.utils.funcutils.StringUtils;

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
	 * @param expString
	 *        The string to parse.
	 *
	 * @return The die expression from the string, or null if it wasn't one
	 */
	public static DiceExpression parseExpression(final String expString) {
		try {
			return doParseExpression(expString);
		} catch(Exception ex) {
			/*
			 * @TODO 10/08/17 Ben Culkin :DieErrors
			 * 
			 * :ErrorRefactor
			 * 
			 * Use different types of exceptions to provide better
			 * error messages.
			 */
			String exMessage = ex.getMessage();

			System.out.printf("ERROR: Could not parse die expression (Cause: %s)\n", exMessage);
			ex.printStackTrace();

			return null;
		}
	}

	private static DiceExpression doParseExpression(final String expString) {
		/* Only bother with valid expressions. */
		if(!isValidExpression(expString)) {
			return null;
		}

		if(scalarDiePattern.matcher(expString).matches()) {
			/* Parse scalar die. */
			final String dieString = StringUtils.substringTo(expString, "s");

			final long lar = Long.parseLong(dieString);

			final Die scal = new ScalarDie(lar);

			return new ScalarDiceExpression(scal);
		} else if(simpleDiePattern.matcher(expString).matches()) {
			/* Parse simple die groups. */
			final String[] dieParts = expString.split("d");

			final long right = Long.parseLong(dieParts[1]);
			final long left;

			if(dieParts[0].equals("")) {
				/* Handle short-form expressions. */
				left = 1;
			} else {
				left = Long.parseLong(dieParts[0]);
			}

			final Die scal = new SimpleDie(left, right);

			return new ScalarDiceExpression(scal);
		} else if(fudgeDiePattern.matcher(expString).matches()) {
			/* Parse fudge dice. */
			final String nDice = StringUtils.substringTo(expString, "d");
			final Die fudge = new FudgeDie(Long.parseLong(nDice));

			return new ScalarDiceExpression(fudge);
		} else if(compoundDiePattern.matcher(expString).matches()) {
			/* Parse compound die expressions. */
			final String[] dieParts = expString.split("c");

			/*
			 * @TODO 10/08/17 :SplitParse
			 * 
			 * Should this split string/parse split parts be
			 * abstracted into something else that handles doing the
			 * splitting correctly, as well as making sure that the
			 * resulting DieExpressions are of the right type?
			 */
			final DiceExpression left = parseExpression(dieParts[0]);
			final DiceExpression right = parseExpression(dieParts[1]);

			/* :ErrorRefactor */
			if(left.isList()) {
				System.out.printf(
						"ERROR: Expected a scalar dice expression for lhs of compound die, got a list expression instead (%s)\n",
						left);
			} else if(right.isList()) {
				System.out.printf(
						"ERROR: Expected a scalar dice expression for rhs of compound die, got a list expression instead (%s)\n",
						right);
			}

			Die lhs = ((ScalarDiceExpression) left).scalar;
			Die rhs = ((ScalarDiceExpression) right).scalar;

			final Die compound = new CompoundDie(lhs, rhs);

			return new ScalarDiceExpression(compound);
		} else if(compoundingDiePattern.matcher(expString).matches()) {
			/* Parse compounding die expressions. */
			final String[] dieParts = expString.split("!!");

			final DiceExpression left = parseExpression(dieParts[0]);
			final Predicate<Long> right = deriveCond(dieParts[1]);

			Die die = ((ScalarDiceExpression) left).scalar;

			final Die scal = new CompoundingDie(die, right, dieParts[1]);

			return new ScalarDiceExpression(scal);
		} else if(explodingDiePattern.matcher(expString).matches()) {
			/* Parse exploding die expressions. */
			final String[] dieParts = expString.split("!");

			final DiceExpression left = parseExpression(dieParts[0]);
			final Predicate<Long> right = deriveCond(dieParts[1]);

			Die lhs = ((ScalarDiceExpression) left).scalar;

			final DieList lst = new ExplodingDice(lhs, right, dieParts[1], false);

			return new ListDiceExpression(lst);
		} else if(penetratingDiePattern.matcher(expString).matches()) {
			/* Parse penetrating die expressions. */
			final String[] dieParts = expString.split("p!");

			final DiceExpression left = parseExpression(dieParts[0]);
			final Predicate<Long> right = deriveCond(dieParts[1]);

			Die lhs = ((ScalarDiceExpression) left).scalar;

			final DieList lst = new ExplodingDice(lhs, right, dieParts[1], true);

			return new ListDiceExpression(lst);
		} else if(diceListPattern.matcher(expString).matches()) {
			/* Parse simple die lists. */
			final String[] dieParts = expString.split("dl");

			final DiceExpression left = parseExpression(dieParts[0]);
			final DiceExpression right = parseExpression(dieParts[1]);

			Die lhs = ((ScalarDiceExpression) left).scalar;
			Die rhs = ((ScalarDiceExpression) right).scalar;

			final DieList lst = new SimpleDieList(lhs, rhs);

			return new ListDiceExpression(lst);
		}

		/* Unhandled type of die expression. */
		System.out.printf("INTERNAL ERROR: Valid die expression '%s' not parsed\n", expString);
		return null;
	}

	/* The strings and patterns used for matching. */
	/*
	 * @TODO 10/08/17 Ben Culkin :RegexResource
	 * 
	 * These regexes and patterns should be moved to something external,
	 * probably using the SimpleProperties-based system that BJC-Utils2
	 * uses.
	 */
	/* Defines a comparison predicate. */
	private static final String comparePoint = "[<>=]\\d+";

	/*
	 * Defines a scalar die.
	 *
	 * This is just a number.
	 */
	private static final String scalarDie = "[\\+\\-]?\\d+sd";
	private static final Pattern scalarDiePattern = Pattern.compile(String.format("\\A%s\\Z", scalarDie));

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
	 * This is an array of dice of the specified size.
	 */
	private static final String diceList = compoundGroup + "dl" + compoundGroup;
	private static final Pattern diceListPattern = Pattern.compile("\\A" + diceList + "\\Z");

	/**
	 * Check if a given string is a valid die expression.
	 *
	 * @param exp
	 *        The string to check validity of.
	 *
	 * @return Whether or not the string is a valid command.
	 */
	public static boolean isValidExpression(final String exp) {
		/*
		 * @NOTE Should this matcher/matches expression be abstracted in
		 * some way?
		 */
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
		} else if(diceListPattern.matcher(exp).matches()) {
			return true;
		} else {
			return false;
		}
	}

	/* Derive a predicate from a compare point */
	private static Predicate<Long> deriveCond(final String patt) {
		final long num = Long.parseLong(patt.substring(1));

		/*
		 * @NOTE Should this be extended in some way, to provide other
		 * operators?
		 */
		switch(patt.charAt(0)) {
		case '<':
			return (roll) -> (roll < num);
		case '=':
			return (roll) -> (roll == num);
		case '>':
			return (roll) -> (roll > num);
		default:
			return (roll) -> (false);
		}
	}
}
