package bjc.dicelang.v1.ast.nodes;

/**
 * Represents the different type of operators.
 *
 * Mostly, what distinguishes groups is that all the operators in a group have
 * similiar precedence, and operate on similiar things
 *
 * @author ben
 *
 */
public enum DiceOperatorType {
	/**
	 * Represents operators that do math operations
	 */
	MATH,
	/**
	 * Represents operators that do things with arrays
	 */
	ARRAY,
	/**
	 * Represents operators that do things with dice
	 */
	DICE,
	/**
	 * Represents operators that do things with expressions
	 */
	EXPRESSION;
}
