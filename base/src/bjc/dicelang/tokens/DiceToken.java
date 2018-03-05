package bjc.dicelang.tokens;

import bjc.dicelang.dice.DiceExpression;

/**
 * A token that contains a dice value.
 * 
 * @author student
 *
 */
public class DiceToken extends Token {
	/**
	 * The value of th token.
	 */
	public DiceExpression diceValue;

	/**
	 * Create a new dice token.
	 * 
	 * @param val
	 *            The value of the token.
	 */
	public DiceToken(DiceExpression val) {
		super(Type.DICE_LIT);

		diceValue = val;
	}

	@Override
	public String toString() {
		return super.toString() + "(" + diceValue + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((diceValue == null) ? 0 : diceValue.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		DiceToken other = (DiceToken) obj;
		
		if (diceValue == null) {
			if (other.diceValue != null)
				return false;
		} else if (!diceValue.equals(other.diceValue))
			return false;
		return true;
	}
}
