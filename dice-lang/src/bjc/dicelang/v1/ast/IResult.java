package bjc.dicelang.v1.ast;

/**
 * Represents a result from an expression evaluation
 * 
 * @author ben
 *
 */
public interface IResult {
	/**
	 * Get the type of this result
	 * 
	 * @return The type of this result
	 */
	public ResultType getType();
}
