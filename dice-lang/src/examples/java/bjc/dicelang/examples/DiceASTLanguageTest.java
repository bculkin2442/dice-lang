package bjc.dicelang.examples;

import java.util.InputMismatchException;
import java.util.Scanner;

import bjc.utils.funcdata.FunctionalMap;
import bjc.utils.funcdata.FunctionalStringTokenizer;
import bjc.utils.funcdata.IList;
import bjc.utils.funcdata.IMap;
import bjc.utils.funcdata.ITree;

import bjc.dicelang.ast.DiceASTEvaluator;
import bjc.dicelang.ast.DiceASTInliner;
import bjc.dicelang.ast.DiceASTOptimizer;
import bjc.dicelang.ast.DiceASTParser;
import bjc.dicelang.ast.DiceASTReferenceSanitizer;
import bjc.dicelang.ast.IResult;
import bjc.dicelang.ast.nodes.IDiceASTNode;
import bjc.dicelang.ast.optimization.ConstantCollapser;
import bjc.dicelang.ast.optimization.OperationCondenser;

/**
 * Test interface for AST-based dice language
 * 
 * @author ben
 *
 */
public class DiceASTLanguageTest {
	private static IMap<String, DiceASTPragma>	actions;

	private static DiceASTOptimizer				optimizer;

	static {
		actions = new FunctionalMap<>();

		actions.put("inline", DiceASTLanguageTest::handleInlineAction);

		actions.put("env", (tokenizer, enviroment) -> {
			enviroment.forEach((varName, varValue) -> {
				System.out.println(varName + " is bound to " + varValue);
			});
		});

		optimizer = new DiceASTOptimizer();

		optimizer.addPass(new ConstantCollapser());
	}

	private static String getNextCommand(Scanner inputSource,
			int commandNumber) {
		System.out.print("\ndice-lang-" + commandNumber + "> ");

		return inputSource.nextLine();
	}

	private static void handleInlineAction(
			FunctionalStringTokenizer tokenizer,
			IMap<String, ITree<IDiceASTNode>> enviroment) {
		// Skip the pragma name
		tokenizer.nextToken();

		IList<String> pragmaArgs = tokenizer.toList();

		if (pragmaArgs.getSize() < 3) {
			System.err.println(
					"ERROR: Inline requires at least 3 parameters. They are:"
							+ "\n\t1. The name of the expression to inline."
							+ "\n\t2. The name of the variable to bind the result to."
							+ "\n\t3 and onwards. Names of variables to inline in the expression.");
		} else {
			String inlineExpression = pragmaArgs.getByIndex(0);
			String variableName = pragmaArgs.getByIndex(1);

			IList<String> inlinedVariables = pragmaArgs.tail().tail();

			ITree<IDiceASTNode> inlinedExpression = DiceASTInliner
					.selectiveInline(enviroment.get(inlineExpression),
							enviroment, inlinedVariables);

			enviroment.put(variableName, inlinedExpression);
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
		IMap<String, ITree<IDiceASTNode>> enviroment = new FunctionalMap<>();

		while (!currentLine.equalsIgnoreCase("quit")) {
			String possibleActionName = currentLine.split(" ")[0];

			if (actions.containsKey(possibleActionName)) {
				// Execute action
				FunctionalStringTokenizer tokenizer = new FunctionalStringTokenizer(
						currentLine);

				actions.get(possibleActionName).accept(tokenizer,
						enviroment);

				currentLine = getNextCommand(inputSource, commandNumber);

				continue;
			}

			// Build an AST from the string expression
			ITree<IDiceASTNode> builtAST;

			long time = System.nanoTime();

			IList<String> preparedTokens = DiceExpressionPreparer
					.prepareCommand(currentLine);

			System.out.println("Command prepared in "
					+ (double) (System.nanoTime() - time) / 1000000000
					+ " s");

			try {
				time = System.nanoTime();

				builtAST = DiceASTParser.createFromString(preparedTokens);

				System.out
						.println(
								"Command parsed in "
										+ (double) (System.nanoTime()
												- time) / 1000000000
										+ " s");
			} catch (InputMismatchException | IllegalStateException
					| UnsupportedOperationException ex) {
				System.out.println("ERROR: " + ex.getLocalizedMessage());

				currentLine = getNextCommand(inputSource, commandNumber);

				continue;
			}

			// Print out results
			System.out.println("\tParsed: " + builtAST.toString());

			time = System.nanoTime();

			ITree<IDiceASTNode> transformedAST = transformAST(builtAST,
					enviroment);

			System.out.println("Command transformed in "
					+ (double) (System.nanoTime() - time) / 1000000000
					+ " s");

			System.out
					.println("\tEvaluated: " + transformedAST.toString());

			IResult sampleRoll;

			try {
				time = System.nanoTime();

				sampleRoll = DiceASTEvaluator.evaluateAST(transformedAST,
						enviroment);

				System.out
						.println(
								"Command evaluated in "
										+ (double) (System.nanoTime()
												- time) / 1000000000
										+ " s");

				enviroment.put("last", transformedAST);
			} catch (UnsupportedOperationException usex) {
				System.out.println("ERROR: " + usex.getLocalizedMessage());

				currentLine = getNextCommand(inputSource, commandNumber);

				continue;
			}

			System.out.println("\t\tSample Roll: " + sampleRoll);

			// Increase the number of commands
			commandNumber++;

			currentLine = getNextCommand(inputSource, commandNumber);
		}

		System.out.println("Bye.");

		// Cleanup after ourselves
		inputSource.close();
	}

	private static ITree<IDiceASTNode> transformAST(
			ITree<IDiceASTNode> builtAST,
			IMap<String, ITree<IDiceASTNode>> enviroment) {
		ITree<IDiceASTNode> optimizedTree = optimizer
				.optimizeTree(builtAST, enviroment);

		ITree<IDiceASTNode> condensedTree = OperationCondenser
				.condense(optimizedTree);

		ITree<IDiceASTNode> sanitizedTree = DiceASTReferenceSanitizer
				.sanitize(condensedTree, enviroment);

		optimizedTree = optimizer.optimizeTree(sanitizedTree, enviroment);

		condensedTree = OperationCondenser.condense(optimizedTree);

		return condensedTree;
	}
}
