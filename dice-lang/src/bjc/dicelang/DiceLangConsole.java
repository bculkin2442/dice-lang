package bjc.dicelang;

import static bjc.dicelang.Errors.ErrorKey.*;

import java.io.IOException;
import java.util.List;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jline.ConsoleReader;
import jline.Terminal;

public class DiceLangConsole {
	private int commandNumber;

	private DiceLangEngine eng;

	private ConsoleReader read;

	public DiceLangConsole(String[] args) {
		commandNumber = 0;

		eng = new DiceLangEngine();

		if(!CLIArgsParser.parseArgs(args, eng)) System.exit(1);

		Terminal.setupTerminal();
	}

	public void run() {
		try {
			read = new ConsoleReader();
		} catch(IOException ioex) {
			System.out.println("ERROR: Console init failed");
			return;
		}

		System.out.println("dice-lang v0.2");

		String comm = null;

		try {
			comm = read.readLine(String.format("(%d) dice-lang> ", commandNumber));
		} catch (IOException ioex) {
			System.out.println("ERROR: I/O failed");
			return;
		}

		while(!comm.equals("quit") && !comm.equals("exit")) {
			if(comm.startsWith("pragma")) {
				boolean success = handlePragma(comm.substring(7));

				if(success) System.out.println("Pragma completed succesfully");
				else        System.out.println("Pragma execution failed");
			} else {
				System.out.printf("\tRaw command: %s\n", comm);

				boolean success = eng.runCommand(comm);

				if(success) System.out.println("Command completed succesfully");
				else        System.out.println("Command execution failed");

				commandNumber += 1;
			}

			try {
				comm = read.readLine(String.format("(%d) dice-lang> ", commandNumber));
			} catch (IOException ioex) {
				System.out.println("ERROR: I/O failed");
				return;
			}
		}
	}

	private boolean handlePragma(String pragma) {
		System.out.println("\tRaw pragma: " + pragma);

		String pragmaName = null;
		int firstIndex = pragma.indexOf(' ');
		if(firstIndex == -1) {
			pragmaName = pragma;
		} else {
			pragmaName = pragma.substring(0, firstIndex);
		}

		switch(pragmaName) {
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
	
	private boolean helpMode(String pragma) {
		switch(pragma.trim()) {
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

		// Help always works
		return true;
	}

	/*
	 * Matches slash-delimited strings
	 * 		(like /text/ or /text\/text/)
	 *		Uses the "normal* (special normal*)*" pattern style
	 *		recommended in 'Mastering regular expressions'
	 *		Here, the normal is 'anything but a forward or backslash'
	 *		(in regex, thats '[^/\\]') and the special is 'an escaped forward slash'
	 *		(in regex, thats '\\\\/')
	 *
	 *		Then, we just follow the pattern, escape it for java strings, and
	 *		add the enclosing slashes
	 */
	private Pattern slashPattern = Pattern.compile("/((?:\\\\.|[^/\\\\])*)/");

	private boolean defineMode(String defineText) {
		int firstIndex    = defineText.indexOf(' ');
		int secondIndex   = defineText.indexOf(' ', firstIndex  + 1);
		int thirdIndex    = defineText.indexOf(' ', secondIndex + 1);
		int fourthIndex   = defineText.indexOf(' ', thirdIndex  + 1);
		int fifthIndex    = defineText.indexOf(' ', fourthIndex + 1);
		int sixthIndex    = defineText.indexOf(' ', fifthIndex + 1);

		if(firstIndex == -1) {
			Errors.inst.printError(EK_CONS_INVDEFINE, "(no priority)");
			return false;
		} else if(secondIndex == -1) {
			Errors.inst.printError(EK_CONS_INVDEFINE, "(no define type)");
			return false;
		} else if(thirdIndex == -1) {
			Errors.inst.printError(EK_CONS_INVDEFINE, "(no recursion type)");
			return false;
		} else if(fourthIndex == -1) {
			Errors.inst.printError(EK_CONS_INVDEFINE, "(no guard type)");
			return false;
		} else if(fifthIndex == -1) {
			Errors.inst.printError(EK_CONS_INVDEFINE, "(no circularity)");
			return false;
		} else if(sixthIndex == -1) {
			Errors.inst.printError(EK_CONS_INVDEFINE, "(no patterns)");
			return false;
		}

		int priority = Integer.parseInt(defineText.substring(0, firstIndex));

		String defineType = defineText.substring(firstIndex + 1, secondIndex);

		Define.Type type;
		boolean     subMode = false;

		switch(defineType) {
			case "line":
				type = Define.Type.LINE;
				break;
			case "token":
				type = Define.Type.TOKEN;
				break;
			case "subline":
				type    = Define.Type.LINE;
				subMode = true;
				break;
			case "subtoken":
				type    = Define.Type.TOKEN;
				subMode = true;
				break;
			default:
				Errors.inst.printError(EK_CONS_INVDEFINE, "(unknown type)");
				return false;
		}

		boolean doRecur    = defineText.substring(secondIndex + 1, thirdIndex)
			.equalsIgnoreCase("true");
		boolean hasGuard   = defineText.substring(thirdIndex + 1, fourthIndex)
			.equalsIgnoreCase("true");
		boolean isCircular = defineText.substring(thirdIndex + 1, fourthIndex)
			.equalsIgnoreCase("true");

		String pats = defineText.substring(fifthIndex + 1).trim();
		Matcher patMatcher = slashPattern.matcher(pats);

		String guardPattern = null;

		if(hasGuard) {
			if(!patMatcher.find()) {
				Errors.inst.printError(EK_CONS_INVDEFINE, "(no guard pattern)");
				return false;
			}

			guardPattern = patMatcher.group(1);
		}

		if(!patMatcher.find()) {
			Errors.inst.printError(EK_CONS_INVDEFINE, "(no search pattern)");
			return false;
		}

		String searchPattern = patMatcher.group(1);
		List<String> replacePatterns = new LinkedList<>();

		while(patMatcher.find()) {
			replacePatterns.add(patMatcher.group(1));
		}

		Define dfn = new Define(priority, subMode, doRecur, isCircular,
				guardPattern, searchPattern, replacePatterns);
		
		if(dfn.inError) return false;

		if(type == Define.Type.LINE) {
			eng.addLineDefine(dfn);
		} else {
			eng.addTokenDefine(dfn);
		}

		return true;
	}

	public static void main(String[] args) {
		DiceLangConsole console = new DiceLangConsole(args);

		console.run();
	}
}