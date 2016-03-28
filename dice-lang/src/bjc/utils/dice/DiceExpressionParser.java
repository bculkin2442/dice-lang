package bjc.utils.dice;

import java.util.Map;
import java.util.Stack;

import org.apache.commons.lang3.StringUtils;

import bjc.utils.funcdata.FunctionalList;
import bjc.utils.funcdata.FunctionalStringTokenizer;
import bjc.utils.parserutils.ShuntingYard;

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
	 * @param exp
	 *            The string to parse an expression from
	 * @param env
	 *            The enviroment to use when parsing expressions
	 * @return The parsed dice expression
	 */
	public IDiceExpression parse(String exp,
			Map<String, IDiceExpression> env) {
		/*
		 * Create a tokenizer over the strings
		 */
		FunctionalStringTokenizer fst = new FunctionalStringTokenizer(exp);

		/*
		 * Create a shunter to rewrite the expression
		 */
		ShuntingYard<String> yard = new ShuntingYard<>();

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
		FunctionalList<String> ls =
				yard.postfix(fst.toList(s -> s), s -> s);

		/*
		 * Create a stack for building an expression from parts
		 */
		Stack<IDiceExpression> dexps = new Stack<>();

		/*
		 * Create the expression from parts
		 */
		ls.forEach((tok) -> {
			/*
			 * Handle compound dice
			 */
			if (StringUtils.countMatches(tok, 'c') == 1
					&& !tok.equalsIgnoreCase("c")) {
				String[] strangs = tok.split("c");

				dexps.push(new CompoundDice(strangs));
			} else if (StringUtils.countMatches(tok, 'd') == 1
					&& !tok.equalsIgnoreCase("d")) {
				/*
				 * Handle dice groups
				 */
				dexps.push(ComplexDice.fromString(tok));
			} else {
				try {
					/*
					 * Handle scalar numbers
					 */
					dexps.push(new ScalarDie(Integer.parseInt(tok)));
				} catch (NumberFormatException nfex) {

					if (dexps.size() >= 2) {
						/*
						 * Apply an operation to two dice
						 */
						IDiceExpression r = dexps.pop();
						IDiceExpression l = dexps.pop();
						switch (tok) {
							case ":=":
								dexps.push(new BindingDiceExpression(l, r,
										env));
								break;
							case "+":
								dexps.push(new CompoundDiceExpression(r, l,
										DiceExpressionType.ADD));
								break;
							case "-":
								dexps.push(new CompoundDiceExpression(r, l,
										DiceExpressionType.SUBTRACT));
								break;
							case "*":
								dexps.push(new CompoundDiceExpression(r, l,
										DiceExpressionType.MULTIPLY));
								break;
							case "/":
								dexps.push(new CompoundDiceExpression(r, l,
										DiceExpressionType.DIVIDE));
								break;
							case "c":
								dexps.push(new CompoundDice(l, r));
								break;
							case "d":
								dexps.push(new ComplexDice(l, r));
								break;
							default:
								/*
								 * Parse it as a variable reference
								 * 
								 * Make sure to restore popped variables
								 */
								dexps.push(l);
								dexps.push(r);

								dexps.push(new ReferenceDiceExpression(tok,
										env));
						}
					} else {
						/*
						 * Parse it as a variable reference
						 */
						dexps.push(new ReferenceDiceExpression(tok, env));
					}
				}
			}
		});

		if (dexps.size() != 1) {
			System.err.println(
					"WARNING: Leftovers found on dice expression stack. Remember, := is assignment.");
		}

		/*
		 * Return the built expression
		 */
		return dexps.pop();
	}
}
