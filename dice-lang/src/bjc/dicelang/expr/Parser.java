package bjc.dicelang.expr;

import java.util.Arrays;
import java.util.Scanner;

import bjc.utils.data.ITree;
import bjc.utils.funcdata.FunctionalList;
import bjc.utils.parserutils.TreeConstructor;

/**
 * Parser for simple math expressions.
 *
 * @author Ben Culkin
 */
public class Parser {
	/*
	 * @TODO 10/08/17 Ben Culkin :MainSeperation
	 * 	This main method should be moved to its own class.
	 */
	/**
	 * Main method.
	 *
	 * @param args
	 *                Unused CLI args.
	 */
	public static void main(final String[] args) {
		/* Create our objects. */
		final Tokens toks = new Tokens();
		final Lexer lex = new Lexer();

		/* Prepare our input source. */
		final Scanner scan = new Scanner(System.in);

		/* Read initial command. */
		System.out.print("Enter a math expression (blank line to quit): ");
		String ln = scan.nextLine().trim();

		/* Enter REPL loop. */
		while (!ln.equals("")) {
			/* Print raw command. */
			System.out.println("Raw command: " + ln);
			System.out.println();

			/* Lex command to infix tokens. */
			final Token[] infixTokens = lex.lexString(ln, toks);
			System.out.println("Lexed tokens: ");
			for (final Token tok : infixTokens) {
				System.out.println("\t" + tok);
			}

			/* Print out infix expression. */
			System.out.print("Lexed expression: ");
			for (final Token tok : infixTokens) {
				System.out.print(tok.toExpr() + " ");
			}

			/* Space stages. */
			System.out.println();
			System.out.println();

			/* Shunt infix tokens to postfix tokens. */
			final Token[] postfixTokens = Shunter.shuntTokens(infixTokens);
			System.out.println("Lexed tokens: ");
			for (final Token tok : postfixTokens) {
				System.out.println("\t" + tok);
			}

			/* Print out postfix tokens. */
			System.out.print("Shunted expression: ");
			for (final Token tok : postfixTokens) {
				System.out.print(tok.toExpr() + " ");
			}

			/* Space stages. */
			System.out.println();
			System.out.println();

			/* Construct a list from the array of tokens. */
			final FunctionalList<Token> tokList = new FunctionalList<>(
					Arrays.asList(postfixTokens));

			/* Construct a tree from the list of postfixed tokens. */
			final ITree<Token> ast = TreeConstructor.constructTree(tokList,
					tok -> tok.typ.isOperator);

			/* Print the tree, then the canonical expression for it. */
			System.out.println("Parsed tree");
			System.out.println(ast.toString());
			System.out.println("\nCanonical expr: " + toCanonicalExpr(ast));

			/* Space stages. */
			System.out.println();
			System.out.println();

			/* Prompt for a new expression. */
			System.out.print("Enter a math expression (blank line to quit): ");
			/* Read it. */
			ln = scan.nextLine().trim();
		}

		/* Cleanup after ourselves. */
		scan.close();
	}

	/*
	 * Convert an expression to one that uses the smallest necessary amount
	 * of parens.
	 */
	private static String toCanonicalExpr(final ITree<Token> ast) {
		final Token data = ast.getHead();

		if (ast.getChildrenCount() == 0) {
			/* Handle leaf nodes. */
			return data.toExpr();
		}

		/* The left/right children. */
		final ITree<Token> left  = ast.getChild(0);
		final ITree<Token> right = ast.getChild(1);

		/* Recursively canonicalize them. */
		String leftExpr  = toCanonicalExpr(left);
		String rightExpr = toCanonicalExpr(right);

		/* Add parens if the left was higher priority. */
		if (left.getChildrenCount() == 0) {
			int leftPriority = left.getHead().typ.operatorPriority;
			int dataPriority = data.typ.operatorPriority;

			if (leftPriority >= dataPriority) {
				leftExpr = String.format("(%s)", leftExpr);
			}
		}

		/* Add parens if the right was higher priority. */
		if (right.getChildrenCount() == 0) {
			int rightPriority = right.getHead().typ.operatorPriority;
			int dataPriority  = data.typ.operatorPriority;

			if (rightPriority >= dataPriority) {
				rightExpr = String.format("(%s)", rightExpr);
			}
		}

		return String.format("%s %s %s", leftExpr, data.toExpr(), rightExpr);
	}
}
