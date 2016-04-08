package bjc.dicelang.examples;

import java.util.Map;
import java.util.function.BiConsumer;

import bjc.dicelang.ast.nodes.IDiceASTNode;
import bjc.dicelang.old.ast.DiceASTExpression;
import bjc.dicelang.old.ast.DiceASTInliner;
import bjc.dicelang.old.ast.DiceASTParser;
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
	 * Handles inlining a specified expression to a new name
	 * 
	 * @author ben
	 *
	 */
	public static final class InlineHandler implements
			BiConsumer<DiceASTParser, Map<String, DiceASTExpression>> {
		private String	expressionToInline;
		private String	resultingVariable;

		/**
		 * Create a new inlining handler
		 * 
		 * @param expressionToInline
		 *            The name of the expression to inline
		 * @param resultingVariable
		 *            The name of the variable to bind the inline
		 *            expression to
		 */
		public InlineHandler(String expressionToInline,
				String resultingVariable) {
			this.expressionToInline = expressionToInline;
			this.resultingVariable = resultingVariable;
		}

		@Override
		public void accept(DiceASTParser astParser,
				Map<String, DiceASTExpression> enviroment) {
			if (enviroment.containsKey(expressionToInline)) {
				System.err.println(
						"ERROR: There is no expression bound to the variable "
								+ expressionToInline + ".");
			} else {
				AST<IDiceASTNode> inlinedAST = DiceASTInliner.inlineAST(
						enviroment.get(expressionToInline),
						new FunctionalMap<>(enviroment));

				enviroment.put(resultingVariable,
						new DiceASTExpression(inlinedAST, enviroment));
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