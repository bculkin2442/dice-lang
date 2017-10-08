package bjc.dicelang.scl;

import bjc.utils.funcdata.FunctionalList;
import bjc.utils.funcdata.IList;

import java.util.Iterator;
import java.util.Scanner;

import java.util.function.Supplier;

public class StreamControlConsole {
	public static void main(String[] args) {
		/* Initialize vars. */
		/* We're not using the DiceLangEngine in the streams yet. */
		StreamEngine sengine = new StreamEngine(null);
		StreamControlEngine sclengine = new StreamControlEngine(sengine);
		Scanner scn = new Scanner(System.in);

		/* Get input from the user. */
		System.out.print("Enter a SCL command string (blank to exit): ");

		while (scn.hasNextLine()) {
			String ln = scn.nextLine();

			if (ln.trim().equals("")) {
				break;
			}

			/* Break the token into strings. */
			IList<String> res = new FunctionalList<>();
			String[] tokens   = ln.split(" ");

			/* Run the stream engine on the tokens. */
			boolean succ = sengine.doStreams(tokens, res);

			if (!succ) {
				continue;
			}

			/* Run the command through SCL. */
			tokens = res.toArray(new String[res.getSize()]);
			succ   = sclengine.runProgram(tokens);

			if (!succ) {
				continue;
			}

			/* Prompt again. */
			System.out.print("Command string executed succesfully.\n\n");
			System.out.print("Enter a SCL command string (blank to exit): ");
		}
	}
}
