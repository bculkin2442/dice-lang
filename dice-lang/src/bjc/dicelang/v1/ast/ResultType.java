package bjc.dicelang.v1.ast;

/**
 * Represents the result of a computation
 * 
 * @author ben
 *
 */
public enum ResultType {
	/**
	 * Represents a result that is equivalent to a single integer
	 */
	INTEGER,
	/**
	 * Represents a result that is an array
	 */
	ARRAY,
	/**
	 * Represents something not to poke at
	 */
	DUMMY
}