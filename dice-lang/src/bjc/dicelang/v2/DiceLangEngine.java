package bjc.utils.dicelang.v2;

import bjc.utils.funcdata.FunctionalList;
import bjc.utils.funcdata.FunctionalMap;
import bjc.utils.funcdata.FunctionalStringTokenizer;
import bjc.utils.funcdata.IList;
import bjc.utils.funcdata.IMap;
import bjc.utils.funcutils.ListUtils;

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
			.fromString(currentLine)
			.toList();

		// Will hold tokens with string literals removed
		IList<String> destringed = new FunctionalList<>();

		// Where we keep the string literals
		// @TODO put these in the sym-table early instead
		// 		 once there is a sym-table
		IMap<String, String> stringLiterals = new FunctionalMap<>();

		// Are we parsing a string literal?
		boolean stringMode = false;

		// The current string literal
		StringBuilder currentLiteral = new StringBuilder();

		for(String token : tokens.toIterable()) {
			String[] tokenParts = token.split("^(?!\\\\\")\"");

			if(tokenParts.length == 1) {
				// Insert token into correct place
				if(stringMode) {
					currentLiteral.add(tokenParts[0]);
				} else {
					destringed.add(tokenParts[0]);
				}
			} else {
				// Handle multiple "'s in a token
				for(String stringPart : tokenParts) {
					// Insert token into correct place
					if(stringMode) {
						currentLiteral.add(stringPart);
					} else {
						destringed.add(stringPart);
					}

					// We found a quote. Toggle string mode
					// and collect the literal
					stringMode = !stringMode;

					if(debugMode)
						System.out.printf("DEBUG: Parsed string"
								+ " literal (" 
								+ currentLiteral.toString() + ")");

					stringLiterals.put("stringLiteral"
							+ nextLiteral,
							currentLiteral.toString());
					destringed.add("stringLiteral" + nextLiteral);

					nextLiteral += 1;
					currentLiteral = new StringBuilder();
				}
			}
		}

		if(stringMode) {
			System.out.printf("\tERROR: Unclosed string literal (%s"
					+ ").\n", currentLiteral.toString());
		}

		if(debugMode)
			System.out.println("Command after destringing: "
					+ destringed.toString());
	}
}
