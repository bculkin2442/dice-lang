package bjc.dicelang.examples;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import bjc.dicelang.DiceExpressionParser;
import bjc.dicelang.IDiceExpression;
import bjc.dicelang.ast.DiceASTExpression;
import bjc.dicelang.ast.DiceASTFreezer;
import bjc.dicelang.ast.DiceASTParser;
import bjc.dicelang.ast.IDiceASTNode;
import bjc.dicelang.ast.VariableDiceNode;
import bjc.utils.data.GenHolder;
import bjc.utils.funcdata.ITreePart.TreeLinearizationMethod;
import bjc.utils.parserutils.AST;

/**
 * A test of the AST based dice language
 * 
 * @author ben
 *
 */
public class DiceASTLanguageTest {
	private static final class LastChecker
			implements Consumer<IDiceASTNode> {
		private GenHolder<Boolean> canUpdateLast;

		public LastChecker(GenHolder<Boolean> canUpdateLast) {
			this.canUpdateLast = canUpdateLast;
		}

		@Override
		public void accept(IDiceASTNode tn) {
			if (tn instanceof VariableDiceNode && ((VariableDiceNode) tn)
					.getVariable().equals("last")) {
				canUpdateLast.transform((s) -> false);
			} else {
				canUpdateLast.transform((s) -> true);
			}
		}
	}

	private static Map<String, BiConsumer<String, DiceASTLanguageState>> acts;

	static {
		acts = new HashMap<>();

		acts.put("roll", DiceASTLanguageTest::rollReference);
		acts.put("env", DiceASTLanguageTest::printEnv);
		acts.put("freeze", DiceASTLanguageTest::freezeVar);
	}

	private static void freezeVar(String ln, DiceASTLanguageState stat) {
		String[] strangs = ln.split(" ");

		System.out.println("Freezing references in " + strangs[1]);

		stat.doWith((dep, env) -> {
			env.put(strangs[1], new DiceASTExpression(
					DiceASTFreezer.freezeAST(env.get(strangs[1]), env),
					env));
		});
	}

	/**
	 * @param ln
	 *            Unused parameter, kept in place to conform to expected
	 *            type sig
	 */
	private static void printEnv(String ln, DiceASTLanguageState stat) {
		System.out.println("Printing enviroment for debugging purposes.");

		stat.doWith((dep, env) -> env.forEach((key, exp) -> System.out
				.println("\tKey: " + key + "\tExp: " + exp.toString())));
	}

	private static void rollReference(String ln,
			DiceASTLanguageState stat) {
		String[] strangs = ln.split(" ");

		System.out.println("\tRolling dice expression " + strangs[1] + " "
				+ strangs[2] + " times.");

		int nRolls = Integer.parseInt(strangs[2]);

		IDiceExpression dexp =
				stat.merge((dep, env) -> env.get(strangs[1]));

		for (int i = 1; i <= nRolls; i++) {
			int roll = dexp.roll();

			System.out.println("\tRolled " + roll);
		}
	}

	/**
	 * Main method of class
	 * 
	 * @param args
	 *            Unused CLI args
	 */
	public static void main(String[] args) {
		Scanner scn = new Scanner(System.in);
		int i = 0;

		System.out.print("dice-lang-" + i + "> ");
		String ln = scn.nextLine();

		DiceASTParser dap = new DiceASTParser();

		DiceExpressionParser dep = new DiceExpressionParser();
		Map<String, DiceASTExpression> env = new HashMap<>();
		DiceASTLanguageState state = new DiceASTLanguageState(dep, env);

		while (!ln.equalsIgnoreCase("quit")) {
			String header = ln.split(" ")[0];

			if (acts.containsKey(header)) {
				acts.get(header).accept(ln, state);
			} else {

				AST<IDiceASTNode> builtAST = dap.buildAST(ln);
				DiceASTExpression exp =
						new DiceASTExpression(builtAST, env);

				System.out.println("\tParsed: " + exp.toString());
				System.out.println("\tSample Roll: " + exp.roll());

				GenHolder<Boolean> canUpdateLast = new GenHolder<>(false);

				exp.getAst().traverse(TreeLinearizationMethod.PREORDER,
						new LastChecker(canUpdateLast));

				if (canUpdateLast.unwrap((s) -> s)) {
					env.put("last", exp);
				}
			}

			i++;

			System.out.print("dice-lang-" + i + "> ");
			ln = scn.nextLine();
		}

		System.out.println("Bye.");
		scn.close();
	}
}
