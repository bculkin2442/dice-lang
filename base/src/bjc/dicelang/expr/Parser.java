package bjc.dicelang.expr;

import bjc.utils.data.ITree;

/**
 * Parser for simple math expressions.
 *
 * @author Ben Culkin
 */
public class Parser {
	/**
	 * Convert an expression to one that uses the smallest necessary amount of
	 * parens.
	 * 
	 * @param ast
	 *            The AST to canonicalize.
	 * @return The canonicalized AST.
	 */
	public static String toCanonicalExpr(final ITree<Token> ast) {
		final Token data = ast.getHead();

		if (ast.getChildrenCount() == 0) {
			/* Handle leaf nodes. */
			return data.toExpr();
		}

		/* The left/right children. */
		final ITree<Token> left = ast.getChild(0);
		final ITree<Token> right = ast.getChild(1);

		/* Recursively canonicalize them. */
		String leftExpr = toCanonicalExpr(left);
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
			int dataPriority = data.typ.operatorPriority;

			if (rightPriority >= dataPriority) {
				rightExpr = String.format("(%s)", rightExpr);
			}
		}

		return String.format("%s %s %s", leftExpr, data.toExpr(), rightExpr);
	}
}
