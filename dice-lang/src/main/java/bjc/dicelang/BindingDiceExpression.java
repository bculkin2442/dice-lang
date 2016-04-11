package bjc.dicelang;

import java.util.Map;

/**
 * A variable expression that represents binding a variable to a name in an
 * enviroment
 * 
 * @author ben
 *
 */
public class BindingDiceExpression implements IDiceExpression {
	/**
	 * The expression being bound to a name
	 */
	private IDiceExpression	expression;

	/**
	 * The name to bind the expression to
	 */
	private String			variableName;

	/**
	 * Create a new dice expression binder from two expressions and an
	 * enviroment
	 * 
	 * @param left
	 *            The left side expression to get a name from. Must be a
	 *            ReferenceDiceExpression
	 * @param right
	 *            The right side to bind to the name
	 * @param env
	 *            The enviroment to bind into
	 */
	public BindingDiceExpression(IDiceExpression left,
			IDiceExpression right, Map<String, IDiceExpression> env) {
		if (!(left instanceof ReferenceDiceExpression)) {
			throw new UnsupportedOperationException(
					"Binding to non-references is unsupported."
							+ " Problematic expression is " + left);
		}

		String varName = ((ReferenceDiceExpression) left).getName();

		initialize(varName, right, env);
	}

	/**
	 * Create a new dice expression binder
	 * 
	 * @param name
	 *            The name of the variable to bind
	 * @param exp
	 *            The expression to bind to the variable
	 * @param env
	 *            The enviroment to bind it in
	 */
	public BindingDiceExpression(String name, IDiceExpression exp,
			Map<String, IDiceExpression> env) {
		initialize(name, exp, env);
	}

	private void initialize(String name, IDiceExpression exp,
			Map<String, IDiceExpression> env) {
		this.variableName = name;
		this.expression = exp;

		env.put(name, exp);
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
		return "assign[n=" + variableName + ", exp="
				+ expression.toString() + "]";
	}
}
