package bjc.dicelang.examples;

import java.util.Map;

import bjc.utils.data.Pair;

import bjc.dicelang.DiceExpressionParser;
import bjc.dicelang.IDiceExpression;

/**
 * Internal state of dice language
 * 
 * @author ben
 *
 */
public class DiceLanguageState
		extends Pair<DiceExpressionParser, Map<String, IDiceExpression>> {

	/**
	 * Create a new state
	 */
	public DiceLanguageState() {
	}

	/**
	 * Create a new state with the desired parameters
	 * 
	 * @param left
	 *            The parser to use
	 * @param right
	 *            The enviroment to use
	 */
	public DiceLanguageState(DiceExpressionParser left,
			Map<String, IDiceExpression> right) {
		super(left, right);
	}
}
