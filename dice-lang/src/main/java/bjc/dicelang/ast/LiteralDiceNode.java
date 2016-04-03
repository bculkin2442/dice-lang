package bjc.dicelang.ast;

/**
 * A AST node that represents a literal value
 * 
 * @author ben
 *
 */
public class LiteralDiceNode implements IDiceASTNode {
	/**
	 * The value contained by this node
	 */
	private String value;

	/**
	 * Create a new node with the given value
	 * 
	 * @param data
	 *            The value to be in this node
	 */
	public LiteralDiceNode(String data) {
		this.value = data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null) {
			return false;
		} else if (getClass() != obj.getClass()) {
			return false;
		} else {
			LiteralDiceNode other = (LiteralDiceNode) obj;

			if (value == null) {
				if (other.value != null) {
					return false;
				}
			} else if (!value.equals(other.value)) {
				return false;
			}

			return true;
		}
	}

	/**
	 * Get the data stored in this AST node
	 * 
	 * @return the data stored in this AST node
	 */
	public String getData() {
		return value;
	}

	@Override
	public DiceASTType getType() {
		return DiceASTType.LITERAL;
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
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

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
		return value;
	}
}