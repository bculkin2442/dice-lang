package bjc.utils.dice.ast;

/**
 * A node that represents a variable reference
 * 
 * @author ben
 *
 */
public class VariableDiceNode implements IDiceASTNode {
	/**
	 * The variable referenced by this node
	 */
	private String var;

	/**
	 * Create a new node representing the specified variable
	 * 
	 * @param data
	 *            The name of the variable being referenced
	 */
	public VariableDiceNode(String data) {
		this.var = data;
	}

	/**
	 * Get the variable referenced by this AST node
	 * 
	 * @return the variable referenced by this AST node
	 */
	public String getVariable() {
		return var;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see bjc.utils.dice.ast.IDiceASTNode#isOperator()
	 */
	@Override
	public boolean isOperator() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return var;
	}
}
