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
	private Map<String, IDiceExpression>	env;

	/**
	 * The name of the bound variable
	 */
	private String							name;

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
		this.name = name;
		this.env = env;
	}

	/**
	 * Get the name of the referenced variable
	 * 
	 * @return the name of the referenced variable
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see bjc.utils.dice.IDiceExpression#roll()
	 */
	@Override
	public int roll() {
		if (!env.containsKey(name)) {
			throw new UnsupportedOperationException(
					"Attempted to reference undefined variable " + name);
		}
		
		return env.get(name).roll();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (env.containsKey(name)) {
			return env.get(name).toString();
		} else {
			return name;
		}
	}
}