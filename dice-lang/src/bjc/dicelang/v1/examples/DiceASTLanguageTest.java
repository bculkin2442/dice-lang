package bjc.dicelang.v1.examples;

import java.util.InputMismatchException;
import java.util.Scanner;

import bjc.dicelang.v1.ast.DiceASTEvaluator;
import bjc.dicelang.v1.ast.DiceASTInliner;
import bjc.dicelang.v1.ast.DiceASTOptimizer;
import bjc.dicelang.v1.ast.DiceASTParser;
import bjc.dicelang.v1.ast.DiceASTReferenceSanitizer;
import bjc.dicelang.v1.ast.IResult;
import bjc.dicelang.v1.ast.nodes.IDiceASTNode;
import bjc.dicelang.v1.ast.optimization.ConstantCollapser;
import bjc.dicelang.v1.ast.optimization.OperationCondenser;
import bjc.utils.data.ITree;
import bjc.utils.funcdata.FunctionalMap;
import bjc.utils.funcdata.FunctionalStringTokenizer;
import bjc.utils.funcdata.IList;
import bjc.utils.funcdata.IMap;

/**
 * Test interface for AST-based dice language
 * 
 * @author ben
 *
 */
public class DiceASTLanguageTest {
	private static IMap<String, DiceASTPragma>	actions;

	private static DiceASTOptimizer				optimizer;

	// Set up things that need to be configured
	static {
		actions = new FunctionalMap<>();

		// Inline all the variables in a given expression
		actions.put("inline", DiceASTLanguageTest::handleInlineAction);

		// Print out the enviroment
		actions.put("env", (tokenizer, enviroment) -> {
			enviroment.forEach((varName, varValue) -> {
				System.out.println(varName + " is bound to " + varValue);
			});
		});
 
		// Create and configure the optimizer
		optimizer = new DiceASTOptimizer();

		optimizer.addPass(new ConstantCollapser());
	}

	// Read in a command
	private static String getNextCommand(Scanner inputSource,
			int commandNumber) {
		// Print a prompt using the current command number
		System.out.print("\ndice-lang-" + commandNumber + "> ");

		// Read in the next command
		return inputSource.nextLine();
	}

	private static void handleInlineAction(
			FunctionalStringTokenizer tokenizer,
			IMap<String, ITree<IDiceASTNode>> enviroment) {
		// Skip the pragma name
		tokenizer.nextToken();

		// Get the pragma arguments
		IList<String> pragmaArgs = tokenizer.toList();

		if (pragmaArgs.getSize() < 3) {
			// Complain about pragma arguments not being valid
			System.err.println(
					"ERROR: Inline requires at least 3 parameters. They are:"
							+ "\n\t1. The name of the expression to inline."
							+ "\n\t2. The name of the variable to bind the result to."
							+ "\n\t3 and onwards. Names of variables to inline in the expression.");
		} else {
			// Get arguments
			String inlineExpression = pragmaArgs.getByIndex(0);
			String variableName = pragmaArgs.getByIndex(1);

			// Grab the variables we want to inline
			IList<String> inlinedVariables = pragmaArgs.tail().tail();

			// Actually inline the variable
			ITree<IDiceASTNode> inlinedExpression = DiceASTInliner
					.selectiveInline(enviroment.get(inlineExpression),
							enviroment, inlinedVariables);

			// Stick the inlined variable into the enviroment
			enviroment.put(variableName, inlinedExpression);
		}
	}

