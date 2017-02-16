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
import java.util.List;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static bjc.dicelang.v2.Token.Type.*;

public class DiceLangEngine {
	// Input rules for processing tokens
	private List<IPair<String, String>> opExpansionList;
	private List<IPair<String, String>> deaffixationList;

	// ID for generation
	private int nextLiteral;
	private int nextSym;

	// Debug indicator
	private boolean debugMode;
	// Should we do shunting?
	private boolean postfixMode;

	// Shunter for token postfixing
	private Shunter shunt;

	// Tables for symbols
	private IMap<Integer, String> symTable;
	private IMap<Integer, String> stringLits;

	// Literal tokens for tokenization
	private IMap<String, Token.Type> litTokens;

	// Lists for preprocessing
	private IList<Define> lineDefns;
	private IList<Define> tokenDefns;

	// Are defns sorted by priority
	private boolean defnsSorted;

	private final int MATH_PREC = 20;
	private final int DICE_PREC = 10;
	private final int EXPR_PREC = 0;

	public DiceLangEngine() {
		lineDefns   = new FunctionalList<>();
		tokenDefns  = new FunctionalList<>();
		defnsSorted = true;

		symTable   = new FunctionalMap<>();
		stringLits = new FunctionalMap<>();
		
		opExpansionList = new LinkedList<>();

		opExpansionList.add(new Pair<>("+", "\\+"));
		opExpansionList.add(new Pair<>("-", "-"));
		opExpansionList.add(new Pair<>("*", "\\*"));
		opExpansionList.add(new Pair<>("//", "//"));
		opExpansionList.add(new Pair<>("/", "/"));
		opExpansionList.add(new Pair<>(":=", ":="));
		opExpansionList.add(new Pair<>("=>", "=>"));

		deaffixationList = new LinkedList<>();

		deaffixationList.add(new Pair<>("(", "\\("));
		deaffixationList.add(new Pair<>(")", "\\)"));
		deaffixationList.add(new Pair<>("[", "\\["));
		deaffixationList.add(new Pair<>("]", "\\]"));

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

		shunt = new Shunter();

		nextLiteral = 1;

		debugMode   = true;
		postfixMode = false;
	}

	public void sortDefns() {


		defnsSorted = true;
	}

	public void addLineDefine(Define dfn) {
		lineDefns.add(dfn);

		defnsSorted = false;
	}

	public void addTokenDefine(Define dfn) {
		tokenDefns.add(dfn);

		defnsSorted = false;
	}

	public boolean toggleDebug() {
		debugMode = !debugMode;

		return debugMode;
	}

	public boolean togglePostfix() {
		postfixMode = !postfixMode;

		return postfixMode;
	}

	/*
	 * Matches quote-delimited strings
	 * 		(like "text" or "text\"text")
	 *		Uses the "normal* (special normal*)*" pattern style
	 *		recommended in 'Mastering regular expressions'
	 *		Here, the normal is 'anything but a forward or backslash'
	 *		(in regex, thats '[^\""]') and the special is 'an escaped forward slash'
	 *		(in regex, thats '\\"')
	 *
	 *		Then, we just follow the pattern, escape it for java strings, and
	 *		add the enclosing quotes
	 */
	private Pattern quotePattern = Pattern.compile("\"([^\\\"]*(?:\\\"/(?:[^\\\"])*)*)\"");

