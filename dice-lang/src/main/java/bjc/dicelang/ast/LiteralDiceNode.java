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
	private String data;

	/**
	 * Create a new node with the given value
	 * 
	 * @param data
	 *            The value to be in this node
	 */
	public LiteralDiceNode(String data) {
		this.data = data;
	}

	@Override
	public boolean isOperator() {
		return false;
	}

	/**
	 * Get the data stored in this AST node
	 * 
	 * @return the data stored in this AST node
	 */
	public String getData() {
		return data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return data;
	}
}
