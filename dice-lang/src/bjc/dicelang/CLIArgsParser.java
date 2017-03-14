package bjc.dicelang;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

import static bjc.dicelang.Errors.ErrorKey.*;

/**
 * Parse CLI arguments.
 * 
 * @author Ben Culkin
 * 
 */
public class CLIArgsParser {
	/**
	 * Parse the provided set of CLI arguments.
	 * 
	 * @param args The CLI arguments to parse.
	 * @param eng The engine to affect with parsing.
	 * 
	 * @return Whether or not to continue to the DiceLang repl.
	 */
	public static boolean parseArgs(String[] args, DiceLangEngine eng) {
		if (args.length < 0) {
			return true;
		}

		if (args.length == 1 && (args[0].equals("--help") || args[0].equals("-h"))) {
			System.out.println("Help for DiceLang v0.2 CLI arguments:");
			System.out.println("\t-d or --debug\tTurn on debug mode.");
			System.out.println("\t\tTurns on debug mode, which prints additional information that may be useful.");
			System.out.println("\t-nd or --no-debug\tTurn off debug mode.");
			System.out.println("\t\tTurns off debug mode.");
			System.out.println("\t-po or --postfix\tTurn on postfix mode.");
			System.out.println("\t\tTurns on postfix mode, which disables the shunter.");
			System.out.println("\t-npo or --no-postfix\tTurn off postfix mode.");
			System.out.println("\t\tTurns off postfix mode.");
			System.out.println("\t-pr or --prefix\tTurn on prefix mode.");
			System.out.println("\t\tTurns on prefix mode, which reverses the expression instead of shunting it.");
			System.out.println("\t-npr or --no-prefix\tTurn off prefix mode.");
			System.out.println("\t\tTurns off prefix mode.");
			System.out.println("\t-se or --step-eval\tTurn on step-eval mode.");
			System.out.println("\t\tTurns on step-evaluation, which shows the evaluation process step-by-step. Currently slightly broken.");
			System.out.println("\t-nse or --no-step-eval\tTurn off step-eval mode.");
			System.out.println("\t\tTurns off step-evaluation.");
			System.out.println("\t-D or ");
			System.exit(0);
		}

		for (int i = 0; i < args.length; i++) {
			String arg = args[i];

			boolean succ = true;

			switch (arg) {
			case "-d":
			case "--debug":
				if (!eng.toggleDebug()) {
					eng.toggleDebug();
				}
				break;
			case "-nd":
			case "--no-debug":
				if (eng.toggleDebug()) {
					eng.toggleDebug();
				}
				break;
			case "-po":
			case "--postfix":
				if (!eng.togglePostfix()) {
					eng.togglePostfix();
				}
				break;
			case "-npo":
			case "--no-postfix":
				if (eng.togglePostfix()) {
					eng.togglePostfix();
				}
				break;
			case "-pr":
			case "--prefix":
				if (!eng.togglePrefix()) {
					eng.togglePrefix();
				}
				break;
			case "-npr":
			case "--no-prefix":
				if (eng.togglePrefix()) {
					eng.togglePrefix();
				}
				break;
			case "-se":
			case "--stepeval":
				if (!eng.toggleStepEval()) {
					eng.toggleStepEval();
				}
				break;
			case "-nse":
			case "--no-stepeval":
				if (eng.toggleStepEval()) {
					eng.toggleStepEval();
				}
				break;
			case "-D":
			case "--define":
				i = simpleDefine(i, args, eng);
				if (i == -1) {
					return false;
				}
				break;
			case "-df":
			case "--define-file":
				i = defineFile(i, args, eng);
				if (i == -1) {
					return false;
				}
				break;
			case "-ctf":
			case "--compiler-tweak-file":
				/*
				 * @TODO not yet implemented
				 */
			default:
				Errors.inst.printError(EK_CLI_UNARG, arg);
				return false;
			}
		}

		return true;
	}

	private static int simpleDefine(int i, String[] args, DiceLangEngine eng) {
		if (i >= (args.length - 1)) {
			Errors.inst.printError(EK_CLI_MISARG, "define");
			return -1;
		}

		if (i >= (args.length - 2)) {
			Define dfn = new Define(5, false, false, false, null, args[i + 1], Arrays.asList(""));

			if (dfn.inError) {
				return -1;
			}

			eng.addLineDefine(dfn);
			return i + 1;
		}

		Define dfn = new Define(5, false, false, false, null, args[i + 1], Arrays.asList(args[i + 2]));

		if (dfn.inError) {
			return -1;
		}

		eng.addLineDefine(dfn);
		return i + 2;
	}

	private static int defineFile(int i, String[] args, DiceLangEngine eng) {
		if (i >= (args.length - 1)) {
			Errors.inst.printError(EK_CLI_MISARG, "define-file");
			return -1;
		}

		String fName = args[i + 1];

		try (FileInputStream fis = new FileInputStream(fName)) {
			try (Scanner scan = new Scanner(fis)) {
				while (scan.hasNextLine()) {
					String ln = scan.nextLine();

					Define dfn = parseDefine(ln.substring(ln.indexOf(' ')));
					if (dfn == null || dfn.inError) {
						return -1;
					}

					if (ln.startsWith("line")) {
						eng.addLineDefine(dfn);
					} else if (ln.startsWith("token")) {
						eng.addTokenDefine(dfn);
					} else {
						String defnType = ln.substring(0, ln.indexOf(' '));

						Errors.inst.printError(EK_CLI_INVDFNTYPE, defnType);
						return -1;
					}
				}
			}
		} catch (FileNotFoundException fnfex) {
			Errors.inst.printError(EK_MISC_NOFILE, fName);
			return -1;
		} catch (IOException ioex) {
			Errors.inst.printError(EK_MISC_IOEX, fName);
			return -1;
		}

		return i + 1;
	}

	private static Define parseDefine(String ln) {
		Define res = null;

		// @TODO move this functionality from DiceLangConsole to some
		// common ground where it can be used by both functions
		return res;
	}
}
