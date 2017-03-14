package bjc.dicelang.v1.ast.nodes;

/**
 * Represents an integer literal of some kind
 *
 * @author ben
 *
 */
public class IntegerLiteralNode implements ILiteralDiceNode {
	private int value;

	/**
	 * Create a new integer literal from the given number
	 *
	 * @param val
	 *                The value this node represents
	 */
	public IntegerLiteralNode(int val) {
		value = val;
	}

	@Override
	public boolean canOptimize() {
		return true;
	}

	@Override
	public DiceLiteralType getLiteralType() {
		return DiceLiteralType.INTEGER;
	}

	/**
	 * Get the value this node represents
	 *
	 * @return The integer value of this node
	 */
	public int getValue() {
		return value;
	}

	@Override
	public int optimize() {
		return value;
	}

	@Override
	public String toString() {
		return Integer.toString(value);
	}
}
