package bjc.dicelang.ast.nodes;

import bjc.dicelang.IDiceExpression;

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
	 *            The expression to attempt to create a literal from
	 */
	public DiceLiteralNode(IDiceExpression exp) {
		expression = exp;
	}

	/**
	 * Check if this node can be optimized to a constant
	 * 
	 * @return Whether or not this node can be optimized to a constant
	 * @see bjc.dicelang.IDiceExpression#canOptimize()
	 */
	public boolean canOptimize() {
		return expression.canOptimize();
	}

	@Override
	public DiceLiteralType getLiteralType() {
		return DiceLiteralType.DICE;
	}

	/**
	 * Return a value from the expression being represented
	 * 
	 * @return A value from the expression being represented
	 */
	public int getValue() {
		return expression.roll();
	}

	/**
	 * Optimize this node to a constant if possible
	 * 
	 * @return This node in constant form if possible
	 * @see bjc.dicelang.IDiceExpression#optimize()
	 */
	public int optimize() {
		return expression.optimize();
	}
}
