package bjc.dicelang.old.ast;

import java.util.Deque;
import java.util.LinkedList;
import java.util.function.Function;

import bjc.dicelang.old.ast.nodes.IDiceASTNode;
import bjc.dicelang.old.ast.nodes.LiteralDiceNode;
import bjc.dicelang.old.ast.nodes.OperatorDiceNode;
import bjc.dicelang.old.ast.nodes.VariableDiceNode;
import bjc.utils.data.IPair;
import bjc.utils.data.Pair;
import bjc.utils.funcdata.FunctionalStringTokenizer;
import bjc.utils.funcdata.IFunctionalList;
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
	private static final class NodeBaker
			implements Function<String, IDiceASTNode> {
		@Override
		public IDiceASTNode apply(String tok) {
			if (isOperator(tok)) {
				return OperatorDiceNode.fromString(tok);
			} else if (LiteralDiceNode.isLiteral(tok)) {
				return new LiteralDiceNode(tok);
			} else {
				return new VariableDiceNode(tok);
			}
		}
	}

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
	public static AST<IDiceASTNode> buildAST(String exp) {
		IFunctionalList<String> tokens = FunctionalStringTokenizer
				.fromString(exp).toList();

		Deque<IPair<String, String>> ops = new LinkedList<>();

		ops.add(new Pair<>("+", "\\+"));
		ops.add(new Pair<>("-", "-"));
		ops.add(new Pair<>("*", "\\*"));
		ops.add(new Pair<>("/", "/"));
		ops.add(new Pair<>(":=", ":="));

		IFunctionalList<String> semiExpandedTokens = ListUtils
				.splitTokens(tokens, ops);

		ops = new LinkedList<>();

		ops.add(new Pair<>("(", "\\("));
		ops.add(new Pair<>(")", "\\)"));

		IFunctionalList<String> fullyExpandedTokens = ListUtils
				.deAffixTokens(semiExpandedTokens, ops);

		IFunctionalList<String> shunted = yard.postfix(fullyExpandedTokens,
				(s) -> s);

		AST<String> rawAST = TreeConstructor.constructTree(shunted,
				DiceASTParser::isOperator);

		AST<IDiceASTNode> bakedAST = rawAST.transmuteAST(new NodeBaker());

		return bakedAST;
	}

	/**
	 * Check if a token represents an operator
	 * 
	 * @param tok
	 *            The token to check if it represents an operator
	 * @return Whether or not the token represents an operator
	 */
	private static boolean isOperator(String tok) {
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
