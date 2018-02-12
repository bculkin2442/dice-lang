package bjc.dicelang.scl;

import bjc.utils.funcdata.FunctionalList;
import bjc.utils.funcdata.IList;

import java.util.Scanner;

/**
 * Implement a SCL REPL
 *
 * @author Ben Culkin
 */
public class StreamControlConsole {
	/*
	 * @TODO 10/08/17 :SCLArgs Do something useful with the CLI args.
	 *
	 */
	/**
	 * Main method
	 *
	 * @param args
	 *        Unused CLI args.
	 */
	public static void main(String[] args) {
		/*
		 * Initialize vars.
		 * 
		 * We can get away with passing the null, because StreamEngine doesn't reference
		 * any parts of DiceLangEngine.
		 */
		StreamEngine sengine = new StreamEngine(null);
		StreamControlEngine sclengine = new StreamControlEngine(sengine);
		Scanner scn = new Scanner(System.in);

		/* Get input from the user. */
		System.out.print("Enter a SCL command string (blank to exit): ");

		/* Process it. */
		while (scn.hasNextLine()) {
			String ln = scn.nextLine().trim();

			if (ln.equals("")) {
				/* Ignore empty lines. */
				break;
			}

			/* Break the token into strings. */
			IList<String> res = new FunctionalList<>();
			String[] tokens = ln.split(" ");

			/* Run the stream engine on the tokens. */
			boolean succ = sengine.doStreams(tokens, res);
			if (!succ) {
				System.out.printf("ERROR: Stream engine failed for line '%s'\n", ln);
				continue;
			}

			/* Run the command through SCL. */
			tokens = res.toArray(new String[res.getSize()]);
			succ = sclengine.runProgram(tokens);
			if (!succ) {
				System.out.printf("ERROR: SCL engine failed for line '%s'\n", ln);
				continue;
			}

			/* Prompt again. */
			System.out.print("Command string executed succesfully.\n\n");
			System.out.print("Enter a SCL command string (blank to exit): ");
		}

		scn.close();
	}
}
