package bjc.dicelang.examples;

import java.util.Scanner;

import bjc.dicelang.ast.DiceASTEvaluator;
import bjc.dicelang.ast.DiceASTInliner;
import bjc.dicelang.ast.DiceASTOptimizer;
import bjc.dicelang.ast.DiceASTParser;
import bjc.dicelang.ast.DiceASTReferenceSanitizer;
import bjc.dicelang.ast.nodes.IDiceASTNode;
import bjc.dicelang.ast.optimization.ConstantCollapser;
import bjc.dicelang.ast.optimization.OperationCondenser;
import bjc.utils.funcdata.FunctionalMap;
import bjc.utils.funcdata.FunctionalStringTokenizer;
import bjc.utils.funcdata.IFunctionalList;
import bjc.utils.funcdata.IFunctionalMap;
import bjc.utils.funcdata.ITree;

/**
 * Test interface for AST-based dice language
 * 
 * @author ben
 *
 */
public class DiceASTLanguageTest {
	private static IFunctionalMap<String, DiceASTPragma>	actions;

	private static DiceASTOptimizer							optimizer;

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

	private static void handleInlineAction(
			FunctionalStringTokenizer tokenizer,
			IFunctionalMap<String, ITree<IDiceASTNode>> enviroment) {
		// Skip the pragma name
		tokenizer.nextToken();

		IFunctionalList<String> pragmaArgs = tokenizer.toList();

		if (pragmaArgs.getSize() < 3) {
			System.err.println(
					"ERROR: Inline requires at least 3 parameters. They are:"
							+ "\n\t1. The name of the expression to inline."
							+ "\n\t2. The name of the variable to bind the result to."
							+ "\n\t3 and onwards. Names of variables to inline in the expression.");
		} else {
			String inlineExpression = pragmaArgs.getByIndex(0);
			String variableName = pragmaArgs.getByIndex(1);

			IFunctionalList<String> inlinedVariables =
					pragmaArgs.tail().tail();

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
		IFunctionalMap<String, ITree<IDiceASTNode>> enviroment =
				new FunctionalMap<>();

		while (!currentLine.equalsIgnoreCase("quit")) {
			String possibleActionName = currentLine.split(" ")[0];

			if (actions.containsKey(possibleActionName)) {
				System.err.println(
						"\nTRACE: Executing action " + possibleActionName
								+ " with line " + currentLine + "\n");

				// Execute action
				FunctionalStringTokenizer tokenizer =
						new FunctionalStringTokenizer(currentLine);

				actions.get(possibleActionName).accept(tokenizer,
						enviroment);

				currentLine = getNextCommand(inputSource, commandNumber);

				continue;
			}

			// Build an AST from the string expression
			ITree<IDiceASTNode> builtAST;

			IFunctionalList<String> preparedTokens =
					DiceExpressionPreparer.prepareCommand(currentLine);

			try {
				builtAST = DiceASTParser.createFromString(preparedTokens);
			} catch (IllegalStateException isex) {
				System.out.println("ERROR: " + isex.getLocalizedMessage());

				currentLine = getNextCommand(inputSource, commandNumber);

				continue;
			}

			int sampleRoll;

			ITree<IDiceASTNode> transformedAST =
					transformAST(builtAST, enviroment);

			try {
				sampleRoll = DiceASTEvaluator.evaluateAST(transformedAST,
						enviroment);

				enviroment.put("last", transformedAST);
			} catch (UnsupportedOperationException usex) {
				System.out.println("ERROR: " + usex.getLocalizedMessage());

				currentLine = getNextCommand(inputSource, commandNumber);

				continue;
			}

			// Print out results
			System.out.println("\tParsed: " + builtAST.toString());
			System.out
					.println("\tEvaluated: " + transformedAST.toString());
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
			IFunctionalMap<String, ITree<IDiceASTNode>> enviroment) {
		ITree<IDiceASTNode> optimizedTree =
				optimizer.optimizeTree(builtAST, enviroment);

		ITree<IDiceASTNode> condensedTree =
				OperationCondenser.condense(optimizedTree);

		ITree<IDiceASTNode> sanitizedTree = DiceASTReferenceSanitizer
				.sanitize(condensedTree, enviroment);

		return sanitizedTree;
	}

	private static String getNextCommand(Scanner inputSource,
			int commandNumber) {
		System.out.print("\ndice-lang-" + commandNumber + "> ");

		return inputSource.nextLine();
	}
}
