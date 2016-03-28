package bjc.dicelang.examples;

import java.util.Map;

import bjc.dicelang.DiceExpressionParser;
import bjc.dicelang.ast.DiceASTExpression;
import bjc.utils.data.Pair;

/**
 * Internal state of the AST-based dice langugae
 * 
 * @author ben
 *
 */
public class DiceASTLanguageState extends
		Pair<DiceExpressionParser, Map<String, DiceASTExpression>> {

	/**
	 * Create a new state
	 */
	public DiceASTLanguageState() {
	}

	/**
	 * Create a new state with the given contents
	 * 
	 * @param left
	 *            The parser to use
	 * @param right
	 *            The enviroment to use
	 */
	public DiceASTLanguageState(DiceExpressionParser left,
			Map<String, DiceASTExpression> right) {
		super(left, right);
	}
}
