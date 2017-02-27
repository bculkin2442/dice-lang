package bjc.dicelang.v2;

import bjc.utils.data.IPair;
import bjc.utils.data.ITree;
import bjc.utils.data.Pair;
import bjc.utils.funcdata.FunctionalList;
import bjc.utils.funcdata.FunctionalMap;
import bjc.utils.funcdata.FunctionalStringTokenizer;
import bjc.utils.funcdata.IList;
import bjc.utils.funcdata.IMap;
import bjc.utils.funcutils.ListUtils;
import bjc.utils.funcutils.StringUtils;

import static bjc.dicelang.v2.Errors.ErrorKey.*;
import static bjc.dicelang.v2.Token.Type.*;

import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	// Should we reverse the token stream
	private boolean prefixMode;
	// Should we do step-by-step evaluation
	private boolean stepEval;

	// Shunter for token postfixing
	private Shunter shunt;
	// Parser for tree construction
	private Parser parsr;
	// Evaluator for evaluating
	private Evaluator eval;

	// Tables for symbols
	public final IMap<Integer, String> symTable;
	public final IMap<Integer, String> stringLits;

	// Literal tokens for tokenization
	private IMap<String, Token.Type> litTokens;

	// Lists for preprocessing
	private IList<Define> lineDefns;
	private IList<Define> tokenDefns;

	// Are defns sorted by priority
	private boolean defnsSorted;

	// Stream engine for processing streams
	private StreamEngine streamEng;

	public DiceLangEngine() {
		lineDefns   = new FunctionalList<>();
		tokenDefns  = new FunctionalList<>();
		defnsSorted = true;

		symTable   = new FunctionalMap<>();
		stringLits = new FunctionalMap<>();
		
		opExpansionList = new LinkedList<>();

		opExpansionList.add(new Pair<>("+",  "\\+"));
		opExpansionList.add(new Pair<>("-",  "-"));
		opExpansionList.add(new Pair<>("*",  "\\*"));
		opExpansionList.add(new Pair<>("//", "//"));
		opExpansionList.add(new Pair<>("/",  "/"));
		opExpansionList.add(new Pair<>(":=", ":="));
		opExpansionList.add(new Pair<>("=>", "=>"));
		opExpansionList.add(new Pair<>(",",  ","));

		deaffixationList = new LinkedList<>();

		deaffixationList.add(new Pair<>("(", "\\("));
		deaffixationList.add(new Pair<>(")", "\\)"));
		deaffixationList.add(new Pair<>("[", "\\["));
		deaffixationList.add(new Pair<>("]", "\\]"));
		deaffixationList.add(new Pair<>("{", "\\{"));
		deaffixationList.add(new Pair<>("}", "}"));

		litTokens = new FunctionalMap<>();

		litTokens.put("+",   ADD);
		litTokens.put("-",   SUBTRACT);
		litTokens.put("*",   MULTIPLY);
		litTokens.put("/",   DIVIDE);
		litTokens.put("//",  IDIVIDE);
		litTokens.put("dg",  DICEGROUP);
		litTokens.put("dc",  DICECONCAT);
		litTokens.put("dl",  DICELIST);
		litTokens.put("=>",  LET);
		litTokens.put(":=",  BIND);
		litTokens.put(",",   GROUPSEP);
		litTokens.put("crc", COERCE);  

		nextLiteral = 1;

		debugMode   = true;
		postfixMode = false;
		prefixMode  = false;
		stepEval    = false;

		streamEng = new StreamEngine(this);

		shunt = new Shunter();
		parsr = new Parser();
		eval  = new Evaluator(this);
	}

	public void sortDefns() {
		Comparator<Define> defnCmp = (dfn1, dfn2) -> dfn1.priority - dfn2.priority;

		lineDefns.sort(defnCmp);
		tokenDefns.sort(defnCmp);

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

	public boolean togglePrefix() {
		prefixMode = !prefixMode;

		return prefixMode;
	}

	public boolean toggleStepEval() {
		stepEval = !stepEval;

		return stepEval;
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
	private Pattern quotePattern = Pattern.compile("\"([^\\\"]*(?:\\\"(?:[^\\\"])*)*)\"");

	// Similiar to the above, but using angle brackets instead of quotes
	private Pattern nonExpandPattern = Pattern.compile("<<([^\\>]*(?:\\>(?:[^\\>])*)*)>>");

	public boolean runCommand(String command) {
		// Sort the defines if they aren't sorted
		if(!defnsSorted) sortDefns();

		IList<String> streamToks = new FunctionalList<>();
		boolean success = streamEng.doStreams(command.split(" "), streamToks);
		if(!success) return false;

		String newComm = ListUtils.collapseTokens(streamToks, " ");

		if(debugMode)
			System.out.println("\tCommand after stream commands: " + newComm);

		for(Define dfn : lineDefns.toIterable()) {
			newComm = dfn.apply(newComm);
		}

		if(debugMode)
			System.out.println("\tCommand after line defines: " + newComm);

		IMap<String, String> stringLiterals = new FunctionalMap<>();

		Matcher quoteMatcher = quotePattern.matcher(newComm);
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
			.fromString(destringedCommand.toString()).toList();

		if(debugMode) {
			System.out.println("\tCommand after destringing: " + destringedCommand);

			if(stringLiterals.getSize() > 0) {
				System.out.println("\tString literals in table");

				stringLiterals.forEach((key, val) -> {
					System.out.printf("\t\tName: (%s)\tValue: (%s)\n", key, val);
				});
			}
		}

		IMap<String, String> nonExpandedTokens = new FunctionalMap<>();
		
		tokens = tokens.map(tk -> {
			Matcher nonExpandMatcher = nonExpandPattern.matcher(tk);

			if(nonExpandMatcher.matches()) {
				String tkName = "nonExpandToken" + nextLiteral++;
				nonExpandedTokens.put(tkName, nonExpandMatcher.group(1));

				return tkName;
			} else {
				return tk;
			}
		});

		System.out.println("\tCommand after removal of non-expanders: " + tokens.toString());

		IList<String> semiExpandedTokens  = deaffixTokens(tokens, deaffixationList);
		IList<String> fullyExpandedTokens = deaffixTokens(semiExpandedTokens, opExpansionList);

		System.out.println("\tCommand after token expansion: " + fullyExpandedTokens.toString());

		fullyExpandedTokens = fullyExpandedTokens.map(tk -> {
			if(tk.startsWith("nonExpandToken")) {
				return nonExpandedTokens.get(tk);
			} else {
				return tk;
			}
		});

		if(debugMode) 
			System.out.printf("\tCommand after non-expander reinsertion: " 
					+ fullyExpandedTokens.toString() + "\n");
		

		IList<Token> lexedTokens = new FunctionalList<>();

		for(String token : fullyExpandedTokens) {
			String newTok = token;

			for(Define dfn : tokenDefns.toIterable()) {
				newTok = dfn.apply(newTok);
			}

			Token tk = lexToken(token, stringLiterals);

			if(tk == null) continue;
			else if(tk == Token.NIL_TOKEN) return false;
			else lexedTokens.add(tk);
		}

		if(debugMode)
			System.out.printf("\tCommand after tokenization: %s\n", lexedTokens.toString());

		IList<Token> shuntedTokens = lexedTokens;

		IList<Token> preparedTokens         = new FunctionalList<>();
		boolean sc = removePreshuntTokens(lexedTokens, preparedTokens);

		if(!sc) return false;
		
		if(debugMode && !postfixMode)
			System.out.printf("\tCommand after pre-shunter removal: %s\n", preparedTokens.toString());

		if(!postfixMode && !prefixMode) {
			shuntedTokens = new FunctionalList<>();
			success       = shunt.shuntTokens(preparedTokens, shuntedTokens);
			if(!success) return false;
		} else if(prefixMode) {
			preparedTokens.reverse();
			shuntedTokens = preparedTokens.map(tk -> {
				switch(tk.type) {
					case OBRACE:
						return new Token(CBRACE, tk.intValue);
					case OPAREN:
						return new Token(CPAREN, tk.intValue);
					case OBRACKET:
						return new Token(CBRACKET, tk.intValue);
					case CBRACE:
						return new Token(OBRACE, tk.intValue);
					case CPAREN:
						return new Token(OPAREN, tk.intValue);
					case CBRACKET:
						return new Token(OBRACKET, tk.intValue);
					default:
						return tk;
				}
			});
		}

		if(debugMode && !postfixMode)
			System.out.printf("\tCommand after shunting: %s\n", shuntedTokens.toString());

		IList<Token> readyTokens = shuntedTokens.flatMap(tk -> {
			if(tk.type == Token.Type.TOKGROUP) {
				return tk.tokenValues;
			} else if(tk.type == Token.Type.TAGOP || tk.type == Token.Type.TAGOPR) {
				return tk.tokenValues;
			} else {
				return new FunctionalList<>(tk);
			}
		});

		if(debugMode && !postfixMode)
			System.out.printf("\tCommand after re-preshunting: %s\n", readyTokens.toString());

		IList<ITree<Node>> astForest = new FunctionalList<>();
		success                      = parsr.parseTokens(readyTokens, astForest);

		if(!success) return false;

		if(debugMode) {
			evaluateForest(astForest);
		}

		return true;
	}

	private void evaluateForest(IList<ITree<Node>> astForest) {
		System.out.println("\tParsed forest of asts");
		int treeNo = 1;

		for(ITree<Node> ast : astForest) {
			System.out.println("\t\tTree " + treeNo + " in forest:\n" + ast);

			if(stepEval) {
				int step = 1;

				for(Iterator<ITree<Node>> itr = eval.stepDebug(ast); itr.hasNext();){
					ITree<Node> nodeStep = itr.next();

					System.out.printf("\t\tStep %d: Node is %s", step, nodeStep);

					if(nodeStep == null) {
						System.out.println();

						step += 1;
						continue;
					}

					if(nodeStep.getHead().type == Node.Type.RESULT) {
						EvaluatorResult res = nodeStep.getHead().resultVal;

						System.out.printf(" (result is %s", res);

						if(res.type == EvaluatorResult.Type.DICE) {
							System.out.printf(" (sample roll %s)", res.diceVal.value());
						}

						if(res.origVal != null) {
							System.out.printf(" (original tree is %s)", res.origVal);
						}

						System.out.printf(")");
					}


					System.out.println();
					step += 1;
				}
			} else {
				EvaluatorResult res = eval.evaluate(ast);
				System.out.printf("\t\tEvaluates to %s", res);

				if(res.type == EvaluatorResult.Type.DICE) {
					System.out.println("\t\t (sample roll " + res.diceVal.value() + ")");
				}
			}

			System.out.println();

			treeNo += 1;
		}
	}

	private boolean removePreshuntTokens(IList<Token> lexedTokens, IList<Token> preparedTokens) {
		boolean success;
		int curBraceCount                   = 0;
		Deque<IList<Token>> bracedTokens    = new LinkedList<>();
		IList<Token> curBracedTokens        = null;

		for(Token tk : lexedTokens) {
			if(tk.type == Token.Type.OBRACE && tk.intValue == 2) {
				curBraceCount += 1;

				if(curBraceCount != 1) {
					bracedTokens.push(curBracedTokens);
				}

				curBracedTokens = new FunctionalList<>();
			} else if(tk.type == Token.Type.CBRACE && tk.intValue == 2) {
				if(curBraceCount == 0) {
					Errors.inst.printError(EK_ENG_NOOPENING);
					return false;
				}

				curBraceCount -= 1;

				IList<Token> preshuntTokens = new FunctionalList<>();

				success = shunt.shuntTokens(curBracedTokens, preshuntTokens);

				if(debugMode)
					System.out.println("\t\tPreshunted " + curBracedTokens + " into " + preshuntTokens);

				if(!success) return false;

				if(curBraceCount >= 1) {
					curBracedTokens = bracedTokens.pop();

					curBracedTokens.add(new Token(Token.Type.TOKGROUP, preshuntTokens));
				} else {
					preparedTokens.add(new Token(Token.Type.TOKGROUP, preshuntTokens));
				}
			} else {
				if(curBraceCount >= 1) {
					curBracedTokens.add(tk);
				} else {
					preparedTokens.add(tk);
				}
			}
		}

		if(curBraceCount > 0) {
			Errors.inst.printError(EK_ENG_NOCLOSING);
			return false;
		}
		
		return true;
	}

	private Token lexToken(String token, IMap<String, String> stringLts) {
		if(token.equals("")) return null;

		Token tk = Token.NIL_TOKEN;

		if(litTokens.containsKey(token)) {
			tk = new Token(litTokens.get(token));
		} else {
			switch(token.charAt(0)) {
				case '(':
				case ')':
				case '[':
				case ']':
				case '{':
				case '}':
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
			switch(token.charAt(0)) {
				case '(':
					tk = new Token(OPAREN, token.length());
					break;
				case ')':
					tk = new Token(CPAREN, token.length());
					break;
				case '[':
					tk = new Token(OBRACKET, token.length());
					break;
				case ']':
					tk = new Token(CBRACKET, token.length());
					break;
				case '{':
					tk = new Token(OBRACE, token.length());
					break;
				case '}':
					tk = new Token(CBRACE, token.length());
					break;
				default:
					Errors.inst.printError(EK_TOK_UNGROUP, token);
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
			int parseBase = Integer.parseInt(token.substring(token.lastIndexOf('B') + 1));

			if(parseBase < Character.MIN_RADIX || parseBase > Character.MAX_RADIX) {
				Errors.inst.printError(EK_TOK_INVBASE, Integer.toString(parseBase));
				return Token.NIL_TOKEN;
			}

			String flexNum = token.substring(0, token.lastIndexOf('B'));

			try {
				tk = new Token(INT_LIT, Long.parseLong(flexNum, parseBase));
			} catch (NumberFormatException nfex) {
				Errors.inst.printError(EK_TOK_INVFLEX, flexNum, Integer.toString(parseBase));
				return Token.NIL_TOKEN;
			}
		} else if(DoubleMatcher.floatingLiteral.matcher(token).matches()) {
			tk = new Token(FLOAT_LIT, Double.parseDouble(token));
		} else if(DiceBox.isValidExpression(token)) {
			tk = new Token(DICE_LIT, DiceBox.parseExpression(token));
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

		for(String tk : tokens) {
			working.add(tk);
		}

		for(IPair<String, String> op : deaffixTokens) {
			Deque<String> newWorking = new LinkedList<>();
			
			String opRegex = op.getRight();

			Pattern opRegexPattern  = Pattern.compile(opRegex);
			Pattern opRegexOnly     = Pattern.compile("\\A(?:" + opRegex + ")+\\Z");
			Pattern opRegexStarting = Pattern.compile("\\A" + opRegex);
			Pattern opRegexEnding   = Pattern.compile(opRegex + "\\Z");

			for(String tk : working) {
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
						newWorking.add(tk.substring(endMatcher.start()));
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
