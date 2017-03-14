package bjc.dicelang.v1.examples;

import bjc.dicelang.v1.DiceExpressionParser;
import bjc.dicelang.v1.IDiceExpression;
import bjc.utils.data.Pair;

import java.util.Map;

/**
 * Internal state of dice language
 *
 * @author ben
 *
 */
public class DiceLanguageState extends Pair<DiceExpressionParser, Map<String, IDiceExpression>> {

	/**
	 * Create a new state
	 */
	public DiceLanguageState() {
	}

	/**
	 * Create a new state with the desired parameters
	 *
	 * @param left
	 *                The parser to use
	 * @param right
	 *                The enviroment to use
	 */
	public DiceLanguageState(DiceExpressionParser left, Map<String, IDiceExpression> right) {
		super(left, right);
	}
}
