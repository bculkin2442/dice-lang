package bjc.dicelang.examples;

import java.util.Map;
import java.util.function.BiConsumer;

import bjc.dicelang.ast.DiceASTExpression;
import bjc.dicelang.ast.DiceASTFreezer;
import bjc.dicelang.ast.DiceASTParser;
import bjc.dicelang.ast.nodes.IDiceASTNode;
import bjc.utils.funcdata.FunctionalMap;
import bjc.utils.parserutils.AST;

/**
 * Container for pragma handlers
 * 
 * @author ben
 *
 */
public class DiceASTLanguagePragmaHandlers {
	/**
	 * Handles freezing a specified expression to a new name
	 * 
	 * @author ben
	 *
	 */
	public static final class FreezeHandler implements
			BiConsumer<DiceASTParser, Map<String, DiceASTExpression>> {
		private String	expressionToFreeze;
		private String	resultingVariable;

		/**
		 * Create a new freeze handler
		 * 
		 * @param expressionToFreeze
		 *            The name of the expression to freeze
		 * @param resultingVariable
		 *            The name of the variable to bind the frozen
		 *            expression to
		 */
		public FreezeHandler(String expressionToFreeze,
				String resultingVariable) {
			this.expressionToFreeze = expressionToFreeze;
			this.resultingVariable = resultingVariable;
		}

		@Override
		public void accept(DiceASTParser astParser,
				Map<String, DiceASTExpression> enviroment) {
			if (enviroment.containsKey(expressionToFreeze)) {
				System.err.println(
						"ERROR: There is no expression bound to the variable "
								+ expressionToFreeze + ".");
			} else {
				AST<IDiceASTNode> frozenAST = DiceASTFreezer.freezeAST(
						enviroment.get(expressionToFreeze),
						new FunctionalMap<>(enviroment));

				enviroment.put(resultingVariable,
						new DiceASTExpression(frozenAST, enviroment));
			}
		}
	}

	/**
	 * Print the enviroment for debugging/inspection purposes
	 * 
	 * @author ben
	 *
	 */
	public static final class EnviromentPrinter implements
			BiConsumer<DiceASTParser, Map<String, DiceASTExpression>> {
		@Override
		public void accept(DiceASTParser astParser,
				Map<String, DiceASTExpression> enviroment) {
			enviroment.forEach((variable, boundExpression) -> System.out
					.println("\tKey: " + variable + "\n\t\tExp: "
							+ boundExpression.toString()));
		}
	}
}