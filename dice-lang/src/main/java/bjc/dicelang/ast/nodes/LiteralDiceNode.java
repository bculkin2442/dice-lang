package bjc.dicelang.ast.nodes;

import org.apache.commons.lang3.StringUtils;

import bjc.dicelang.ComplexDice;
import bjc.dicelang.CompoundDice;
import bjc.dicelang.IDiceExpression;
import bjc.dicelang.ScalarDie;
import bjc.utils.data.Pair;
import bjc.utils.parserutils.AST;

/**
 * A AST node that represents a literal value
 * 
 * @author ben
 *
 */
public class LiteralDiceNode implements IDiceASTNode {
	private static boolean isValidInfixOperator(String dat, String op) {
		return StringUtils.countMatches(dat, op) == 1
				&& !dat.equalsIgnoreCase(op) && !dat.startsWith(op);
	}

	/**
	 * The value contained by this node
	 */
	private String value;

	/**
	 * Create a new node with the given value
	 * 
	 * @param data
	 *            The value to be in this node
	 */
	public LiteralDiceNode(String data) {
		this.value = data;
	}

	/**
	 * Create a new node with the given value
	 * @param val The value for this node
	 */
	public LiteralDiceNode(int val) {
		this(Integer.toString(val));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null) {
			return false;
		} else if (getClass() != obj.getClass()) {
			return false;
		} else {
			LiteralDiceNode other = (LiteralDiceNode) obj;

			if (value == null) {
				if (other.value != null) {
					return false;
				}
			} else if (!value.equals(other.value)) {
				return false;
			}

			return true;
		}
	}

	/**
	 * Get the data stored in this AST node
	 * 
	 * @return the data stored in this AST node
	 */
	public String getData() {
		return value;
	}

	@Override
	public DiceASTType getType() {
		return DiceASTType.LITERAL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean isOperator() {
		return false;
	}

	/**
	 * Parse this node into an expression
	 * 
	 * @return The node in expression form
	 */
	public IDiceExpression toExpression() {
		String literalData = this.getData();

		if (LiteralDiceNode.isValidInfixOperator(literalData, "c")) {
			String[] strangs = literalData.split("c");

			return new CompoundDice(strangs);
		} else if (LiteralDiceNode.isValidInfixOperator(literalData,
				"d")) {
			/*
			 * Handle dice groups
			 */
			return ComplexDice.fromString(literalData);
		} else {
			try {
				return new ScalarDie(Integer.parseInt(literalData));
			} catch (NumberFormatException nfex) {
				throw new UnsupportedOperationException(
						"Found malformed leaf token " + this);
			}
		}
	}

	/**
	 * Parse this node into an expression
	 * 
	 * @return The node as a pair of a sample value and the AST it
	 *         represents
	 */
	public Pair<Integer, AST<IDiceASTNode>> toParseValue() {
		AST<IDiceASTNode> returnedAST = new AST<>(this);

		IDiceExpression expression = toExpression();

		return new Pair<>(expression.roll(), returnedAST);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return value;
	}

	/**
	 * Check if this node represents a constant value
	 * 
	 * @return Whether or not this node represents a constant value
	 */
	public boolean isConstant() {
		try {
			Integer.parseInt(value);
			return true;
		} catch (NumberFormatException nfex) {
			return false;
		}
	}

	/**
	 * Return the constant value this node represents
	 * 
	 * @return The constant value of this node
	 * 
	 * @throws NumberFormatException
	 *             if you call this on a node that doesn't represent a
	 *             constant value
	 */
	public int toConstant() {
		return Integer.parseInt(value);
	}
}