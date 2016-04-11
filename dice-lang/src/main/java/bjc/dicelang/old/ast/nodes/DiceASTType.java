package bjc.dicelang.old.ast.nodes;

/**
 * An enum to represent the type of node an AST node is
 * 
 * @author ben
 *
 */
public enum DiceASTType {
	/**
	 * A node that contains a literal value
	 */
	LITERAL,
	/**
	 * A node that contains an operator expression
	 */
	OPERATOR,
	/**
	 * A node that contains a variable reference
	 */
	VARIABLE;

	@Override
	public String toString() {
		return this.name().toLowerCase();
	}
}