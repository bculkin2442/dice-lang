package bjc.dicelang;

import bjc.dicelang.scl.StreamEngine;

import bjc.utils.data.IPair;
import bjc.utils.data.ITree;
import bjc.utils.data.Pair;
import bjc.utils.funcdata.FunctionalList;
import bjc.utils.funcdata.FunctionalMap;
import bjc.utils.funcdata.FunctionalStringTokenizer;
import bjc.utils.funcdata.IList;
import bjc.utils.funcdata.IMap;
import bjc.utils.funcutils.ListUtils;

import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static bjc.dicelang.Errors.ErrorKey.*;
import static bjc.dicelang.Token.Type.*;
/**
 * Implements the orchestration necessary for processing DiceLang commands
 *
 * @author Ben Culkin
 */
public class DiceLangEngine {
	/*
	 * Input rules for processing tokens.
	 */
	private List<IPair<String, String>> opExpansionList;

	/*
	 * ID for generation.
	 */
	private int nextLiteral;

	/*
	 * Debug indicator.
	 */
	private boolean debugMode;
	/*
	 * Should we do shunting?
	 */
	private boolean postfixMode;
	/*
	 * Should we reverse the token stream?
	 */
	private boolean prefixMode;
	/*
	 * Should we do step-by-step evaluation?
	 */
	private boolean stepEval;

	/*
	 * Shunter for token postfixing.
	 */
	private Shunter shunt;
	/*
	 * Tokenizer for tokenizing.
	 */
	private Tokenizer tokenzer;
	/*
	 * Parser for tree construction.
	 */
	private Parser parsr;
	/*
	 * Evaluator for evaluating.
	 */
	private Evaluator eval;

	/*
	 * Tables for symbols.
	 */
	public final IMap<Integer, String> symTable;
	public final IMap<Integer, String> stringLits;


	/*
	 * Lists for preprocessing.
	 */
	private IList<Define> lineDefns;
	private IList<Define> tokenDefns;

	/*
	 * Are defns sorted by priority?
	 */
	private boolean defnsSorted;

	/*
	 * Stream engine for processing streams.
	 */
	private StreamEngine streamEng;

	public DiceLangEngine() {
		/*
		 * Initialize defns.
		 */
		lineDefns   = new FunctionalList<>();
		tokenDefns  = new FunctionalList<>();
		defnsSorted = true;

		/*
		 * Init tables.
		 */
		symTable   = new FunctionalMap<>();
		stringLits = new FunctionalMap<>();
		
		/*
		 * Initialize operator expansion list.
		 */
		opExpansionList = new LinkedList<>();
		opExpansionList.add(new Pair<>("+",  "\\+"));
		opExpansionList.add(new Pair<>("-",  "-"));
		opExpansionList.add(new Pair<>("*",  "\\*"));
		opExpansionList.add(new Pair<>("//", "//"));
		opExpansionList.add(new Pair<>("/",  "/"));
		opExpansionList.add(new Pair<>(":=", ":="));
		opExpansionList.add(new Pair<>("=>", "=>"));
		opExpansionList.add(new Pair<>(",",  ","));
		opExpansionList.add(new Pair<>("(", "\\("));
		opExpansionList.add(new Pair<>(")", "\\)"));
		opExpansionList.add(new Pair<>("[", "\\["));
		opExpansionList.add(new Pair<>("]", "\\]"));
		opExpansionList.add(new Pair<>("{", "\\{"));
		opExpansionList.add(new Pair<>("}", "}"));  

		nextLiteral = 1;

		/*
		 * Initial mode settings.
		 */
		debugMode   = true;
		postfixMode = false;
		prefixMode  = false;
		stepEval    = false;

		/*
		 * Create components.
		 */
		streamEng = new StreamEngine(this);
		shunt     = new Shunter();
		tokenzer  = new Tokenizer(this);
		parsr     = new Parser();
		eval      = new Evaluator(this);
	}

