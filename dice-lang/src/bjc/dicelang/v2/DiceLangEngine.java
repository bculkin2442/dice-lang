package bjc.dicelang.v2;

import bjc.utils.data.IPair;
import bjc.utils.data.Pair;
import bjc.utils.funcdata.FunctionalList;
import bjc.utils.funcdata.FunctionalMap;
import bjc.utils.funcdata.FunctionalStringTokenizer;
import bjc.utils.funcdata.IList;
import bjc.utils.funcdata.IMap;
import bjc.utils.funcutils.ListUtils;
import bjc.utils.funcutils.StringUtils;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static bjc.dicelang.v2.Token.Type.*;

public class DiceLangEngine {
	// Input rules for processing tokens
	private Deque<IPair<String, String>> opExpansionTokens;
	private Deque<IPair<String, String>> deaffixationTokens;

	// ID for generation
	private int nextLiteral;
	private int nextSym;

	// Debug indicator
	private boolean debugMode;

	// Shunter for token postfixing
	private Shunter shunt;

	// Tables for symbols
	private IMap<Integer, String> symTable;
	private IMap<Integer, String> stringLits;

	private IMap<String, Token.Type> litTokens;

	private final int MATH_PREC = 20;
	private final int DICE_PREC = 10;
	private final int EXPR_PREC = 0;

	public DiceLangEngine() {
		symTable   = new FunctionalMap<>();
		stringLits = new FunctionalMap<>();
		
		opExpansionTokens = new LinkedList<>();

		opExpansionTokens.add(new Pair<>("+", "\\+"));
		opExpansionTokens.add(new Pair<>("-", "-"));
		opExpansionTokens.add(new Pair<>("*", "\\*"));
		opExpansionTokens.add(new Pair<>("//", "//"));
		opExpansionTokens.add(new Pair<>("/", "/"));
		opExpansionTokens.add(new Pair<>(":=", ":="));
		opExpansionTokens.add(new Pair<>("=>", "=>"));

		deaffixationTokens = new LinkedList<>();

		deaffixationTokens.add(new Pair<>("(", "\\("));
		deaffixationTokens.add(new Pair<>(")", "\\)"));
		deaffixationTokens.add(new Pair<>("[", "\\["));
		deaffixationTokens.add(new Pair<>("]", "\\]"));

		nextLiteral = 1;

		// @TODO make configurable
		debugMode = true;

		shunt = new Shunter();

		litTokens = new FunctionalMap<>();

		litTokens.put("+", ADD);
		litTokens.put("-", SUBTRACT);
		litTokens.put("*", MULTIPLY);
		litTokens.put("/", DIVIDE);
		litTokens.put("//", IDIVIDE);
		litTokens.put("dg", DICEGROUP);
		litTokens.put("dc", DICECONCAT);
		litTokens.put("dl", DICELIST);
		litTokens.put("=>", LET);
		litTokens.put(":=", BIND);
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
			System.out.println("\tCommand after destringing: " + destringed.toString());

			System.out.println("\tString literals in table");

			stringLiterals.forEach((key, val) -> {
				System.out.printf("\t\tName: (%s)\tValue: (%s)\n",
					key, val);
			});
		}

		IList<String> semiExpandedTokens  = ListUtils.deAffixTokens(destringed, deaffixationTokens);
		IList<String> fullyExpandedTokens = ListUtils.splitTokens(semiExpandedTokens, opExpansionTokens);

		if(debugMode) {
			System.out.printf("\tCommand after token deaffixation: " 
					+ semiExpandedTokens.toString() + "\n");

			System.out.printf("\tCommand after token expansion: " 
					+ fullyExpandedTokens.toString() + "\n");
		}

		IList<Token> lexedTokens = new FunctionalList<>();

		for(String token : fullyExpandedTokens.toIterable()) {
			Token tk = lexToken(token, stringLiterals);

			if(tk == null) continue;
			else if(tk == Token.NIL_TOKEN) return false;
			else lexedTokens.add(tk);
		}

