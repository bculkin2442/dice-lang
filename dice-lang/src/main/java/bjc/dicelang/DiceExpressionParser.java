package bjc.dicelang;

import java.util.Map;
import java.util.Stack;

import bjc.utils.funcdata.FunctionalStringTokenizer;
import bjc.utils.funcdata.IList;
import bjc.utils.parserutils.ShuntingYard;

import org.apache.commons.lang3.StringUtils;

/**
 * Parse a dice expression from a string
 * 
 * @author ben
 *
 */
public class DiceExpressionParser {
	/**
	 * Parse a dice expression from a string
	 * 
	 * @param expression
	 *            The string to parse an expression from
	 * @param enviroment
	 *            The enviroment to use when parsing expressions
	 * @return The parsed dice expression
	 */
	public static IDiceExpression parse(String expression,
			Map<String, IDiceExpression> enviroment) {
		/*
		 * Create a tokenizer over the strings
		 */
		FunctionalStringTokenizer tokenizer = new FunctionalStringTokenizer(
				expression);

		/*
		 * Create a shunter to rewrite the expression
		 */
		ShuntingYard<String> yard = new ShuntingYard<>(true);

		/*
		 * Add our custom operators to the yard
		 */
		yard.addOp("d", 5); // dice operator: use for creating variable
							// size dice groups
		yard.addOp("c", 6); // compound operator: use for creating compound
							// dice from expressions
		yard.addOp(":=", 0); // binding operator: Bind a name to a variable
								// expression

		/*
		 * Shunt the expression to postfix form
		 */
		IList<String> list = yard.postfix(tokenizer.toList(), s -> s);

		/*
		 * Create a stack for building an expression from parts
		 */
		Stack<IDiceExpression> expressions = new Stack<>();

		/*
		 * Create the expression from parts
		 */
		list.forEach((expressionPart) -> {
			/*
			 * Handle compound dice
			 */
			if (StringUtils.countMatches(expressionPart, 'c') == 1
					&& !expressionPart.equalsIgnoreCase("c")) {
				String[] strangs = expressionPart.split("c");

				expressions.push(new CompoundDice(strangs));
			} else if (StringUtils.countMatches(expressionPart, 'd') == 1
					&& !expressionPart.equalsIgnoreCase("d")) {
				/*
				 * Handle dice groups
				 */
				expressions.push(ComplexDice.fromString(expressionPart));
			} else {
				try {
					/*
					 * Handle scalar numbers
					 */
					expressions.push(new ScalarDie(
							Integer.parseInt(expressionPart)));
				} catch (@SuppressWarnings("unused") NumberFormatException nfex) {
					// We don't care about details, just that it failed
					if (expressions.size() >= 2) {
						/*
						 * Apply an operation to two dice
						 */
						IDiceExpression rightExpression = expressions
								.pop();
						IDiceExpression leftExpression = expressions.pop();

						switch (expressionPart) {
							case ":=":
								expressions.push(new BindingDiceExpression(
										leftExpression, rightExpression,
										enviroment));
								break;
							case "+":
								expressions
										.push(new OperatorDiceExpression(
												rightExpression,
												leftExpression,
												DiceExpressionType.ADD));
								break;
							case "-":
								expressions
										.push(new OperatorDiceExpression(
												rightExpression,
												leftExpression,
												DiceExpressionType.SUBTRACT));
								break;
							case "*":
								expressions
										.push(new OperatorDiceExpression(
												rightExpression,
												leftExpression,
												DiceExpressionType.MULTIPLY));
								break;
							case "/":
								expressions
										.push(new OperatorDiceExpression(
												rightExpression,
												leftExpression,
												DiceExpressionType.DIVIDE));
								break;
							case "c":
								expressions.push(new CompoundDice(
										leftExpression, rightExpression));
								break;
							case "d":
								expressions.push(new ComplexDice(
										leftExpression, rightExpression));
								break;
							default:
								/*
								 * Parse it as a variable reference
								 * 
								 * Make sure to restore popped variables
								 */
								expressions.push(leftExpression);
								expressions.push(rightExpression);

								expressions
										.push(new ReferenceDiceExpression(
												expressionPart,
												enviroment));
						}
					} else {
						/*
						 * Parse it as a variable reference
						 */
						expressions.push(new ReferenceDiceExpression(
								expressionPart, enviroment));
					}
				}
			}
		});

		if (expressions.size() != 1) {
			System.err.println(
					"WARNING: Leftovers found on dice expression stack. Remember, := is assignment.");
		}

		/*
		 * Return the built expression
		 */
		return expressions.pop();
	}
}
