package bjc.dicelang.neodice.commands;

import java.util.*;

import bjc.dicelang.neodice.*;
import bjc.dicelang.neodice.statements.*;

public class LiteralCommand implements Command {
	private final StatementValue value;

	private final String shortHelp;
	private final String longHelp;

	public LiteralCommand(StatementValue value, String shortHelp, String longHelp) {
		this.value = value;
		
		this.shortHelp = shortHelp;
		this.longHelp = longHelp;
	}
	
	@Override
	public StatementValue execute(Iterator<String> words, DieBoxCLI state) {
		return value;
	}
	
	@Override
	public String shortHelp() {
		return shortHelp;
	}
	
	@Override
	public String longHelp() {
		return longHelp;
	}
}