package bjc.dicelang.examples;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.function.BiConsumer;

import bjc.dicelang.IDiceExpression;
import bjc.dicelang.ast.DiceASTDefinedChecker;
import bjc.dicelang.ast.DiceASTExpression;
import bjc.dicelang.ast.DiceASTInliner;
import bjc.dicelang.ast.DiceASTParser;
import bjc.dicelang.ast.DiceASTReferenceChecker;
import bjc.dicelang.ast.nodes.IDiceASTNode;
import bjc.dicelang.ast.optimization.DiceASTOptimizer;

import static bjc.dicelang.examples.DiceASTLanguagePragmaHandlers.*;

import bjc.utils.data.GenHolder;
import bjc.utils.data.IHolder;
import bjc.utils.funcdata.FunctionalMap;
import bjc.utils.funcdata.bst.ITreePart.TreeLinearizationMethod;
import bjc.utils.parserutils.AST;

/**
 * A test of the AST based dice language
 * 
 * @author ben
 *
 */
public class DiceASTLanguageTest {
	/**
	 * The 'special commands' that aren't normal expressions that can be
	 * invoked from the prompt
	 */
	private static Map<String, BiConsumer<String, DiceASTLanguageState>> specialCommands;

	static {
		specialCommands = new HashMap<>();

		// Put all the defined special commands in place
		specialCommands.put("roll", DiceASTLanguageTest::rollReference);
		specialCommands.put("env", DiceASTLanguageTest::printEnv);
		specialCommands.put("inline", DiceASTLanguageTest::inlineVariable);
		specialCommands.put("optimize",
				DiceASTLanguageTest::optimizeReference);
	}

	private static void optimizeReference(String command,
			DiceASTLanguageState languageState) {
		String[] args = command.split(" ");

		if (args.length != 2) {
			System.err.println(
					"ERROR: Optimize requires the name of the expression to optimize");
		}

		languageState.doWith((astParser, enviroment) -> {
			if (!enviroment.containsKey(args[1])) {
				System.err.println(
						"ERROR: Attempted to optimize undefined variable "
								+ args[1]);
			}

			AST<IDiceASTNode> optimizedTree = DiceASTOptimizer
					.optimizeTree(enviroment.get(args[1]).getAst());

			System.out.println("Optimized:  " + optimizedTree);
		});
	}

	/**
	 * Inline the references in an expression.
	 * 
	 * This means replace variable references with the current contents of
	 * the variables they refer to
	 * 
	 * @param command
	 *            The command and its arguments
	 * @param languageState
	 *            The state of the language at the moment
	 */
	private static void inlineVariable(String command,
			DiceASTLanguageState languageState) {
		// Split the string into components
		String[] args = command.split(" ");

		// Make sure we have the correct amount of arguments
		if (args.length != 3) {
			System.err.println(
					"ERROR: Inline requires you provide the name of expression"
							+ " to inline, as well as the name of the variable to bind"
							+ " the result to.");
			return;
		}

		String expressionToInline = args[1];

		String resultingVariable = args[2];

		System.out.println("Inlining references in " + args[1]
				+ " and binding to " + resultingVariable);

		languageState.doWith(
				new InlineHandler(expressionToInline, resultingVariable));
	}

	/**
	 * Print all bound variables in the current enviroment, as well as what
	 * they're bound to
	 * 
	 * @param command
	 *            The command and its arguments
	 * @param stat
	 *            The state of the language at the moment
	 */
	private static void printEnv(String command,
			DiceASTLanguageState stat) {
		System.out.println("Printing enviroment for debugging purposes.");

		stat.doWith(new EnviromentPrinter());
	}

	/**
	 * Roll an expression a given number of times
	 * 
	 * @param command
	 *            The command and its arguments
	 * @param languageState
	 *            The state of the language at the moments
	 */
	private static void rollReference(String command,
			DiceASTLanguageState languageState) {
		String[] args = command.split(" ");

		if (args.length != 3) {
			System.err.println("ERROR: Roll requires two arguments."
					+ " The name of the expression to roll, "
					+ "followed by the number of times to roll it");
			return;
		}

		System.out.println("\tRolling dice expression " + args[1] + " "
				+ args[2] + " times.");

		String expressionName = args[1];

		int numberOfRolls;

		try {
			numberOfRolls = Integer.parseInt(args[2]);
		} catch (NumberFormatException nfex) {
			System.err.println(
					"ERROR: The second argument must be a valid number, and "
							+ args[2] + " is not one.");
			return;
		}

		IDiceExpression expressionToRoll =
				languageState.merge((astParser, enviroment) -> {
					if (!enviroment.containsKey(expressionName)) {
						return null;
					} else {
						return enviroment.get(expressionName);
					}
				});

		if (expressionToRoll == null) {
			System.err.println(
					"ERROR: There is no expression bound to the variable "
							+ expressionName + ".");
			return;
		}

		for (int i = 1; i <= numberOfRolls; i++) {
			int currentRoll = expressionToRoll.roll();

			System.out.println("\tRolled " + currentRoll);
		}
	}

