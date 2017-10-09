package bjc.dicelang;

import static bjc.dicelang.Errors.ErrorKey.EK_CLI_INVDFNTYPE;
import static bjc.dicelang.Errors.ErrorKey.EK_CLI_MISARG;
import static bjc.dicelang.Errors.ErrorKey.EK_CLI_UNARG;
import static bjc.dicelang.Errors.ErrorKey.EK_MISC_IOEX;
import static bjc.dicelang.Errors.ErrorKey.EK_MISC_NOFILE;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

import bjc.dicelang.util.ResourceLoader;

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
	 * @param args
	 *                The CLI arguments to parse.
	 * @param eng
	 *                The engine to affect with parsing.
	 *
	 * @return Whether or not to continue to the DiceLang repl.
	 */
	public static boolean parseArgs(final String[] args, final DiceLangEngine eng) {
		if (args.length < 0) {
			return true;
		}

		if (args.length == 1 && (args[0].equals("--help") || args[0].equals("-h"))) {
			for (final String lne : ResourceLoader.loadHelpFile("cli")) {
				System.out.println(lne);
			}

			System.exit(0);
		}

		for (int i = 0; i < args.length; i++) {
			final String arg = args[i];

			/*
			 * @TODO 10/08/17 Ben Culkin :CLIArgRefactor
			 * 	Use whatever library gets added to BJC-Utils for
			 * 	this, and extend these to do more things.
			 */
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
				/* @NOTE
				 * 	Not yet implemented.
				 */
			default:
				Errors.inst.printError(EK_CLI_UNARG, arg);
				return false;
			}
		}

		return true;
	}

	/* Handle parsing a simple define. */
	private static int simpleDefine(final int i, final String[] args,
	                                final DiceLangEngine eng) {
		/* :DefineRefactor */

		if (i >= args.length - 1) {
			Errors.inst.printError(EK_CLI_MISARG, "define");
			return -1;
		}

		if (i >= args.length - 2) {
			final Define dfn = new Define(5, false, false, false, null, args[i + 1],
			                              Arrays.asList(""));

			if (dfn.inError) {
				return -1;
			}

			eng.addLineDefine(dfn);
			return i + 1;
		}

		final Define dfn = new Define(5, false, false, false, null, args[i + 1],
		                              Arrays.asList(args[i + 2]));

		if (dfn.inError) {
			return -1;
		}

		eng.addLineDefine(dfn);
		return i + 2;
	}

	/* Load a series of defines from a file. */
	private static int defineFile(final int i, final String[] args,
	                              final DiceLangEngine eng) {
		if (i >= args.length - 1) {
			Errors.inst.printError(EK_CLI_MISARG, "define-file");
			return -1;
		}

		final String fName = args[i + 1];

		try (FileInputStream fis = new FileInputStream(fName)) {
			try (Scanner scan = new Scanner(fis)) {
				while (scan.hasNextLine()) {
					final String ln = scan.nextLine();

					final Define dfn = parseDefine(ln.substring(ln.indexOf(' ')));

					if (dfn == null || dfn.inError) {
						return -1;
					}

					if (ln.startsWith("line")) {
						eng.addLineDefine(dfn);
					} else if (ln.startsWith("token")) {
						eng.addTokenDefine(dfn);
					} else {
						final String defnType = ln.substring(0, ln.indexOf(' '));

						Errors.inst.printError(EK_CLI_INVDFNTYPE, defnType);
						return -1;
					}
				}
			}
		} catch (final FileNotFoundException fnfex) {
			Errors.inst.printError(EK_MISC_NOFILE, fName);
			return -1;
		} catch (final IOException ioex) {
			Errors.inst.printError(EK_MISC_IOEX, fName);
			return -1;
		}

		return i + 1;
	}

	private static Define parseDefine(final String ln) {
		final Define res = null;

		/* :DefineRefactor */
		return res;
	}
}
