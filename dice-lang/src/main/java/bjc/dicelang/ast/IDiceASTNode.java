package bjc.dicelang.ast;

/**
 * The interface for a node in a dice AST
 * 
 * @author ben
 *
 */
public interface IDiceASTNode {
	/**
	 * Check if this node represents an operator or not
	 * 
	 * @return Whether or not this node represents an operator
	 */
	public boolean isOperator();

	/**
	 * Get the type of AST node this node is
	 * 
	 * @return The type of AST node this AST node is
	 */
	public DiceASTType getType();
}