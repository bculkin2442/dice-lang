package bjc.dicelang.v2;

import bjc.utils.funcdata.IList;

/**
 * Lexer token
 */
public class Token {
	public final static Token NIL_TOKEN = new Token(Type.NIL);

	/**
	 * Possible token types
	 */
	public static enum Type {
		// Natural tokens
		// These are produced from lexemes
		ADD,      SUBTRACT,
		MULTIPLY,
		DIVIDE,   IDIVIDE,
		INT_LIT,  FLOAT_LIT, STRING_LIT,
		VREF,
		DICE_LIT, DICEGROUP, DICECONCAT, DICELIST,
		LET,      BIND,
		OPAREN,   CPAREN,
		OBRACKET, CBRACKET,
		OBRACE,   CBRACE,
		// Synthetic tokens
		// These are produced when needed
		NIL,      PRESHUNT,  GROUPSEP,
		TOKGROUP
	}

	public final Type type;

	// At most one of these is valid
	// based on the token type
	public long 				 intValue;
	public double 				 floatValue;
	public String 				 stringValue;
	public DiceBox.DieExpression diceValue;
	public IList<Token>          tokenValues;

	public Token(Type typ) {
		type = typ;
	}

	public Token(Type typ, long val) {
		this(typ);

		intValue = val;
	}

	public Token(Type typ, double val) {
		this(typ);

		floatValue = val;
	}

	public Token(Type typ, String val) {
		this(typ);

		stringValue = val;
	}

	public Token(Type typ, DiceBox.DieExpression val) {
		this(typ);

		diceValue = val;
	}

	public Token(Type typ, IList<Token> tkVals) {
		this(typ);

		tokenValues = tkVals;
	}

	public String toString() {
		switch(type) {
			case INT_LIT:
			case STRING_LIT:
			case VREF:
			case OPAREN:
			case CPAREN:
			case OBRACKET:
			case CBRACKET:
			case OBRACE:
			case CBRACE:
				return type.toString() + "("
					+ intValue + ")";
			case FLOAT_LIT:
				return type.toString() + "("
					+ floatValue + ")";
			case DICE_LIT:
				return type.toString() + "("
					+ diceValue + ")";
			case TOKGROUP:
				return type.toString() + "("
					+ tokenValues + ")";
			default:
				return type.toString();
		}
	}

	public boolean equals(Object other) {
		if(!(other instanceof Token)) return false;

		Token otk = (Token)other;

		if(otk.type != type) return false;

		switch(type) {
			case OBRACE:
			case OBRACKET:
				return intValue == otk.intValue;
			default:
				return true;
		}
	}

	public boolean isGrouper() {
		switch(type) {
			case OPAREN:
			case OBRACE:
			case OBRACKET:
				return true;
			default:
				return false;
		}
	}
}
