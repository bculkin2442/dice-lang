package bjc.dicelang.v2;

import bjc.dicelang.IDiceExpression;

/**
 * Lexer token
 */
public class Token {
	public final static Token NIL_TOKEN = new Token(Type.NIL);

	/**
	 * Possible token types
	 */
	public static enum Type {
		ADD, SUBTRACT,
		MULTIPLY, 
		DIVIDE, IDIVIDE,
		INT_LIT, FLOAT_LIT, STRING_LIT,
		VREF,
		DICE_LIT, DICEGROUP, DICECONCAT,
		LET, BIND,
		OPAREN, CPAREN,
		OBRACKET, CBRACKET,
		NIL,
	}

	public final Type type;

	// At most one of these is valid
	// based on the token type
	public int intValue;
	public double floatValue;
	public DiceBox.Die diceValue;

	public Token(Type typ) {
		type = typ;
	}

	public Token(Type typ, int val) {
		this(typ);

		intValue = val;
	}

	public Token(Type typ, double val) {
		this(typ);

		floatValue = val;
	}

	public Token(Type typ, DiceBox.Die val) {
		this(typ);

		diceValue = val;
	}

	public String toString() {
		switch(type) {
			case INT_LIT:
			case STRING_LIT:
			case VREF:
				return type.toString() + "("
					+ intValue + ")";
			case FLOAT_LIT:
				return type.toString() + "("
					+ floatValue + ")";
			case DICE_LIT:
				return type.toString() + "("
					+ diceValue + ")";
			default:
				return type.toString();
		}
	}
}
