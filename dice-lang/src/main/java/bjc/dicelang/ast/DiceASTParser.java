package bjc.dicelang.ast;

import java.util.Deque;
import java.util.LinkedList;

import org.apache.commons.lang3.StringUtils;

import bjc.utils.data.Pair;
import bjc.utils.funcdata.FunctionalList;
import bjc.utils.funcdata.FunctionalStringTokenizer;
import bjc.utils.funcutils.ListUtils;
import bjc.utils.parserutils.AST;
import bjc.utils.parserutils.ShuntingYard;
import bjc.utils.parserutils.TreeConstructor;

/**
 * Create an AST from a string expression
 * 
 * @author ben
 *
 */
public class DiceASTParser {
	/**
	 * The yard to use for shunting expressions
	 */
	private static ShuntingYard<String> yard;

	static {
		yard = new ShuntingYard<>();

		yard.addOp("d", 5); // dice operator: use for creating variable
		// size dice groups
		yard.addOp("c", 6); // compound operator: use for creating compound
		// dice from expressions
		yard.addOp(":=", 0); // binding operator: Bind a name to a variable
		// expression
	}

	/**
	 * Build an AST from a string expression
	 * 
	 * @param exp
	 *            The string to build from
	 * @return An AST built from the passed in string
	 */
	public AST<IDiceASTNode> buildAST(String exp) {
		FunctionalList<String> tokens =
				FunctionalStringTokenizer.fromString(exp).toList((s) -> s);

		Deque<Pair<String, String>> ops = new LinkedList<>();

		ops.add(new Pair<>("+", "\\+"));
		ops.add(new Pair<>("-", "-"));
		ops.add(new Pair<>("*", "\\*"));
		ops.add(new Pair<>("/", "/"));
		ops.add(new Pair<>(":=", ":="));

		FunctionalList<String> semiExpandedTokens =
				ListUtils.splitTokens(tokens, ops);

		ops = new LinkedList<>();

		ops.add(new Pair<>("(", "\\("));
		ops.add(new Pair<>(")", "\\)"));

		FunctionalList<String> fullyExpandedTokens =
				ListUtils.deAffixTokens(semiExpandedTokens, ops);

		FunctionalList<String> shunted =
				yard.postfix(fullyExpandedTokens, (s) -> s);

		AST<String> rawAST = TreeConstructor.constructTree(shunted,
				this::isOperator, (op) -> false, null);

		AST<IDiceASTNode> bakedAST = rawAST.transmuteAST((tok) -> {
			if (isOperator(tok)) {
				return OperatorDiceNode.fromString(tok);
			} else if (isLiteral(tok)) {
				return new LiteralDiceNode(tok);
			} else {
				return new VariableDiceNode(tok);
			}
		});

		return bakedAST;
	}

	/**
	 * Check if a token represents a literal
	 * 
	 * @param tok
	 *            The token to check
	 * @return Whether or not the token represents a literal
	 */
	private static boolean isLiteral(String tok) {
		if (StringUtils.countMatches(tok, 'c') == 1
				&& !tok.equalsIgnoreCase("c")) {
			return true;
		} else if (StringUtils.countMatches(tok, 'd') == 1
				&& !tok.equalsIgnoreCase("d")) {
			return true;
		} else {
			try {
				Integer.parseInt(tok);
				return true;
			} catch (NumberFormatException nfx) {
				return false;
			}
		}
	}

	/**
	 * Check if a token represents an operator
	 * 
	 * @param tok
	 *            The token to check if it represents an operator
	 * @return Whether or not the token represents an operator
	 */
	private boolean isOperator(String tok) {
		switch (tok) {
			case ":=":
			case "+":
			case "-":
			case "*":
			case "/":
			case "c":
			case "d":
				return true;
			default:
				return false;
		}
	}
}
