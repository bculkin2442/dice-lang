package bjc.dicelang;

import static bjc.dicelang.Errors.ErrorKey.EK_CONS_INVDEFINE;
import static bjc.dicelang.Errors.ErrorKey.EK_CONS_INVPRAG;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jline.ConsoleReader;
import jline.Terminal;

/**
 * CLI interface to DiceLang
 *
 * @author EVE
 *
 */
public class DiceLangConsole {
	/* The number of commands executed so far. */
	private int commandNumber;
	/* The engine that executes commands. */
	private final DiceLangEngine eng;
	/* The place to read input from. */
	private ConsoleReader read;

	/**
	 * Create a new console.
	 *
	 * @param args
	 *                The CLI args for the console.
	 */
	public DiceLangConsole(final String[] args) {
		commandNumber = 0;
		eng = new DiceLangEngine();

		if (!CLIArgsParser.parseArgs(args, eng)) {
			System.exit(1);
		}

		Terminal.setupTerminal();
	}

	/** Run the console. */
	public void run() {
		/* Set up console. */
		try {
			read = new ConsoleReader();
		} catch (final IOException ioex) {
			System.out.println("ERROR: Console init failed");
			return;
		}

		/* Print greeting. */
		System.out.println("dice-lang v0.2");
		String comm = null;

		/* Read initial command. */
		try {
			comm = read.readLine(String.format("(%d) dice-lang> ", commandNumber));
		} catch (final IOException ioex) {
			System.out.println("ERROR: I/O failed");
			return;
		}

		/* Run commands. */
		/* @NOTE
		 * 	Should switch this to a do-while loop to reduce code
		 * 	duplication.
		 */
		while (!comm.equals("quit") && !comm.equals("exit")) {
			if (comm.startsWith("pragma")) {
				/* Run pragmas. */
				final boolean success = handlePragma(comm.substring(7));

				if (success) {
					System.out.println("Pragma completed succesfully");
				} else {
					System.out.println("Pragma execution failed");
				}
			} else {
				/* Run commands. */
				if (eng.debugMode) {
					System.out.printf("\tRaw command: %s\n", comm);
				}

				final boolean success = eng.runCommand(comm);

				if (success) {
					System.out.println("Command completed succesfully");
				} else {
					System.out.println("Command execution failed");
				}

				commandNumber += 1;
			}

			/* Read the next command. */
			try {
				comm = read.readLine(String.format("(%d) dice-lang> ", commandNumber));
			} catch (final IOException ioex) {
				System.out.println("ERROR: I/O failed");
				return;
			}
		}
	}

	/* Handle running pragmas. */
	private boolean handlePragma(final String pragma) {
		if (eng.debugMode) {
			System.out.println("\tRaw pragma: " + pragma);
		}

		/* Grab the name from the arguments. */
		String pragmaName = null;
		final int firstIndex = pragma.indexOf(' ');

		/* Handle argless pragmas. */
		if (firstIndex == -1) {
			pragmaName = pragma;
		} else {
			pragmaName = pragma.substring(0, firstIndex);
		}

		/* Run pragmas. */
		/*
		 * @TODO 10/09/17 Ben Culkin :PragmaRefactor
		 * 	Swap to using something that makes it easier to add
		 * 	pragmas.
		 */
		switch (pragmaName) {
		case "debug":
			System.out.println("\tDebug mode is now " + eng.toggleDebug());
			break;
		case "postfix":
			System.out.println("\tPostfix mode is now " + eng.togglePostfix());
			break;
		case "prefix":
			System.out.println("\tPrefix mode is now " + eng.togglePrefix());
			break;
		case "stepeval":
			System.out.println("\tStepeval mode is now" + eng.toggleStepEval());
			break;
		case "define":
			return defineMode(pragma.substring(7));
		case "help":
			return helpMode(pragma.substring(5));
		default:
			Errors.inst.printError(EK_CONS_INVPRAG, pragma);
			return false;
		}

		return true;
	}

	/* Run a help mode. */
	private static boolean helpMode(final String pragma) {
		/* Get the help topic. */
		switch (pragma.trim()) {
		case "help":
			System.out.println("\tGet help on pragmas");
			break;
		case "debug":
			System.out.println("\tToggle debug mode. (Output stage results)");
			break;
		case "postfix":
			System.out.println("\tToggle postfix mode. (Don't shunt tokens)");
			break;
		case "prefix":
			System.out.println("\tToggle prefix mode. (Reverse token order instead of shunting)");
			break;
		case "stepeval":
			System.out.println("\tToggle stepeval mode. (Print out evaluation progress)");
			break;
		case "define":
			System.out.println("\tAdd a macro rewrite directive.");
			System.out.println("\tdefine <priority> <type> <recursion> <guard> <circular> <patterns>...");
			break;
		default:
			System.out.println("\tNo help available for pragma " + pragma);
		}
		/* Help always works */
		return true;
	}

