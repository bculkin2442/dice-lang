package bjc.dicelang.examples;

import java.util.Deque;
import java.util.LinkedList;

import bjc.utils.data.IPair;
import bjc.utils.data.Pair;
import bjc.utils.funcdata.FunctionalStringTokenizer;
import bjc.utils.funcdata.IFunctionalList;
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
	private static ShuntingYard<String>	yard;

	private static final int			MATH_PREC	= 20;
	private static final int			DICE_PREC	= 10;
	private static final int			EXPR_PREC	= 0;

	static {
		yard = new ShuntingYard<>(false);

		// Basic mathematical operators
		yard.addOp("+", 0 + MATH_PREC);
		yard.addOp("-", 0 + MATH_PREC);

		yard.addOp("*", 1 + MATH_PREC);
		yard.addOp("/", 1 + MATH_PREC);

		yard.addOp("d", 0 + DICE_PREC); // dice operator: use for creating
		// variable size dice groups
		yard.addOp("c", 1 + DICE_PREC); // compound operator: use for
		// creating compound dice from expressions

		yard.addOp("=>", 0 + EXPR_PREC); // let operator: evaluate an
		// expression in the context of another
		yard.addOp(":=", 1 + EXPR_PREC); // binding operator: Bind a name
		// to a variable expression
	}

	static IFunctionalList<String> prepareCommand(String currentLine) {
		IFunctionalList<String> tokens =
				FunctionalStringTokenizer.fromString(currentLine).toList();

		Deque<IPair<String, String>> ops = new LinkedList<>();

		ops.add(new Pair<>("+", "\\+"));
		ops.add(new Pair<>("-", "-"));
		ops.add(new Pair<>("*", "\\*"));
		ops.add(new Pair<>("/", "/"));
		ops.add(new Pair<>(":=", ":="));
		ops.add(new Pair<>("=>", "=>"));

		IFunctionalList<String> semiExpandedTokens =
				ListUtils.splitTokens(tokens, ops);

		ops = new LinkedList<>();

		ops.add(new Pair<>("(", "\\("));
		ops.add(new Pair<>(")", "\\)"));
		ops.add(new Pair<>("[", "\\["));
		ops.add(new Pair<>("]", "\\]"));

		IFunctionalList<String> fullyExpandedTokens =
				ListUtils.deAffixTokens(semiExpandedTokens, ops);

		fullyExpandedTokens.removeIf((strang) -> strang.equals(""));

		return yard.postfix(fullyExpandedTokens, (token) -> token);
	}
}
