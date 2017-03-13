package bjc.dicelang.v1.examples;

import java.util.Deque;
import java.util.LinkedList;

import bjc.utils.data.IPair;
import bjc.utils.data.Pair;
import bjc.utils.funcdata.FunctionalStringTokenizer;
import bjc.utils.funcdata.IList;
import bjc.utils.funcutils.ListUtils;
import bjc.utils.parserutils.ShuntingYard;

/**
 * Prepare a dice expression to be parsed
 * 
 * @author ben
 *
 */
public class DiceExpressionPreparer {
	/**
	 * The yard to use for shunting expressions
	 */
	private static ShuntingYard<String> yard;

	private static final int MATH_PREC = 20;
	private static final int DICE_PREC = 10;
	private static final int EXPR_PREC = 0;

	// Do initialization for all parsers
	static {
		// The shunter we're going to use
		yard = new ShuntingYard<>(false);

		// Configure the shunters operators
		// Basic mathematical operators
		yard.addOp("+", 0 + MATH_PREC);
		yard.addOp("-", 0 + MATH_PREC);

		yard.addOp("*", 1 + MATH_PREC);
		yard.addOp("/", 1 + MATH_PREC);

		yard.addOp("d", 0 + DICE_PREC); // dice operator: use for
						// creating
		// variable size dice groups
		yard.addOp("c", 1 + DICE_PREC); // compound operator: use for
		// creating compound dice from expressions

		yard.addOp("=>", 0 + EXPR_PREC); // let operator: evaluate an
		// expression in the context of another
		yard.addOp(":=", 1 + EXPR_PREC); // binding operator: Bind a
							// name
		// to a variable expression
	}

	/**
	 * Prepare a command, turning raw tokens into input for the tree builder
	 * 
	 * @param currentLine
	 *                The command to prepare
	 * @return A stream of tokens representing the command
	 */
	public static IList<String> prepareCommand(String currentLine) {
		// Split the command into tokens
		IList<String> tokens = FunctionalStringTokenizer.fromString(currentLine).toList();

		// The linked list to use for handling tokens
		Deque<IPair<String, String>> ops = new LinkedList<>();

		// Prepare the list for operator expansion
		ops.add(new Pair<>("+", "\\+"));
		ops.add(new Pair<>("-", "-"));
		ops.add(new Pair<>("*", "\\*"));
		ops.add(new Pair<>("/", "/"));
		ops.add(new Pair<>(":=", ":="));
		ops.add(new Pair<>("=>", "=>"));

		// Expand infix single tokens to multiple infix tokens
		IList<String> semiExpandedTokens = ListUtils.splitTokens(tokens, ops);

		// Reinitialize the list
		ops = new LinkedList<>();

		// Prepare the list for deaffixation
		ops.add(new Pair<>("(", "\\("));
		ops.add(new Pair<>(")", "\\)"));
		ops.add(new Pair<>("[", "\\["));
		ops.add(new Pair<>("]", "\\]"));

		// Deaffix ('s and ['s from tokens
		IList<String> fullyExpandedTokens = ListUtils.deAffixTokens(semiExpandedTokens, ops);

		// Remove blank tokens
		fullyExpandedTokens.removeIf((strang) -> strang.equals(""));

		// Shunt the tokens, and hand them back
		return yard.postfix(fullyExpandedTokens, (token) -> token);
	}
}
