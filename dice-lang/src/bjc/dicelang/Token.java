package bjc.dicelang;

import bjc.dicelang.dice.DieExpression;
import bjc.utils.funcdata.IList;

/**
 * Lexer token.
 */
@SuppressWarnings("javadoc")
public class Token {
	public final static Token NIL_TOKEN = new Token(Type.NIL);

	/**
	 * Possible token types
	 */
	public static enum Type {
		// Natural tokens
		// These are produced from lexemes
		ADD, SUBTRACT, MULTIPLY, DIVIDE, IDIVIDE, INT_LIT, FLOAT_LIT, STRING_LIT, VREF, DICE_LIT, DICESCALAR, DICEFUDGE, DICEGROUP, DICECONCAT, DICELIST, LET, BIND, COERCE, STRCAT, STRREP, OPAREN, CPAREN, OBRACKET, CBRACKET, OBRACE, CBRACE,

		// Synthetic tokens
		// These are produced when needed
		NIL, GROUPSEP, TOKGROUP, TAGOP, TAGOPR
	}

	public final Type type;

	// This is used for the following token types
	// INT_LIT (int value)
	// STRING_LIT (index into string table)
	// VREF (index into sym table)
	// O* and C* (sym-count of current token)
	public long intValue;

	// This is used for the following token types
	// FLOAT_LIT (float value)
	public double floatValue;

	// This is used for the following token types
	// DICE_LIT (dice value)
	public DieExpression diceValue;

	// This is used for the following token types
	// TOKGROUP (the tokens in the group)
	// TAG* (the tagged construct)
	public IList<Token> tokenValues;

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

	public Token(Type typ, DieExpression val) {
		this(typ);

		diceValue = val;
	}

	public Token(Type typ, IList<Token> tkVals) {
		this(typ);

		tokenValues = tkVals;
	}

	@Override
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
			return type.toString() + "(" + intValue + ")";
		case FLOAT_LIT:
			return type.toString() + "(" + floatValue + ")";
		case DICE_LIT:
			return type.toString() + "(" + diceValue + ")";
		case TAGOP:
		case TAGOPR:
		case TOKGROUP:
			return type.toString() + "(" + tokenValues + ")";
		default:
			return type.toString();
		}
	}

	@Override
	public boolean equals(Object other) {
		if(!(other instanceof Token)) return false;

		Token otk = (Token) other;

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

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
	}
}
