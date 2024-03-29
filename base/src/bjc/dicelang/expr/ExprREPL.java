package bjc.dicelang.expr;

import java.util.Scanner;

import bjc.data.Tree;
import bjc.funcdata.ListEx;
import bjc.utils.parserutils.TreeConstructor;

/**
 * REPL for expressions.
 * @author student
 *
 */
public class ExprREPL {
	/**
	 * Main method.
	 *
	 * @param args
	 *        Unused CLI args.
	 */
	public static void main(final String[] args) {
		/* Create our objects. */
		final Tokens toks = new Tokens();
		final Lexer lex = new Lexer();

		/* Prepare our input source. */
		@SuppressWarnings("resource")
		final Scanner scan = new Scanner(System.in);

		/* Read initial command. */
		System.out.print("Enter a math expression (blank line to quit): ");
		String ln = scan.nextLine().trim();

		/* Enter REPL loop. */
		while(!ln.equals("")) {
			/* Print raw command. */
			System.out.println("Raw command: " + ln);
			System.out.println();

			/* Lex command to infix tokens. */
			final Token[] infixTokens = lex.lexString(ln, toks);
			System.out.println("Lexed tokens: ");
			for(final Token tok : infixTokens) {
				System.out.println("\t" + tok);
			}

			/* Print out infix expression. */
			System.out.print("Lexed expression: ");
			for(final Token tok : infixTokens) {
				System.out.print(tok.toExpr() + " ");
			}

			/* Space stages. */
			System.out.println();
			System.out.println();

			/* Shunt infix tokens to postfix tokens. */
			final ListEx<Token> postfixTokens = Shunter.shuntTokens(infixTokens);
			System.out.println("Lexed tokens: ");
			for(final Token tok : postfixTokens) {
				System.out.println("\t" + tok);
			}

			/* Print out postfix tokens. */
			System.out.print("Shunted expression: ");
			for(final Token tok : postfixTokens) {
				System.out.print(tok.toExpr() + " ");
			}

			/* Space stages. */
			System.out.println();
			System.out.println();

			/*
			 * Construct a tree from the list of postfixed tokens.
			 */
			final Tree<Token> ast = TreeConstructor.constructTree(postfixTokens,
					tok -> tok.typ.isOperator);

			/*
			 * Print the tree, then the canonical expression for it.
			 */
			System.out.println("Parsed tree");
			System.out.println(ast.toString());
			System.out.println("\nCanonical expr: " + Parser.toCanonicalExpr(ast));

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

}
