package bjc.dicelang;

import java.util.Map;

/**
 * A dice expression that refers to a variable bound in a mutable
 * enviroment
 * 
 * @author ben
 *
 */
public class ReferenceDiceExpression implements IDiceExpression {
	/**
	 * The enviroment to do variable dereferencing against
	 */
	private Map<String, IDiceExpression>	enviroment;

	/**
	 * The name of the bound variable
	 */
	private String							variableName;

	/**
	 * Create a new reference dice expression referring to the given name
	 * in an enviroment
	 * 
	 * @param name
	 *            The name of the bound variable
	 * @param env
	 *            The enviroment to resolve the variable against
	 */
	public ReferenceDiceExpression(String name,
			Map<String, IDiceExpression> env) {
		this.variableName = name;
		this.enviroment = env;
	}

	/**
	 * Get the name of the referenced variable
	 * 
	 * @return the name of the referenced variable
	 */
	public String getName() {
		return variableName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see bjc.utils.dice.IDiceExpression#roll()
	 */
	@Override
	public int roll() {
		if (!enviroment.containsKey(variableName)) {
			throw new UnsupportedOperationException(
					"Attempted to reference undefined variable " + variableName);
		}

		return enviroment.get(variableName).roll();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (enviroment.containsKey(variableName)) {
			return enviroment.get(variableName).toString();
		}

		return variableName;
	}
}