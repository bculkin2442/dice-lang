package bjc.dicelang.v1.ast.nodes;

/**
 * The interface for a node in a dice AST
 * 
 * @author ben
 *
 */
public interface IDiceASTNode {
	/**
	 * Get the type of AST node this node is
	 * 
	 * @return The type of AST node this AST node is
	 */
	public DiceASTType getType();

	/**
	 * Check if this node represents an operator or not
	 * 
	 * @return Whether or not this node represents an operator
	 */
	public boolean isOperator();
}