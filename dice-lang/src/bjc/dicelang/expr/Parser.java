package bjc.dicelang.expr;

import bjc.utils.data.ITree;
import bjc.utils.funcdata.FunctionalList;
import bjc.utils.parserutils.TreeConstructor;

import java.util.Arrays;
import java.util.Scanner;

/**
 * Parser for simple math expressions.
 *
 * @author Ben Culkin
 */
public class Parser {
	/**
	 * Main method.
	 *
	 * @param args
	 *                Unused CLI args.
	 */
	public static void main(String[] args) {
		/*
		 * Create our objects.
		 */
		Tokens toks = new Tokens();
		Lexer lex = new Lexer();

		/*
		 * Prepare our input.
		 */
		Scanner scan = new Scanner(System.in);

		/*
		 * Read initial command.
		 */
		System.out.print("Enter a math expression (blank line to quit): ");
		String ln = scan.nextLine().trim();

		/*
		 * Enter REPL loop.
		 */
		while(!ln.equals("")) {
			/*
			 * Print raw command.
			 */
			System.out.println("Raw command: " + ln);
			System.out.println();

			/*
			 * Lex command to infix tokens.
			 */
			Token[] infixTokens = lex.lexString(ln, toks);
			System.out.println("Lexed tokens: ");
			for(Token tok : infixTokens) {
				System.out.println("\t" + tok);
			}

			/*
			 * Print out infix expression.
			 */
			System.out.print("Lexed expression: ");
			for(Token tok : infixTokens) {
				System.out.print(tok.toExpr() + " ");
			}
			System.out.println();
			System.out.println();

			/*
			 * Shunt infix tokens to postfix tokens.
			 */
			Token[] postfixTokens = Shunter.shuntTokens(infixTokens);
			System.out.println("Lexed tokens: ");
			for(Token tok : postfixTokens) {
				System.out.println("\t" + tok);
			}

			/*
			 * Print out postfix tokens.
			 */
			System.out.print("Shunted expression: ");
			for(Token tok : postfixTokens) {
				System.out.print(tok.toExpr() + " ");
			}
			System.out.println();
			System.out.println();

			FunctionalList<Token> tokList = new FunctionalList<>(Arrays.asList(postfixTokens));
			ITree<Token> ast = TreeConstructor.constructTree(tokList, tok -> tok.type.isOperator);

			/*
			 * Print the tree, then the canonical expression for it.
			 */
			System.out.println("Parsed tree");
			System.out.println(ast.toString());
			System.out.println("\nCanonical expr: " + toCanonicalExpr(ast));

			System.out.println();

			/*
			 * Prompt for a new expression.
			 */
			System.out.print("Enter a math expression (blank line to quit): ");
			ln = scan.nextLine().trim();
		}

		/*
		 * Cleanup after ourselves.
		 */
		scan.close();
	}

	/*
	 * Convert an expression to one that uses the smallest necessary amount
	 * of parens.
	 */
	private static String toCanonicalExpr(ITree<Token> ast) {
		Token data = ast.getHead();

		if(ast.getChildrenCount() == 0)
			/*
			 * Handle leaf nodes.
			 */
			return data.toExpr();
		else {
			ITree<Token> left = ast.getChild(0);
			ITree<Token> right = ast.getChild(1);

			String leftExpr = toCanonicalExpr(left);
			String rightExpr = toCanonicalExpr(right);

			/*
			 * Add parens if the left was higher priority.
			 */
			if(left.getChildrenCount() == 0) {
				if(left.getHead().type.operatorPriority >= data.type.operatorPriority) {
					leftExpr = "(" + leftExpr + ")";
				}
			}

			/*
			 * Add parens if the right was higher priority.
			 */
			if(right.getChildrenCount() == 0) {
				if(right.getHead().type.operatorPriority >= data.type.operatorPriority) {
					rightExpr = "(" + rightExpr + ")";
				}
			}

			return leftExpr + " " + data.toExpr() + " " + rightExpr;
		}
	}
}