	/**
	 * Sort defns by priority.
	 */
	public void sortDefns() {
		Comparator<Define> defnCmp = (dfn1, dfn2) -> dfn1.priority - dfn2.priority;

		lineDefns.sort(defnCmp);
		tokenDefns.sort(defnCmp);

		defnsSorted = true;
	}

	/**
	 * Add a defn that's applied to lines.
	 *
	 * @param dfn The defn to add.
	 */
	public void addLineDefine(Define dfn) {
		lineDefns.add(dfn);

		defnsSorted = false;
	}

	/**
	 * Add a defn that's applied to tokens.
	 *
	 * @param dfn The defn to add.
	 */
	public void addTokenDefine(Define dfn) {
		tokenDefns.add(dfn);

		defnsSorted = false;
	}

	/**
	 * Toggle debug mode.
	 *
	 * @return The current state of debug mode.
	 */
	public boolean toggleDebug() {
		debugMode = !debugMode;

		return debugMode;
	}

	/**
	 * Toggle postfix mode.
	 *
	 * @return The current state of postfix mode.
	 */
	public boolean togglePostfix() {
		postfixMode = !postfixMode;

		return postfixMode;
	}

	/**
	 * Toggle prefix mode.
	 *
	 * @return The current state of prefix mode
	 */
	public boolean togglePrefix() {
		prefixMode = !prefixMode;

		return prefixMode;
	}

	/**
	 * Toggle step-eval mode
	 *
	 * @return The current state of step-eval mode
	 */
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

	/*
	 * Similiar to the above, but using angle brackets instead of quotes
	 */
	private Pattern nonExpandPattern = Pattern.compile("<<([^\\>]*(?:\\>(?:[^\\>])*)*)>>");

