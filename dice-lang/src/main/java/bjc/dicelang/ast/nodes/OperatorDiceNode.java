package bjc.dicelang.ast.nodes;

import static bjc.dicelang.ast.nodes.DiceOperatorType.*;

// The following classes need to be changed upon addition of a new operator
// 1. DiceASTExpression
// 2. DiceASTFlattener
// 3. DiceASTParser
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
	 * Represents assigning one node to another
	 */
	ASSIGN(EXPRESSION),
	/**
	 * Representings combining two node values together
	 */
	COMPOUND(DICE),
	/**
	 * Represents dividing two nodes
	 */
	DIVIDE(MATH),
	/**
	 * Represents using one node a variable number of times
	 */
	GROUP(DICE),
	/**
	 * Represents multiplying two nodes
	 */
	MULTIPLY(MATH),
	/**
	 * Represents subtracting two nodes
	 */
	SUBTRACT(MATH);

	/**
	 * Represents the group of operator this operator is sorted into.
	 * 
	 */
	public final DiceOperatorType type;

	private OperatorDiceNode(DiceOperatorType ty) {
		type = ty;
	}

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
			default:
				throw new IllegalArgumentException(
						s + " is not a valid operator node");
		}
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
