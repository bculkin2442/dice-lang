package bjc.dicelang;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

import static bjc.dicelang.Errors.ErrorKey.*;

public class CLIArgsParser {
	public static boolean parseArgs(String[] args, DiceLangEngine eng) {
		if(args.length < 0) return true;

		if(args.length == 1 && (args[0].equals("--help") || args[0].equals("-h"))) {
			/*
			 * @TODO show help
			 */
			System.exit(0);
		}

		for(int i = 0; i < args.length; i++) {
			String arg = args[i];

			boolean succ = true;

			switch(arg) {
				case "-d":
				case "--debug":
					if(!eng.toggleDebug()) eng.toggleDebug();
					break;
				case "-nd":
				case "--no-debug":
					if(eng.toggleDebug()) eng.toggleDebug();
					break;
				case "-po":
				case "--postfix":
					if(!eng.togglePostfix()) eng.togglePostfix();
					break;
				case "-npo":
				case "--no-postfix":
					if(eng.togglePostfix()) eng.togglePostfix();
					break;
				case "-pr":
				case "--prefix":
					if(!eng.togglePrefix()) eng.togglePrefix();
					break;
				case "-npr":
				case "--no-prefix":
					if(eng.togglePrefix()) eng.togglePrefix();
					break;
				case "-se":
				case "--stepeval":
					if(!eng.toggleStepEval()) eng.toggleStepEval();
					break;
				case "-nse":
				case "--no-stepeval":
					if(eng.toggleStepEval()) eng.toggleStepEval();
					break;
				case "-D":
				case "--define":
					i = simpleDefine(i, args, eng);
					if(i == -1) return false;
					break;					
				case "-df":
				case "--define-file":
					i = defineFile(i, args, eng);
					if(i == -1) return false;
					break;
				default:
					Errors.inst.printError(EK_CLI_UNARG, arg);
					return false;
			}
		}

		return true;
	}

	private static int simpleDefine(int i, String[] args, DiceLangEngine eng) {
		if(i >= (args.length - 1)) {
			Errors.inst.printError(EK_CLI_MISARG, "define");
			return -1;
		}

		if(i >= (args.length - 2)) {
			Define dfn = new Define(5, false, false, false, null, args[i + 1], Arrays.asList(""));
			
			if(dfn.inError) return -1;
			eng.addLineDefine(dfn);
			return i + 1;
		}

		Define dfn = new Define(5, false, false, false, null, args[i + 1], Arrays.asList(args[i + 2]));
		if(dfn.inError) return -1;
		eng.addLineDefine(dfn);
		return i + 2;
	}

	private static int defineFile(int i, String[] args, DiceLangEngine eng) {
		if(i >= (args.length - 1)) {
			Errors.inst.printError(EK_CLI_MISARG, "define-file");
			return -1;
		}

		String fName = args[i + 1];

		try(FileInputStream fis = new FileInputStream(fName)) {
			try(Scanner scan = new Scanner(fis)) {
				while(scan.hasNextLine()) {
					String ln = scan.nextLine();
	
					Define dfn = parseDefine(ln.substring(ln.indexOf(' ')));
					if(dfn == null || dfn.inError) return -1;
	
					if(ln.startsWith("line")) {
						eng.addLineDefine(dfn);
					} else if(ln.startsWith("token")) {
						eng.addTokenDefine(dfn);
					} else {
						Errors.inst.printError(EK_CLI_INVDFNTYPE, ln.substring(0, ln.indexOf(' ')));
						return -1;
					}
				}
			}
		} catch (FileNotFoundException fnfex) {
			Errors.inst.printError(EK_CLI_NOFILE, fName);
			return -1;
		} catch (IOException ioex) {
			Errors.inst.printError(EK_CLI_IOEX);
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