	/**
	 * Run a command to completion.
	 *
	 * @param command The command to run
	 * 
	 * @return Whether or not the command ran succesfully
	 */
	public boolean runCommand(String command) {
		/*
		 * Sort the defines if they aren't sorted
		 */
		if(!defnsSorted) sortDefns();

		/*
		 * Run the tokens through the stream engine
		 */
		IList<String> streamToks = new FunctionalList<>();
		boolean succ = streamEng.doStreams(command.split(" "), streamToks);
		if(!succ) return false;

		/*
		 * Apply line defns
		 */
		String newComm = ListUtils.collapseTokens(streamToks, " ");
		if(debugMode)
			System.out.println("\tCommand after stream commands: " + newComm);
		for(Define dfn : lineDefns.toIterable()) {
			newComm = dfn.apply(newComm);
		}
		if(debugMode)
			System.out.println("\tCommand after line defines: " + newComm);

		/*
		 * Destring command
		 */
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
		if(debugMode) {
			System.out.println("\tCommand after destringing: " + destringedCommand);

			if(stringLiterals.getSize() > 0) {
				System.out.println("\tString literals in table");

				stringLiterals.forEach((key, val) -> {
					System.out.printf("\t\tName: (%s)\tValue: (%s)\n", key, val);
				});
			}
		}

		/*
		 * Split the command into tokens
		 */
		IList<String> tokens = FunctionalStringTokenizer
			.fromString(destringedCommand.toString()).toList();


		/*
		 * Temporarily remove non-expanding tokens
		 */
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
		if(debugMode)
			System.out.printf("\tCommand after removal of non-expanders: %s\n", tokens.toString());

		/*
		 * Expand tokens
		 */
		IList<String> fullyExpandedTokens = deaffixTokens(tokens, opExpansionList);
		System.out.println("\tCommand after token expansion: " + fullyExpandedTokens.toString());

		/*
		 * Reinsert non-expanded tokens
		 */
		fullyExpandedTokens = fullyExpandedTokens.map(tk -> {
			if(tk.startsWith("nonExpandToken")) {
				return nonExpandedTokens.get(tk);
			} else {
				return tk;
			}
		});
		if(debugMode) 
			System.out.printf("\tCommand after non-expander reinsertion: %s\n", 
					fullyExpandedTokens.toString());
		

		IList<Token> lexedTokens = new FunctionalList<>();
		for(String token : fullyExpandedTokens) {
			String newTok = token;

			/*
			 * Apply token defns
			 */
			for(Define dfn : tokenDefns.toIterable()) {
				newTok = dfn.apply(newTok);
			}

			/*
			 * Lex the token
			 */
			Token tk = tokenzer.lexToken(token, stringLiterals);

			/*
			 * Ignore blank tokens
			 */
			if(tk == null) continue;
			/*
			 * Fail on bad tokens
			 */
			else if(tk == Token.NIL_TOKEN) return false;
			else lexedTokens.add(tk);
		}
		if(debugMode)
			System.out.printf("\tCommand after tokenization: %s\n", lexedTokens.toString());

		/*
		 * Handle preshunted tokens
		 */
		IList<Token> shuntedTokens = lexedTokens;
		IList<Token> preparedTokens         = new FunctionalList<>();
		succ = removePreshuntTokens(lexedTokens, preparedTokens);
		if(!succ) return false;
		if(debugMode && !postfixMode)
			System.out.printf("\tCommand after pre-shunter removal: %s\n", preparedTokens.toString());

		if(!postfixMode && !prefixMode) {
			/*
			 * Shunt the tokens
			 */
			shuntedTokens = new FunctionalList<>();
			succ       = shunt.shuntTokens(preparedTokens, shuntedTokens);
			if(!succ) return false;
		} else if(prefixMode) {
			/*
			 * Reverse directional tokens
			 */
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

		/*
		 * Expand token groups
		 */
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

		/*
		 * Parse the tokens
		 */
		IList<ITree<Node>> astForest = new FunctionalList<>();
		succ                         = parsr.parseTokens(readyTokens, astForest);
		if(!succ) return false;

		/*
		 * Evaluate the tokens
		 */
		evaluateForest(astForest);

		return true;
	}

	private void evaluateForest(IList<ITree<Node>> astForest) {
		if(debugMode)
			System.out.println("\tParsed forest of asts");
		int treeNo = 1;

		for(ITree<Node> ast : astForest) {
			if(debugMode)
				System.out.println("\t\tTree " + treeNo + " in forest:\n" + ast);

			if(debugMode && stepEval) {
				int step = 1;

				/*
				 * Evaluate it step by step
				 */
				for(Iterator<ITree<Node>> itr = eval.stepDebug(ast); itr.hasNext();){
					ITree<Node> nodeStep = itr.next();

					System.out.printf("\t\tStep %d: Node is %s", step, nodeStep);

					/*
					 * Don't evaluate null steps
					 */
					if(nodeStep == null) {
						System.out.println();

						step += 1;
						continue;
					}

					/*
					 * Print out details for results
					 */
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

					/*
					 * Advance a step
					 */
					System.out.println();
					step += 1;
				}
			} else {
				/*
				 * Evaluate it normally
				 */
				EvaluatorResult res = eval.evaluate(ast);

				if(debugMode) {
					System.out.printf("\t\tEvaluates to %s", res);

					if(res.type == EvaluatorResult.Type.DICE) {
						System.out.println("\t\t (sample roll " + res.diceVal.value() + ")");
					}
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
					/*
					 * The string contains only the operator
					 */
					newWorking.add(tk);
				} else {
					Matcher medianMatcher = opRegexPattern.matcher(tk);
					
					/*
					 * Read the first match
					 */
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
							/*
							 * Skip the starting operator
							 */
							medianMatcher.find();
							newWorking.add(tk.substring(0, startMatcher.end()));
						}

						for(int i = 0; i < pieces.length; i++) {
							String piece = pieces[i];

							/*
							 * Find the next operator
							 */
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
