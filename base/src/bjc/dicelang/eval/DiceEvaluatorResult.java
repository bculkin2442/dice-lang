package bjc.dicelang.eval;

import bjc.dicelang.dice.DiceExpression;
import bjc.dicelang.dice.Die;
import bjc.dicelang.dice.DieList;
import bjc.dicelang.dice.ListDiceExpression;
import bjc.dicelang.dice.ScalarDiceExpression;

public class DiceEvaluatorResult extends EvaluatorResult {
	/**
	 * The dice value of the result.
	 */
	public DiceExpression diceVal;

	public DiceEvaluatorResult(DiceExpression expr) {
		super(Type.DICE);

		diceVal = expr;
	}

	public DiceEvaluatorResult(Die die) {
		this(new ScalarDiceExpression(die));
	}

	public DiceEvaluatorResult(DieList list) {
		this(new ListDiceExpression(list));
	}

	public boolean isList() {
		return diceVal.isList();
	}
}
