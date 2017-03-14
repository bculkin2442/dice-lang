package bjc.dicelang.v1.ast.nodes;

import bjc.dicelang.v1.IDiceExpression;

/**
 * Represents a literal backed by a dice expression
 *
 * @author ben
 *
 */
public class DiceLiteralNode implements ILiteralDiceNode {
	private IDiceExpression expression;

	/**
	 * Create a new literal from an expression
	 *
	 * @param exp
	 *                The expression to attempt to create a literal from
	 */
	public DiceLiteralNode(IDiceExpression exp) {
		expression = exp;
	}

	@Override
	public boolean canOptimize() {
		return expression.canOptimize();
	}

	@Override
	public DiceLiteralType getLiteralType() {
		return DiceLiteralType.DICE;
	}

	/**
	 * Return the expression being represented
	 *
	 * @return The expression being represented
	 */
	public IDiceExpression getValue() {
		return expression;
	}

	@Override
	public int optimize() {
		return expression.optimize();
	}

	@Override
	public String toString() {
		return expression.toString();
	}
}
