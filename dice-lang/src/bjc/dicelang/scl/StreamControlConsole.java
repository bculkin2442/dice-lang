package bjc.dicelang.scl;

import bjc.utils.funcdata.FunctionalList;
import bjc.utils.funcdata.IList;

import java.util.Scanner;

public class StreamControlConsole {
	public static void main(String[] args) {
		/*
		 * We're not using the DiceLangEngine in the streams yet.
		 */
		StreamEngine sengine = new StreamEngine(null);
		
		StreamControlEngine sclengine = new StreamControlEngine(sengine);

		Scanner scn = new Scanner(System.in);

		System.out.print("Enter a SCL command string (blank to exit): ");

		while(scn.hasNextLine()) {
			String ln = scn.nextLine();

			if(ln.trim().equals("")) break;

			IList<String> res = new FunctionalList<>();

			String[] tokens = ln.split(" ");

			boolean succ = sengine.doStreams(tokens, res);

			if(!succ) continue;

			tokens = res.toArray(new String[res.getSize()]);

			succ = sclengine.runProgram(tokens);

			if(!succ) continue;

			System.out.print("Command string executed succesfully.\n\n");

			System.out.print("Enter a SCL command string (blank to exit): ");
		}
	}
}
