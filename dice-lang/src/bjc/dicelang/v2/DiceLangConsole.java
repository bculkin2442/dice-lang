package bjc.dicelang.v2;

import java.util.Scanner;

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
			System.out.printf("\tRaw command: %s\n", comm);

			boolean success = eng.runCommand(comm);

			if(success) 
				System.out.println("Command completed succesfully");
			else
				System.out.println("Command execution failed");

			commandNumber += 1;

			System.out.printf("(%d) dice-lang> ", commandNumber);
			comm = scn.nextLine();
		}

		scn.close();
	}

	public static void main(String[] args) {
		DiceLangConsole console = new DiceLangConsole(args);

		console.run();
	}
}
