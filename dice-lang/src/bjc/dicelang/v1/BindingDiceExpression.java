package bjc.dicelang.v1;

import java.util.Map;

/**
 * A variable expression that represents binding a variable to a name in an
 * enviroment
 * 
 * @author ben
 *
 */
public class BindingDiceExpression implements IDiceExpression {
	/*
	 * The expression being bound to a name
	 */
	private IDiceExpression	expression;

	/*
	 * The name to bind the expression to
	 */
	private String			name;

	/**
	 * Create a new dice expression binder from two expressions and an
	 * enviroment
	 * 
	 * @param left
	 *            The left side expression to get a name from. Must be a
	 *            ReferenceDiceExpression
	 * @param right
	 *            The right side to bind to the name
	 * @param enviroment
	 *            The enviroment to bind into
	 */
	public BindingDiceExpression(IDiceExpression left,
			IDiceExpression right,
			Map<String, IDiceExpression> enviroment) {
		if (!(left instanceof ReferenceDiceExpression)) {
			throw new UnsupportedOperationException(
					"Error: Binding an expression to something that is not a variable reference,"
					+ " or array thereof. is unsupported."
					+ " Problematic expression is " + left);
		}

		String varName = ((ReferenceDiceExpression) left).getName();

		initialize(varName, right, enviroment);
	}

	/**
	 * Create a new dice expression binder
	 * 
	 * @param name
	 *            The name of the variable to bind
	 * @param expression
	 *            The expression to bind to the variable
	 * @param enviroment
	 *            The enviroment to bind it in
	 */
	public BindingDiceExpression(String name, IDiceExpression expression,
			Map<String, IDiceExpression> enviroment) {
		initialize(name, expression, enviroment);
	}

	private void initialize(String name, IDiceExpression expr,
			Map<String, IDiceExpression> enviroment) {
		this.name = name;
		this.expression = expr;

		enviroment.put(name, expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see bjc.utils.dice.IDiceExpression#roll()
	 */
	@Override
	public int roll() {
		return expression.roll();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "assign[n=" + name + ", exp="
			+ expression.toString() + "]";
	}
}
