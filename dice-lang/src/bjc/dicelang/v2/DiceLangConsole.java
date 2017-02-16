package bjc.dicelang.v2;

import java.util.List;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiceLangConsole {
	private int commandNumber;

	private DiceLangEngine eng;

	public DiceLangConsole(String[] args) {
		// @TODO do something with the args
		
		commandNumber = 0;

		eng = new DiceLangEngine();
	}

	public void run() {
		System.out.println("dice-lang v0.2");

		Scanner scn = new Scanner(System.in);

		System.out.printf("(%d) dice-lang> ", commandNumber);
		String comm = scn.nextLine();

		while(!comm.equals("quit") && !comm.equals("exit")) {
			if(comm.startsWith("pragma")) {
				boolean success = handlePragma(comm.substring(7));

				if(success) 
					System.out.println("Pragma completed succesfully");
				else
					System.out.println("Pragma execution failed");
			} else {
				System.out.printf("\tRaw command: %s\n", comm);

				boolean success = eng.runCommand(comm);

				if(success) 
					System.out.println("Command completed succesfully");
				else
					System.out.println("Command execution failed");

				commandNumber += 1;
			}

			System.out.printf("(%d) dice-lang> ", commandNumber);
			comm = scn.nextLine();
		}

		scn.close();
	}

	private boolean handlePragma(String pragma) {
		System.out.println("\tRaw pragma: " + pragma);

		switch(pragma) {
			case "debug":
				System.out.println("\tDebug mode is now " + eng.toggleDebug());
				break;
			case "postfix":
				System.out.println("\tPostfix mode is now " + eng.togglePostfix());
				break;
			case "define":
				return defineMode(pragma.substring(7));
			default:
				System.out.println("\tERROR: Unknown pragma: " + pragma);
				return false;
		}

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
	private Pattern slashPattern = Pattern.compile("/([^/\\\\]*(?:\\\\/(?:[^/\\\\])*)*)/");

	private boolean defineMode(String defineText) {
		int firstIndex    = defineText.indexOf(' ');
		int secondIndex   = defineText.indexOf(' ', firstIndex  + 1);
		int thirdIndex    = defineText.indexOf(' ', secondIndex + 1);
		int fourthIndex   = defineText.indexOf(' ', thirdIndex  + 1);
		int fifthIndex    = defineText.indexOf(' ', fourthIndex + 1);

		if(firstIndex == -1) {
			System.out.println("\tERROR: Improperly formatted define (no priority)");
			return false;
		} else if(secondIndex == -1) {
			System.out.println("\tERROR: Improperly formatted define (no define type)");
			return false;
		} else if(thirdIndex == -1) {
			System.out.println("\tERROR: Improperly formatted define (no recursion type)");
			return false;
		} else if(fourthIndex == -1) {
			System.out.println("\tERROR: Improperly formatted define (no guard type)");
			return false;
		} else if(fifthIndex == -1) {
			System.out.println("\tERROR: Improperly formatted define (no patterns)");
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
				System.out.println("\tERROR: Unknown define type "
						+ defineType);
				return false;
		}

		boolean doRecur = defineText.substring(secondIndex + 1, thirdIndex)
			.equalsIgnoreCase("true");
		boolean hasGuard = defineText.substring(thirdIndex + 1, fourthIndex).
			equalsIgnoreCase("true");

		String pats = defineText.substring(fourthIndex + 1);

		Matcher patMatcher = slashPattern.matcher(pats);

		String guardPattern = null;

		if(hasGuard) {
			if(!patMatcher.find()) {
				System.out.println("\tERROR: Improperly formatted define (no guard pattern)");
			}

			guardPattern = patMatcher.group(1);
		}

		if(!patMatcher.find()) {
			System.out.println("\tERROR: Improperly formatted define (no search pattern)");
		}

		String searchPattern = patMatcher.group(1);
		List<String> replacePatterns = new LinkedList<>();

		while(patMatcher.find()) {
			replacePatterns.add(patMatcher.group(1));
		}

		Define dfn = new Define(priority, subMode, doRecur, guardPattern, searchPattern, replacePatterns);
		
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
