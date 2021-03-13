package bjc.dicelang.neodice.commands;

import static bjc.dicelang.neodice.statements.VoidStatementValue.*;

import java.util.*;

import bjc.dicelang.neodice.*;
import bjc.dicelang.neodice.statements.*;

/**
 * Diebox help command. Unimplemented as of yet.
 * @author Ben Culkin
 *
 */
public class HelpCommand implements Command {
	@Override
	public StatementValue execute(Iterator<String> words, DieBoxCLI state) {
		state.output.println("help has not yet been implemented. TODO");
		return VOID_INST;
	}
	
	@Override
	public String shortHelp() {
		return "prints out help for commands";
	}
	
	@Override
	public String longHelp() {
		return "Invoke the help system. Unfortunately, not yet implemented";
	}
}