	public boolean runCommand(String command) {
		// Sort the defines if they aren't sorted
		if(!defnsSorted) sortDefns();

		IMap<String, String> stringLiterals = new FunctionalMap<>();

		Matcher quoteMatcher = quotePattern.matcher(command);
		StringBuffer destringedCommand = new StringBuffer();

		while(quoteMatcher.find()) {
			String stringLit = quoteMatcher.group(1);

			String litName = "stringLiteral" + nextLiteral++;
			stringLiterals.put(litName, stringLit);

			quoteMatcher.appendReplacement(destringedCommand, " " + litName + " ");
		}

		quoteMatcher.appendTail(destringedCommand);

		// Split the command into tokens
		IList<String> tokens = FunctionalStringTokenizer
			.fromString(destringedCommand.toString())
			.toList();

		if(debugMode) {
			System.out.println("\tCommand after destringing: " + tokens.toString());

			System.out.println("\tString literals in table");

			stringLiterals.forEach((key, val) -> {
				System.out.printf("\t\tName: (%s)\tValue: (%s)\n",
					key, val);
			});
		}

		IList<String> semiExpandedTokens  = deaffixTokens(tokens, deaffixationList);
		IList<String> fullyExpandedTokens = deaffixTokens(semiExpandedTokens, opExpansionList);

		if(debugMode) {
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

		IList<Token> shuntedTokens = lexedTokens;

		if(!postfixMode) {
			shuntedTokens = new FunctionalList<>();
			boolean success       = shunt.shuntTokens(lexedTokens, shuntedTokens);
			if(!success) return false;
		}

		if(debugMode && !postfixMode)
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
				int litNum = Integer.parseInt(stringLit.group(1));

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

	 private IList<String> deaffixTokens(IList<String> tokens, List<IPair<String, String>> deaffixTokens) {
		Deque<String> working = new LinkedList<>();

		for(String tk : tokens.toIterable()) {
			working.add(tk);
		}

		for(IPair<String, String> op : deaffixTokens) {
			Deque<String> newWorking = new LinkedList<>();
			
			String opName  = op.getLeft();
			String opRegex = op.getRight();

			Pattern opRegexPattern  = Pattern.compile(opRegex);
			Pattern opRegexOnly     = Pattern.compile("\\A(?:" + opRegex + ")+\\Z");
			Pattern opRegexStarting = Pattern.compile("\\A" + opRegex);
			Pattern opRegexEnding   = Pattern.compile(opRegex + "\\Z");

			for(String tk : working) {
				// @Incomplete
				if(opRegexOnly.matcher(tk).matches()) {
					// The string contains only the operator
					newWorking.add(tk);
				} else {
					Matcher medianMatcher = opRegexPattern.matcher(tk);
					
					// Read the first match
					boolean found = medianMatcher.find();

					if(!found) {
						newWorking.add(tk);
						continue;
					}

					Matcher startMatcher  = opRegexStarting.matcher(tk);
					Matcher endMatcher    = opRegexEnding.matcher(tk);

					boolean startsWith = startMatcher.find();
					boolean endsWith   = endMatcher.find();

					boolean doSplit = medianMatcher.find();

					medianMatcher.reset();

					if(doSplit || (!startsWith && !endsWith)) {
						String[] pieces = opRegexPattern.split(tk);

						if(startsWith) {
							// Skip the starting operator
							medianMatcher.find();
							newWorking.add(tk.substring(0, startMatcher.end()));
						}

						for(int i = 0; i < pieces.length; i++) {
							String piece = pieces[i];

							// Find the next operator
							boolean didFind = medianMatcher.find();

							if(piece.equals("")) {
								System.out.printf("\tWARNING: Empty token found during operator expansion"
										+ "of token (%s). Weirdness may happen as a result\n", tk);
								continue;
							}

							newWorking.add(piece);

							if(didFind)
								newWorking.add(tk.substring(medianMatcher.start(), medianMatcher.end()));
						}

						if(endsWith)
							newWorking.add(tk.substring(endMatcher.start()));
					} else if(startsWith && endsWith) {
						newWorking.add(tk.substring(0, startMatcher.end()));
						newWorking.add(tk.substring(startMatcher.end(), endMatcher.start()));
						newWorking.add(tk.substring(endMatcher.start()));
					} else if(startsWith) {
						newWorking.add(tk.substring(0, startMatcher.end()));
						newWorking.add(tk.substring(startMatcher.end()));
					} else if(endsWith) {
						newWorking.add(tk.substring(0, endMatcher.start()));
						newWorking.add(tk.substring(endMatcher.end()));
					} else {
						newWorking.add(tk);
					}
				}

			}

			working = newWorking;
		}

		IList<String> returned = new FunctionalList<>();
		for(String ent : working) {
			returned.add(ent);
		}

		return returned;
	}
}