	/**
	 * Main method of class
	 * 
	 * @param args
	 *			  Unused CLI args
	 */
	public static void main(String[] args) {
		// Prepare the things we need for input
		Scanner inputSource = new Scanner(System.in);
		int commandNumber = 0;

		// Grab the initial command
		String currentLine = getNextCommand(inputSource, commandNumber);

		// The enviroment for variables
		IMap<String, ITree<IDiceASTNode>> enviroment = new FunctionalMap<>();

		// Handle commands
		while (!currentLine.equalsIgnoreCase("quit")) {
			// Get the name of a possible action
			String possibleActionName = currentLine.split(" ")[0];

			// Check and see if we're executing an action
			if (actions.containsKey(possibleActionName)) {
				// Execute action
				FunctionalStringTokenizer tokenizer = new FunctionalStringTokenizer(
						currentLine);

				// Execute the action
				actions.get(possibleActionName).accept(tokenizer,
						enviroment);

				// Get the next command
				currentLine = getNextCommand(inputSource, commandNumber);

				continue;
			}

			// The AST we are going to build
			ITree<IDiceASTNode> builtAST;

			// Time command preparation
			long time = System.nanoTime();

			// Prepare the command
			IList<String> preparedTokens = DiceExpressionPreparer
					.prepareCommand(currentLine);

			System.out.println("Command prepared in "
					+ (double) (System.nanoTime() - time) / 1000000000
					+ " s");

			try {
				// Time the AST creation
				time = System.nanoTime();

				// Create the AST
				builtAST = DiceASTParser.createFromString(preparedTokens);

				System.out
						.println(
								"Command parsed in "
										+ (double) (System.nanoTime()
												- time) / 1000000000
										+ " s");
			} catch (InputMismatchException | IllegalStateException
					| UnsupportedOperationException ex) {
				// Tell the user there was an error in parsing
				System.out.println("PARSING ERROR: " + ex.getLocalizedMessage());

				// Move onto the next command
				currentLine = getNextCommand(inputSource, commandNumber);

				continue;
			}

			// Print out parsed AST
			System.out.println("\tParsed: " + builtAST.toString());

			// Time AST transformation
			time = System.nanoTime();

			// Transform the AST
			ITree<IDiceASTNode> transformedAST = transformAST(builtAST,
					enviroment);

			System.out.println("Command transformed in "
					+ (double) (System.nanoTime() - time) / 1000000000
					+ " s");

			// Print out the transformed AST
			System.out
					.println("\tTransformed: " + transformedAST.toString());


			try {
				// Time the evaluation
				time = System.nanoTime();

				// Evaluate the expression once
				IResult sampleResult = DiceASTEvaluator.evaluateAST(transformedAST,
						enviroment);

				System.out
						.println(
								"Command evaluated in "
										+ (double) (System.nanoTime()
												- time) / 1000000000
										+ " s");

				// Print out the result of evaluating the expression
				System.out.println("\t\tSample Result: " + sampleResult);
				
				// Update the 'last' meta-variable
				enviroment.put("last", transformedAST);
			} catch (UnsupportedOperationException usex) {
				// Tell the user there was an error in evaluation
				System.out.println("EVALUATION ERROR: " + usex.getLocalizedMessage());

				// Get the next command
				currentLine = getNextCommand(inputSource, commandNumber);

				// Process it
				continue;
			}


			// Increase the number of commands
			commandNumber++;

			// Get the next command
			currentLine = getNextCommand(inputSource, commandNumber);
		}

		System.out.println("Bye.");

		// Cleanup after ourselves
		inputSource.close();
	}

	// Transform a parsed AST
	private static ITree<IDiceASTNode> transformAST(
			ITree<IDiceASTNode> builtAST,
			IMap<String, ITree<IDiceASTNode>> enviroment) {
		// Optimize the tree first
		ITree<IDiceASTNode> optimizedTree = optimizer
				.optimizeTree(builtAST, enviroment);

		// Then, condense unnecessary operations
		ITree<IDiceASTNode> condensedTree = OperationCondenser
				.condense(optimizedTree);

		// Next, sanitize references
		ITree<IDiceASTNode> sanitizedTree = DiceASTReferenceSanitizer
				.sanitize(condensedTree, enviroment);

		// Re-optimize the sanitized & condensed tree
		optimizedTree = optimizer.optimizeTree(sanitizedTree, enviroment);

		// Re-condense the newly optimized tree
		condensedTree = OperationCondenser.condense(optimizedTree);

		return condensedTree;
	}
}
