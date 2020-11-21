package bjc.dicelang.neodice.commands;

import java.util.*;
import java.util.function.*;

import bjc.dicelang.neodice.*;
import bjc.dicelang.neodice.statements.*;

/**
 * A single CLI command.
 * 
 * @author Ben Culkin
 *
 */
@FunctionalInterface
public interface Command {
	/**
	 * Execute this command.
	 * 
	 * @param words The remaining input.
	 * @param state The current state.
	 * 
	 * @return The result of executing this command.
	 */
	public StatementValue execute(Iterator<String> words, DieBoxCLI state);
	
	/**
	 * Get the 'short help' or usage summary for this command.
	 * 
	 * @return The short help for this command.
	 */
	default String shortHelp() {
		return "no short help";
	}
	
	/**
	 * Get the 'long help' or detailed usage for this command.
	 * 
	 * @return The long help for this command.
	 */
	default String longHelp() {
		return "no long help";
	}
	
	/**
	 * Create a new command, backed by a function.
	 * 
	 * @param executor The function which backs the command.
	 * @param shortHelp The short help string.
	 * @param longHelp The long help string.
	 * 
	 * @return A command backed by the function, with help.
	 */
	static Command newCommand(
			BiFunction<Iterator<String>, DieBoxCLI, StatementValue> executor,
			String shortHelp, String longHelp) {
		return new FunctionalCommand(executor, shortHelp, longHelp);
	}
}

class FunctionalCommand implements Command {
	private final BiFunction<Iterator<String>, DieBoxCLI, StatementValue> executor;
	private final String shortHelp;
	private final String longHelp;
	
	public FunctionalCommand(
			BiFunction<Iterator<String>, DieBoxCLI, StatementValue> executor, String shortHelp,
			String longHelp) {
		this.executor = executor;
		this.shortHelp = shortHelp;
		this.longHelp = longHelp;
	}

	@Override
	public StatementValue execute(Iterator<String> words, DieBoxCLI state) {
		return executor.apply(words, state);
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