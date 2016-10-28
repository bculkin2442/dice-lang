package bjc.dicelang.ast.nodes;

import static bjc.dicelang.ast.nodes.DiceOperatorType.DICE;
import static bjc.dicelang.ast.nodes.DiceOperatorType.EXPRESSION;
import static bjc.dicelang.ast.nodes.DiceOperatorType.MATH;

/**
 * A node that represents an operator
 * 
 * @author ben
 *
 */
public enum OperatorDiceNode implements IDiceASTNode {
	/**
	 * Represents adding two nodes
	 */
	ADD(MATH),
	/**
	 * Represents dividing two nodes
	 */
	DIVIDE(MATH),
	/**
	 * Represents multiplying two nodes
	 */
	MULTIPLY(MATH),
	/**
	 * Represents subtracting two nodes
	 */
	SUBTRACT(MATH),
	/**
	 * Representings combining two node values together
	 */
	COMPOUND(DICE),
	/**
	 * Represents using one node a variable number of times
	 */
	GROUP(DICE),
	/**
	 * Represents constructing an array from a sequence of expressions
	 */
	ARRAY(DiceOperatorType.ARRAY),
	/**
	 * Represents assigning one node to another
	 */
	ASSIGN(EXPRESSION),
	/**
	 * Represents evaluating one expression in the context of another
	 */
	LET(EXPRESSION);

	/**
	 * Create a operator node from a string
	 * 
	 * @param s
	 *            The string to convert to a node
	 * @return The operator corresponding to the node
	 */
	public static OperatorDiceNode fromString(String s) {
		switch (s) {
			case ":=":
				return ASSIGN;
			case "+":
				return ADD;
			case "-":
				return SUBTRACT;
			case "*":
				return MULTIPLY;
			case "/":
				return DIVIDE;
			case "d":
			case "group":
				return GROUP;
			case "c":
			case "compound":
				return COMPOUND;
			case "=>":
				return LET;
			case "[]":
				return ARRAY;
			default:
				throw new IllegalArgumentException(
						s + " is not a valid operator node");
		}
	}

	/**
	 * Represents the group of operator this operator is sorted into.
	 * 
	 */
	public final DiceOperatorType type;

	private OperatorDiceNode(DiceOperatorType ty) {
		type = ty;
	}

	@Override
	public DiceASTType getType() {
		return DiceASTType.OPERATOR;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see bjc.utils.dice.ast.IDiceASTNode#isOperator()
	 */
	@Override
	public boolean isOperator() {
		return true;
	}
}
