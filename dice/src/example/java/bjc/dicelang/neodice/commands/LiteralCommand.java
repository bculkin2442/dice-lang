package bjc.dicelang.neodice.commands;

import java.util.*;

import bjc.dicelang.neodice.*;
import bjc.dicelang.neodice.statements.*;

/**
 * A command that produces a literal statement value.
 * 
 * This command will always produce the same statement value, so passing any sort
 * of mutable statement value to it is asking to be hurt.
 * 
 * @author Ben Culkin
 *
 */
public class LiteralCommand implements Command {
	private final StatementValue value;

	private final String shortHelp;
	private final String longHelp;

	/**
	 * Create a new command producing a literal statement value.
	 * 
	 * @param value The value this command returns.
	 * @param shortHelp The short-help (summary) for this command.
	 * @param longHelp The long-help (description) for this command.
	 */
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