	/**
	 * Main method of class
	 * 
	 * @param args
	 *            Unused CLI args
	 */
	public static void main(String[] args) {
		Scanner inputSource = new Scanner(System.in);
		int commandNumber = 0;

		System.out.print("dice-lang-" + commandNumber + "> ");
		String currentLine = inputSource.nextLine();

		// The enviroment for variables
		Map<String, DiceASTExpression> enviroment = new HashMap<>();

		// The parser to turn strings into AST's
		DiceASTParser astParser = new DiceASTParser();

		DiceASTLanguageState languageState =
				new DiceASTLanguageState(astParser, enviroment);

		while (!currentLine.equalsIgnoreCase("quit")) {
			String prospectiveCommandName = currentLine.split(" ")[0];

			// Handle special commands
			if (specialCommands.containsKey(prospectiveCommandName)) {
				specialCommands.get(prospectiveCommandName)
						.accept(currentLine, languageState);
			} else {

				// Build an AST from the string expression
				AST<IDiceASTNode> builtAST;

				try {
					builtAST = astParser.buildAST(currentLine);
				} catch (IllegalStateException isex) {
					System.out.println(
							"ERROR: " + isex.getLocalizedMessage());

					currentLine =
							getNextCommand(inputSource, commandNumber);

					continue;
				}

				// Build a rollable expression from the AST
				DiceASTExpression expression =
						new DiceASTExpression(builtAST, enviroment);

				int sampleRoll;

				try {
					sampleRoll = expression.roll();
				} catch (UnsupportedOperationException usex) {
					System.out.println(
							"ERROR: " + usex.getLocalizedMessage());

					currentLine =
							getNextCommand(inputSource, commandNumber);

					continue;
				}

				if (checkUndefined(enviroment, expression)) {
					System.out.println(
							"ERROR: Expression contains undefined variables."
									+ " Problematic expression: \n\t"
									+ expression);

					currentLine =
							getNextCommand(inputSource, commandNumber);

					continue;
				}

				expression = handleUpdateLast(enviroment, expression);

				// Print out results
				System.out.println("\tParsed: " + expression.toString());
				System.out.println("\t\tSample Roll: " + sampleRoll);
			}

			// Increase the number of commands
			commandNumber++;

			currentLine = getNextCommand(inputSource, commandNumber);
		}

		System.out.println("Bye.");

		// Cleanup after ourselves
		inputSource.close();
	}

	private static DiceASTExpression handleUpdateLast(
			Map<String, DiceASTExpression> enviroment,
			DiceASTExpression expression) {
		IHolder<Boolean> canUpdateLast = new GenHolder<>(true);

		// Check that no node references last
		expression.getAst().traverse(TreeLinearizationMethod.PREORDER,
				new DiceASTReferenceChecker(canUpdateLast, "last"));

		// Update last if we can
		if (canUpdateLast.unwrap((flag) -> flag)) {
			enviroment.put("last", expression);
		} else {
			// We need to freeze out references to last
			expression = freezeOutLast(enviroment, expression.getAst());

			enviroment.put("last", expression);
		}

		return expression;
	}

	private static boolean checkUndefined(
			Map<String, DiceASTExpression> enviroment,
			DiceASTExpression expression) {
		IHolder<Boolean> containsUndefined = new GenHolder<>(false);

		// Check that no node references last
		expression.getAst().traverse(TreeLinearizationMethod.PREORDER,
				new DiceASTDefinedChecker(containsUndefined, enviroment));

		return containsUndefined.unwrap((bool) -> bool);
	}

	private static String getNextCommand(Scanner inputSource,
			int commandNumber) {
		String currentLine;
		// Read a new command
		System.out.print("dice-lang-" + commandNumber + "> ");
		currentLine = inputSource.nextLine();
		return currentLine;
	}

	private static DiceASTExpression freezeOutLast(
			Map<String, DiceASTExpression> enviroment,
			AST<IDiceASTNode> builtAST) {
		FunctionalMap<String, AST<IDiceASTNode>> transformedEnviroment =
				new FunctionalMap<>(enviroment)
						.mapValues((expr) -> expr.getAst());

		AST<IDiceASTNode> expressionSansLast = DiceASTInliner
				.selectiveInline(builtAST, transformedEnviroment, "last");

		return new DiceASTExpression(expressionSansLast, enviroment);
	}
}
