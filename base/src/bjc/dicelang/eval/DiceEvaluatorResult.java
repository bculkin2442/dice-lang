package bjc.dicelang.eval;

import bjc.dicelang.dice.DiceExpression;
import bjc.dicelang.dice.Die;
import bjc.dicelang.dice.DieList;
import bjc.dicelang.dice.ListDiceExpression;
import bjc.dicelang.dice.ScalarDiceExpression;

/**
 * Represents a result containing a dice value.
 * 
 * @author student
 *
 */
public class DiceEvaluatorResult extends EvaluatorResult {
	/**
	 * The dice value of the result.
	 */
	public DiceExpression diceVal;

	/**
	 * Create a new result from an expression.
	 * 
	 * @param expr
	 *            The expression to use.
	 */
	public DiceEvaluatorResult(DiceExpression expr) {
		super(Type.DICE);

		diceVal = expr;
	}

	/**
	 * Create a new result from a die.
	 * 
	 * @param die
	 *            The die to use.
	 */
	public DiceEvaluatorResult(Die die) {
		this(new ScalarDiceExpression(die));
	}

	/**
	 * Create a new result from a die list.
	 * 
	 * @param list
	 *            The die list to use.
	 */
	public DiceEvaluatorResult(DieList list) {
		this(new ListDiceExpression(list));
	}

	/**
	 * Check if the result is a list.
	 * 
	 * @return If the result is a list.
	 */
	public boolean isList() {
		return diceVal.isList();
	}
}
