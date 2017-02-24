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
	/*
	 * The enviroment to do variable dereferencing against
	 */
	private Map<String, IDiceExpression>	enviroment;

	/*
	 * The name of the bound variable
	 */
	private String							name;

	/**
	 * Create a new reference dice expression referring to the given name
	 * in an enviroment
	 * 
	 * @param nme
	 *            The name of the bound variable
	 * @param env
	 *            The enviroment to resolve the variable against
	 */
	public ReferenceDiceExpression(String nme,
			Map<String, IDiceExpression> env) {
		this.name = nme;
		this.enviroment = env;
	}

	/**
	 * Get the name of the referenced variable
	 * 
	 * @return the name of the referenced variable
	 */
	public String getName() {
		return name;
	}

	@Override
	public int roll() {
		if (!enviroment.containsKey(name)) {
			throw new UnsupportedOperationException(
					"Attempted to reference undefined variable "
							+ name);
		}

		return enviroment.get(name).roll();
	}

	@Override
	public String toString() {
		if (enviroment.containsKey(name)) {
			return enviroment.get(name).toString() + "(bound to " + name + ")";
		}

		return name + "(unbound)";
	}
}