		if(debugMode)
			System.out.printf("\tCommand after tokenization: %s\n", lexedTokens.toString());

		IList<Token> shuntedTokens = new FunctionalList<>();
		success                    = shunt.shuntTokens(lexedTokens, shuntedTokens);

		if(!success) return false;

		if(debugMode)
			System.out.printf("\tCommand after shunting: %s\n", shuntedTokens.toString());

		return true;
	}

	private Token lexToken(String token, IMap<String, String> stringLts) {
		if(token.equals("")) return null;

		Token tk = Token.NIL_TOKEN;

		if(litTokens.containsKey(token)) {
			tk = new Token(litTokens.get(token));
		} else {
			switch(token) {
				case "(":
				case ")":
				case "[":
				case "]":
					   tk = tokenizeGrouping(token);
					   break;
				default:
					   tk = tokenizeLiteral(token, stringLts);
			}
		}

		return tk;
	}

	private Token tokenizeGrouping(String token) {
		Token tk = Token.NIL_TOKEN;

		if(StringUtils.containsOnly(token, "\\" + token.charAt(0))) {
			switch(token) {
				case "(":
					tk = new Token(OPAREN, token.length());
					break;
				case ")":
					tk = new Token(CPAREN, token.length());
					break;
				case "[":
					tk = new Token(OBRACKET, token.length());
					break;
				case "]":
					tk = new Token(CBRACKET, token.length());
					break;
			}
		}

		return tk;
	}

	private Pattern intMatcher          = Pattern.compile("\\A[\\-\\+]?\\d+\\Z");
	private Pattern hexadecimalMatcher  = Pattern.compile("\\A[\\-\\+]?0x[0-9A-Fa-f]+\\Z");
	private Pattern flexadecimalMatcher = Pattern.compile("\\A[\\-\\+]?[0-9][0-9A-Za-z]+B\\d{1,2}\\Z");
	private Pattern stringLitMatcher    = Pattern.compile("\\AstringLiteral(\\d+)\\Z");

	private Token tokenizeLiteral(String token, IMap<String, String> stringLts) {
		Token tk = Token.NIL_TOKEN;

		if(intMatcher.matcher(token).matches()) {
			tk = new Token(INT_LIT, Long.parseLong(token));
		} else if(hexadecimalMatcher.matcher(token).matches()) {
			String newToken = token.substring(0, 1) + token.substring(token.indexOf('x'));

			tk = new Token(INT_LIT, Long.parseLong(newToken.substring(2).toUpperCase(), 16));
		} else if(flexadecimalMatcher.matcher(token).matches()) {
			tk = new Token(INT_LIT, Long.parseLong(token.substring(0, token.lastIndexOf('B')),
						Integer.parseInt(token.substring(token.lastIndexOf('B') + 1))));
		} else if(DoubleMatcher.floatingLiteral.matcher(token).matches()) {
			tk = new Token(FLOAT_LIT, Double.parseDouble(token));
		} else if(DiceBox.isValidExpression(token)) {
			tk = new Token(DICE_LIT, DiceBox.parseExpression(token));

			if(debugMode)
				System.out.println("\tDEBUG: Parsed dice expression"
						+ ", evaluated as: " 
						+ tk.diceValue.value());
		} else {
			Matcher stringLit = stringLitMatcher.matcher(token);

			if(stringLit.matches()) {
				int litNum = Integer.parseInt(stringLit.group());

				stringLits.put(litNum, stringLts.get(token));
				tk = new Token(STRING_LIT, litNum);
			} else {
				// @TODO define what a valid identifier is
				symTable.put(nextSym++, token);

				tk = new Token(VREF, nextSym - 1);
			}

			// @TODO uncomment when we have a defn. for var names
			// System.out.printf("\tERROR: Unrecognized token:"
			// 		+ "%s\n", token);
		}

		return tk;
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
