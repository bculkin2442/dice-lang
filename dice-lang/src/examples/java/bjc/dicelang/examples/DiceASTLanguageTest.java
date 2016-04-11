package bjc.dicelang.examples;

import java.util.Scanner;

import bjc.dicelang.ast.DiceASTEvaluator;
import bjc.dicelang.ast.DiceASTParser;
import bjc.dicelang.ast.nodes.IDiceASTNode;

import bjc.utils.funcdata.FunctionalMap;
import bjc.utils.funcdata.IFunctionalList;
import bjc.utils.funcdata.IFunctionalMap;
import bjc.utils.parserutils.AST;

/**
 * Test interface for AST-based dice language
 * 
 * @author ben
 *
 */
public class DiceASTLanguageTest {
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
		IFunctionalMap<String, AST<IDiceASTNode>> enviroment =
				new FunctionalMap<>();

		while (!currentLine.equalsIgnoreCase("quit")) {
			// Build an AST from the string expression
			AST<IDiceASTNode> builtAST;

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

			try {
				sampleRoll =
						DiceASTEvaluator.evaluateAST(builtAST, enviroment);
			} catch (UnsupportedOperationException usex) {
				System.out.println("ERROR: " + usex.getLocalizedMessage());

				currentLine = getNextCommand(inputSource, commandNumber);

				continue;
			}

			// Print out results
			System.out.println("\tParsed: " + builtAST.toString());
			System.out.println("\t\tSample Roll: " + sampleRoll);

			// Increase the number of commands
			commandNumber++;

			currentLine = getNextCommand(inputSource, commandNumber);
		}

		System.out.println("Bye.");

		// Cleanup after ourselves
		inputSource.close();
	}

	private static String getNextCommand(Scanner inputSource,
			int commandNumber) {
		System.out.print("dice-lang-" + commandNumber + "> ");

		return inputSource.nextLine();
	}
}
