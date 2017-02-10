package bjc.dicelang.v2;

import bjc.utils.data.IPair;
import bjc.utils.data.Pair;
import bjc.utils.funcdata.FunctionalList;
import bjc.utils.funcdata.FunctionalMap;
import bjc.utils.funcdata.FunctionalStringTokenizer;
import bjc.utils.funcdata.IList;
import bjc.utils.funcdata.IMap;
import bjc.utils.funcutils.ListUtils;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

public class DiceLangEngine {
	// Input rules for processing tokens
	private Deque<IPair<String, String>> opExpansionTokens;
	private Deque<IPair<String, String>> deaffixationTokens;

	// ID for generation of string literal variables
	private int nextLiteral;

	// Debug indicator
	private boolean debugMode;

	public DiceLangEngine() {
		opExpansionTokens = new LinkedList<>();

		opExpansionTokens.add(new Pair<>("+", "\\+"));
		opExpansionTokens.add(new Pair<>("-", "-"));
		opExpansionTokens.add(new Pair<>("*", "\\*"));
		opExpansionTokens.add(new Pair<>("/", "/"));
		opExpansionTokens.add(new Pair<>(":=", ":="));
		opExpansionTokens.add(new Pair<>("=>", "=>"));

		deaffixationTokens = new LinkedList<>();

		deaffixationTokens.add(new Pair<>("(", "\\("));
		deaffixationTokens.add(new Pair<>(")", "\\("));
		deaffixationTokens.add(new Pair<>("[", "\\["));
		deaffixationTokens.add(new Pair<>("]", "\\]"));

		nextLiteral = 1;
		
		// @TODO make configurable
		debugMode = true;
	}

	public boolean runCommand(String command) {
		// Split the command into tokens
		IList<String> tokens = FunctionalStringTokenizer
			.fromString(command)
			.toList();

		// Will hold tokens with string literals removed
		IList<String> destringed = new FunctionalList<>();

		// Where we keep the string literals
		// @TODO put these in the sym-table early instead
		// 		 once there is a sym-table
		IMap<String, String> stringLiterals = new FunctionalMap<>();

		boolean success = destringTokens(tokens, stringLiterals,
				destringed);

		if(!success) return success;

		if(debugMode) {
			System.out.println("\tCommand after destringing: "
					+ destringed.toString());

			System.out.println("\tString literals in table");
			stringLiterals.forEach((key, val) -> {
				System.out.printf("\t\tName: (%s)\tValue: (%s)\n",
					key, val);
			});
		}

		return true;
	}

	private boolean destringTokens(IList<String> tokens,
			IMap<String, String> stringLiterals,
			IList<String> destringed) {
		// Are we parsing a string literal?
		boolean stringMode = false;

		// The current string literal
		StringBuilder currentLiteral = new StringBuilder();
		String literalName = "stringLiteral";

		for(String token : tokens.toIterable()) {
			if(token.startsWith("\"")) {
				if(token.endsWith("\"")) {
					String litName = literalName + nextLiteral++;

					stringLiterals.put(litName,
						token.substring(1, token.length() - 1));
					destringed.add(litName);

					continue;
				}

				if(stringMode) {
					// @TODO make this not an error
					System.out.printf("\tPARSER ERROR: Initial" 
						+" quotes can only start strings\n");
				} else {
					currentLiteral.append(token.substring(1) + " ");

					stringMode = true;
				}
			} else if (token.endsWith("\"")) {
				if(!stringMode) {
					// @TODO make this not an error
					System.out.printf("\tPARSER ERROR: Terminal" 
						+" quotes can only end strings\n"); 
					return false;
				} else {
					currentLiteral.append(
						token.substring(0, token.length() - 1));

					String litName = literalName + nextLiteral++;

					stringLiterals.put(litName,
							currentLiteral.toString());
					destringed.add(litName);

					currentLiteral = new StringBuilder();

					stringMode = false;
				}
			} else if (token.contains("\"")) {
				if(token.contains("\\\"")) {
					if(stringMode) {
						currentLiteral.append(token + " ");
					} else {
						System.out.printf("\tERROR: Escaped quote "
							+ " outside of string literal\n");
						return false;
					}
				} else {
					// @TODO make this not an error
					System.out.printf("\tPARSER ERROR: A string"
							+ " literal must be delimited by spaces"
							+ " for now.\n");
					return false;
				}
			} else {
				if(stringMode) {
					currentLiteral.append(token + " ");
				} else {
					destringed.add(token);
				}
			}
		}

		if(stringMode) {
			System.out.printf("\tERROR: Unclosed string literal (%s"
					+ ").\n", currentLiteral.toString());

			return false;
		}

		return true;
	}
}
