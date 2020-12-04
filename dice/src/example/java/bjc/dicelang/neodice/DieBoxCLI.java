package bjc.dicelang.neodice;

import static bjc.dicelang.neodice.statements.BooleanStatementValue.*;
import static bjc.dicelang.neodice.statements.VoidStatementValue.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import bjc.data.*;
import bjc.dicelang.neodice.commands.*;
import bjc.dicelang.neodice.statements.*;
import bjc.funcdata.*;

/**
 * Command-line interface for testing the neodice implementation.
 * 
 * @author Ben Culkin
 *
 */
public class DieBoxCLI {
	private static final Pattern INT_PATTERN = Pattern.compile("(?:\\+|-)?\\d+");
	/**
	 * The scanner we read input from.
	 */
	public Scanner input;
	/**
	 * The place we write output to.
	 */
	public PrintStream output;

	/**
	 * The current set of variable bindings
	 */
	public MapEx<String, StatementValue> bindings = new FunctionalMap<>();

	/**
	 * The current source of random numbers.
	 */
	public Random rng = new Random();

	/**
	 * The built-in diebox commands.
	 */
	public static final MapEx<String, Command> builtInCommands;
	/**
	 * The built-in diebox literal formers.
	 */
	public static final MapEx<String, Command> builtInliterals;

	/**
	 * The current set of diebox commands.
	 */
	public final MapEx<String, Command> commands;
	/**
	 * The current set of diebox literal-formers.
	 */
	public final MapEx<String, Command> literals;

	private int numStatements = 0;

	/**
	 * Whether or not to print out a prompt before asking for input
	 */
	public boolean doPrompt = true;

	/**
	 * Whether or not to output the results of each command.
	 */
	public boolean doOutput = true;

	/**
	 * Should warning messages be printed?
	 */
	public boolean doWarn = true;

	static {
		// Initialize all of our literal-formers
		builtInliterals = new FunctionalMap<>();

		builtInliterals.put("void", new LiteralCommand(VOID_INST,
				"the unique instance of type VOID",
				"Returns a reference to the unique instance of type VOID."));
		builtInliterals.put("true", new LiteralCommand(TRUE_INST,
				"the unique true value of type BOOLEAN",
				"Returns a reference to the unique true instance of type BOOLEAN"));
		builtInliterals.put("false", new LiteralCommand(FALSE_INST,
				"the unique false value of type BOOLEAN",
				"Returns a reference to the unique false instance of type BOOLEAN"));

		builtInliterals.deepFreeze();

		// Initialize all of our built-in commands
		builtInCommands = new FunctionalMap<>();

		builtInCommands.put("show-bindings", new ShowBindingsCommand());
		builtInCommands.put("bind", new BindCommand());
		builtInCommands.put("polyhedral-die", new PolyhedralDieCommand());
		builtInCommands.put("roll", new RollCommand());
		builtInCommands.put("help", new HelpCommand());
		builtInCommands.deepFreeze();
	}

	/**
	 * Create a new CLI for interacting with dice.
	 * 
	 * @param input
	 *               The place to read input from.
	 * @param output
	 *               The place to read output from.
	 */
	public DieBoxCLI(Scanner input, PrintStream output) {
		this.input = input;
		this.output = output;

		this.commands = builtInCommands.extend();
		this.literals = builtInliterals.extend();
	}

	/**
	 * Create a new CLI for interacting with dice.
	 * 
	 * @param input
	 *               The place to read input from.
	 * @param output
	 *               The place to read output from.
	 */
	public DieBoxCLI(InputStream input, OutputStream output) {
		this(new Scanner(input), new PrintStream(output));
	}

	/**
	 * Main method.
	 * 
	 * @param args
	 *             Currently unused CLI arguments.
	 */
	public static void main(String[] args) {
		Scanner input = new Scanner(System.in);
		PrintStream output = System.out;

		DieBoxCLI box = new DieBoxCLI(input, output);
		box.run();
	}

	private void run() {
		if (doPrompt) {
			output.println("diebox CLI - enter 'help' for help, 'exit' to exit");
		}

		if (doPrompt)
			output.printf("diebox(%d)> ", numStatements);
		while (input.hasNextLine()) {
			String nextLine = input.nextLine().trim();

			numStatements += 1;

			if (nextLine.equals(""))
				continue;
			// @FIXME Nov 15th, 2020 Ben Culkin :HardcodeExit
			// Exit should not be hard-coded like this
			if (nextLine.equals("exit"))
				break;

			String[] lineWords = nextLine.split("\\s+");
			Iterator<String> wordIter = new ArrayIterator<>(lineWords);
			try {
				StatementValue val = runStatement(wordIter);

				if (doOutput)
					output.printf("%s%s\n", doPrompt ? "==> " : "",
							val);
			} catch (DieBoxException dbex) {
				output.printf("ERROR (in statement %d): %s\n",
						numStatements, dbex.getMessage());
				Throwable curEx = dbex.getCause();
				while (curEx != null) {
					output.printf("...caused by: %s\n", curEx);

					curEx = dbex.getCause();
				}
			} catch (Exception ex) {
				output.printf("INTERNAL ERROR (in statement %d): %s\n",
						numStatements, ex.getMessage());
				ex.printStackTrace(output);
			}

			if (doPrompt)
				output.printf("diebox(%d)> ", numStatements);
		}

		input.close();
		output.close();
	}

	/**
	 * Extract an instance of {@link StatementValue} from a source of strings.
	 * 
	 * @param words The strings to use.
	 * 
	 * @return A statement value, parsed from the strings.
	 */
	public StatementValue runStatement(Iterator<String> words) {
		if (!words.hasNext()) {
			return VOID_INST;
		}

		String command = words.next().trim();
		DieBoxCLI state = this;

		if (command.startsWith("$")) {
			// All variable refs start with $
			String varName = command.substring(1);

			return bindings.get(varName)
					.orElseThrow(() -> new DieBoxException(
							"Attempted to reference non-existing variable %s",
							varName));
			// @TODO Nov 15th, 2020 Ben Culkin :Autovars
			// Perhaps something along the lines of 'auto-variables' (here
			// called 'spring-loaded variables') should be created? These
			// would be essentially values which invoke a given expression
			// whenever they are referenced.
		} else if (command.startsWith("#")) {
			// All literals/literal-formers start with #
			String actualCommand = command.substring(1);

			// Attempt to use a mapped literal/literal-former
			StatementValue val = literals.get(actualCommand)
					.map((com) -> com.execute(words, state))
					.orElseGet(() -> parseActualLiteral(actualCommand));

			return val;
		} else {
			// Attempt to use a mapped command first
			StatementValue val = commands.get(command)
					.map((com) -> com.execute(words, state))
					.orElseThrow(() -> handleUnknownCommand(command));
			return val;
		}
	}

	private DieBoxException handleUnknownCommand(String command) {
		StringBuilder msg = new StringBuilder("Unknown command ");
		msg.append(command);
		msg.append(".");
		
		if (INT_PATTERN.matcher(command).matches()) {
			msg.append("\n...Int literals must start with #, so try #");
			msg.append(command);
			msg.append(" instead.");
		}
		
		return new DieBoxException(msg.toString());
	}

	private StatementValue parseActualLiteral(String litText) {
		if (INT_PATTERN.matcher(litText).matches()) {
			try {
				int val = Integer.parseInt(litText);

				return new IntegerStatementValue(val);
			} catch (NumberFormatException nfex) {
				throw new DieBoxException(nfex,
						"Improper integer literal (%s)",
						litText);
			}
		} else {
			throw new DieBoxException("Unknown literal format (%s)",
					litText);
		}
	}
}