	/* Matches slash-delimited strings (like /text/ or /text\/text/). */
	private final Pattern slashPattern = Pattern.compile("/((?:\\\\.|[^/\\\\])*)/");

	/* Parse a define macro. */
	private boolean defineMode(final String defineText) {
		/* Grab all of the separator spaces. */
		final int firstIndex  = defineText.indexOf(' ');
		final int secondIndex = defineText.indexOf(' ', firstIndex  + 1);
		final int thirdIndex  = defineText.indexOf(' ', secondIndex + 1);
		final int fourthIndex = defineText.indexOf(' ', thirdIndex  + 1);
		final int fifthIndex  = defineText.indexOf(' ', fourthIndex + 1);
		final int sixthIndex  = defineText.indexOf(' ', fifthIndex  + 1);

		/*
		 * Error if we got something we didn't need, or didn't get
		 * something we need.
		 */
		if (firstIndex == -1) {
			Errors.inst.printError(EK_CONS_INVDEFINE, "(no priority)");
			return false;
		} else if (secondIndex == -1) {
			Errors.inst.printError(EK_CONS_INVDEFINE, "(no define type)");
			return false;
		} else if (thirdIndex == -1) {
			Errors.inst.printError(EK_CONS_INVDEFINE, "(no recursion type)");
			return false;
		} else if (fourthIndex == -1) {
			Errors.inst.printError(EK_CONS_INVDEFINE, "(no guard type)");
			return false;
		} else if (fifthIndex == -1) {
			Errors.inst.printError(EK_CONS_INVDEFINE, "(no circularity)");
			return false;
		} else if (sixthIndex == -1) {
			Errors.inst.printError(EK_CONS_INVDEFINE, "(no patterns)");
			return false;
		}

		/* Get the priority and define type. */
		final int priority = Integer.parseInt(defineText.substring(0, firstIndex));
		final String defineType = defineText.substring(firstIndex + 1, secondIndex);

		Define.Type type;
		boolean subMode = false;

		/* Parse the define type. */
		switch (defineType) {
		case "line":
			type = Define.Type.LINE;
			break;
		case "token":
			type = Define.Type.TOKEN;
			break;
		case "subline":
			type = Define.Type.LINE;
			subMode = true;
			break;
		case "subtoken":
			type = Define.Type.TOKEN;
			subMode = true;
			break;
		default:
			Errors.inst.printError(EK_CONS_INVDEFINE, "(unknown type)");
			return false;
		}

		/* Do we want this to be a recursive pattern? */
		final boolean doRecur    = defineText.substring(secondIndex + 1, thirdIndex)
		                           .equalsIgnoreCase("true");
		/* Do we want this pattern to have a guard? */
		final boolean hasGuard   = defineText.substring(thirdIndex + 1,  fourthIndex)
		                           .equalsIgnoreCase("true");
		/* Do we want this pattern to use circular replacements. */
		final boolean isCircular = defineText.substring(thirdIndex + 1,  fourthIndex)
		                           .equalsIgnoreCase("true");

		/* The part of the string that contains patterns. */
		final String pats = defineText.substring(fifthIndex + 1).trim();
		final Matcher patMatcher = slashPattern.matcher(pats);
		String guardPattern = null;

		if (hasGuard) {
			/* Grab the guard pattern. */
			if (!patMatcher.find()) {
				Errors.inst.printError(EK_CONS_INVDEFINE, "(no guard pattern)");
				return false;
			}

			guardPattern = patMatcher.group(1);
		}

		if (!patMatcher.find()) {
			/* Grab the search pattern. */
			Errors.inst.printError(EK_CONS_INVDEFINE, "(no search pattern)");
			return false;
		}

		final String searchPattern = patMatcher.group(1);
		final List<String> replacePatterns = new LinkedList<>();

		while (patMatcher.find()) {
			/* Grab the replacer patterns. */
			replacePatterns.add(patMatcher.group(1));
		}

		final Define dfn = new Define(priority, subMode, doRecur,
		                              isCircular, guardPattern, searchPattern,
		                              replacePatterns);

		if (dfn.inError) {
			return false;
		}

		/* Add the define to the proper place. */
		if (type == Define.Type.LINE) {
			eng.addLineDefine(dfn);
		} else {
			eng.addTokenDefine(dfn);
		}

		return true;
	}

	/**
	 * Main method.
	 *
	 * @param args
	 *                CLI arguments.
	 */
	public static void main(final String[] args) {
		final DiceLangConsole console = new DiceLangConsole(args);
		console.run();
	}
}