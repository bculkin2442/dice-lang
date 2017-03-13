package bjc.dicelang.v1.ast.nodes;

/**
 * A node that represents a reference to a variable
 * 
 * @author ben
 *
 */
public class VariableDiceNode implements IDiceASTNode {
	/**
	 * The variable referenced by this node
	 */
	private String variableName;

	/**
	 * Create a new node representing the specified variable
	 * 
	 * @param varName
	 *                The name of the variable being referenced
	 */
	public VariableDiceNode(String varName) {
		this.variableName = varName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		// Handle special cases
		if (this == obj) {
			return true;
		} else if (obj == null) {
			return false;
		} else if (getClass() != obj.getClass()) {
			return false;
		} else {
			VariableDiceNode other = (VariableDiceNode) obj;

			if (variableName == null) {
				if (other.variableName != null) {
					return false;
				}
			} else if (!variableName.equals(other.variableName)) {
				return false;
			}

			return true;
		}
	}

	@Override
	public DiceASTType getType() {
		return DiceASTType.VARIABLE;
	}

	/**
	 * Get the variable referenced by this AST node
	 * 
	 * @return the variable referenced by this AST node
	 */
	public String getVariable() {
		return variableName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((variableName == null) ? 0 : variableName.hashCode());
		return result;
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
		return variableName;
	}
}