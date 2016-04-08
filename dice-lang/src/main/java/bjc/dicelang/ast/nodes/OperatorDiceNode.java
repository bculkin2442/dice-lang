package bjc.dicelang.ast.nodes;

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
	ADD,
	/**
	 * Represents assigning one node to another
	 */
	ASSIGN,
	/**
	 * Representings combining two node values together
	 */
	COMPOUND,
	/**
	 * Represents dividing two nodes
	 */
	DIVIDE,
	/**
	 * Represents using one node a variable number of times
	 */
	GROUP,
	/**
	 * Represents multiplying two nodes
	 */
	MULTIPLY,
	/**
	 * Represents subtracting two nodes
	 */
	SUBTRACT,
	/**
	 * Represents executing one statement in the context of the other
	 */
	LET;

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
				return GROUP;
			case "c":
				return COMPOUND;
			case "->":
				return LET;
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
