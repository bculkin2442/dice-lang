package bjc.dicelang;

import bjc.dicelang.scl.StreamEngine;
import bjc.utils.data.ITree;
import bjc.utils.funcdata.FunctionalList;
import bjc.utils.funcdata.FunctionalMap;
import bjc.utils.funcdata.FunctionalStringTokenizer;
import bjc.utils.funcdata.IList;
import bjc.utils.funcdata.IMap;
import bjc.utils.funcutils.ListUtils;
import bjc.utils.parserutils.TokenSplitter;
import bjc.utils.parserutils.TokenUtils;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static bjc.dicelang.Errors.ErrorKey.EK_ENG_NOCLOSING;
import static bjc.dicelang.Errors.ErrorKey.EK_ENG_NOOPENING;
import static bjc.dicelang.Token.Type.*;

/**
 * Implements the orchestration necessary for processing DiceLang commands.
 *
 * @author Ben Culkin
 */
public class DiceLangEngine {
	/*
	 * The random fields that are package private instead of private-private
	 * are for the benefit of the tweaker, so that it can mess around with
	 * them.
	 */

	/*
	 * Split tokens around operators with regex
	 */
	TokenSplitter opExpander;

	/*
	 * ID for generation.
	 */
	int nextLiteral;

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
	 * Shunter for token shunting.
	 */
	Shunter shunt;
	/*
	 * Tokenizer for tokenizing.
	 */
	Tokenizer tokenzer;
	/*
	 * Parser for tree construction.
	 */
	Parser parsr;
	/*
	 * Evaluator for evaluating.
	 */
	Evaluator eval;

	/*
	 * Tables for various things.
	 */
	public final IMap<Integer, String> symTable;

	private IMap<Integer, String>	stringLits;
	private IMap<String, String>	stringLiterals;

	/*
	 * Lists of defns.
	 */
	private IList<Define>	lineDefns;
	private IList<Define>	tokenDefns;

	/*
	 * Are defns sorted by priority?
	 */
	private boolean defnsSorted;

	/*
	 * Stream engine for processing streams.
	 */
	StreamEngine streamEng;

	public DiceLangEngine() {
		/*
		 * Initialize defns.
		 */
		lineDefns = new FunctionalList<>();
		tokenDefns = new FunctionalList<>();
		defnsSorted = true;

		/*
		 * Init tables.
		 */
		symTable = new FunctionalMap<>();
		stringLits = new FunctionalMap<>();
		stringLiterals = new FunctionalMap<>();

		/*
		 * Initialize operator expansion list.
		 */
		opExpander = new TokenSplitter();
		opExpander.addMultiDelimiter("\\(");
		opExpander.addMultiDelimiter("\\)");
		opExpander.addMultiDelimiter("\\[");
		opExpander.addMultiDelimiter("\\]");
		opExpander.addMultiDelimiter("\\{");
		opExpander.addMultiDelimiter("\\}");
		opExpander.addDelimiter(":=");
		opExpander.addDelimiter("=>");
		opExpander.addDelimiter("//");
		opExpander.addDelimiter(".+.");
		opExpander.addDelimiter(".*.");
		opExpander.addDelimiter("+");
		opExpander.addDelimiter("-");
		opExpander.addDelimiter("*");
		opExpander.addDelimiter("/");
		opExpander.compile();

		/*
		 * Initialize literal IDs
		 */
		nextLiteral = 1;

		/*
		 * Initial mode settings.
		 */
		debugMode = true;
		postfixMode = false;
		prefixMode = false;
		stepEval = false;

		/*
		 * Create components.
		 */
		streamEng = new StreamEngine(this);
		shunt = new Shunter();
		tokenzer = new Tokenizer(this);
		parsr = new Parser();
		eval = new Evaluator(this);
	}

	/**
	 * Sort defns by priority.
	 */
	public void sortDefns() {
		lineDefns.sort(null);
		tokenDefns.sort(null);

		defnsSorted = true;
	}

	/**
	 * Add a defn that's applied to lines.
	 *
	 * @param dfn
	 *                The defn to add.
	 */
	public void addLineDefine(Define dfn) {
		lineDefns.add(dfn);

		defnsSorted = false;
	}

	/**
	 * Add a defn that's applied to tokens.
	 *
	 * @param dfn
	 *                The defn to add.
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
	 * Matches double-angle bracketed strings.
	 */
	private Pattern nonExpandPattern = Pattern.compile("<<([^\\>]*(?:\\>(?:[^\\>])*)*)>>");

	/**
	 * Run a command to completion.
	 *
	 * @param command
	 *                The command to run
	 *
	 * @return Whether or not the command ran successfully
	 */
	public boolean runCommand(String command) {
		/*
		 * Preprocess the command into tokens
		 */
		IList<String> preprocessedTokens = preprocessCommand(command);

		if(preprocessedTokens == null) return false;

		/*
		 * Lex the string tokens into token-tokens
		 */
		IList<Token> lexedTokens = lexTokens(preprocessedTokens);

		if(lexedTokens == null) return false;

		/*
		 * Parse the tokens into an AST forest
		 */
		IList<ITree<Node>> astForest = new FunctionalList<>();
		boolean succ = parsr.parseTokens(lexedTokens, astForest);

		if(!succ) return false;

		/*
		 * Evaluate the AST forest
		 */
		evaluateForest(astForest);

		return true;
	}

	/*
	 * Lex string tokens into token-tokens
	 */
	private IList<Token> lexTokens(IList<String> preprocessedTokens) {
		IList<Token> lexedTokens = new FunctionalList<>();

		for(String token : preprocessedTokens) {
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

			if(tk == null) {
				/*
				 * Ignore blank tokens
				 */
				continue;
			} else if(tk == Token.NIL_TOKEN)
				/*
				 * Fail on bad tokens
				 */
				return null;
			else {
				lexedTokens.add(tk);
			}
		}

		if(debugMode) {
			System.out.printf("\tCommand after tokenization: %s\n", lexedTokens.toString());
		}

		/*
		 * Preshunt preshunt-marked groups of tokens
		 */
		IList<Token> shuntedTokens = lexedTokens;
		IList<Token> preparedTokens = new FunctionalList<>();

		boolean succ = removePreshuntTokens(lexedTokens, preparedTokens);

		if(!succ) return null;

		if(debugMode && !postfixMode) {
			System.out.printf("\tCommand after pre-shunter removal: %s\n", preparedTokens.toString());
		}

		if(!postfixMode && !prefixMode) {
			/*
			 * Shunt the tokens
			 */
			shuntedTokens = new FunctionalList<>();
			succ = shunt.shuntTokens(preparedTokens, shuntedTokens);

			if(!succ) return null;
		} else if(prefixMode) {
			/*
			 * Reverse directional tokens
			 */
			preparedTokens.reverse();
			shuntedTokens = preparedTokens.map(this::reverseToken);
		}

		if(debugMode && !postfixMode) {
			System.out.printf("\tCommand after shunting: %s\n", shuntedTokens.toString());
		}

		/*
		 * Expand token groups
		 */
		IList<Token> readyTokens = shuntedTokens.flatMap(tk -> {
			if(tk.type == Token.Type.TOKGROUP)
				return tk.tokenValues;
			else if(tk.type == Token.Type.TAGOP || tk.type == Token.Type.TAGOPR)
				return tk.tokenValues;
			else
				return new FunctionalList<>(tk);
		});

		if(debugMode && !postfixMode) {
			System.out.printf("\tCommand after re-preshunting: %s\n", readyTokens.toString());
		}

		return readyTokens;
	}

	/*
	 * Reverse orientation-sensitive tokens.
	 *
	 * These are mostly just things like (, {, and [
	 */
	private Token reverseToken(Token tk) {
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
	}

	/*
	 * Preprocess a command into a list of string tokens.
	 */
	private IList<String> preprocessCommand(String command) {
		/*
		 * Sort the defines if they aren't sorted
		 */
		if(!defnsSorted) {
			sortDefns();
		}

		/*
		 * Run the tokens through the stream engine
		 */
		IList<String> streamToks = new FunctionalList<>();
		boolean succ = streamEng.doStreams(command.split(" "), streamToks);

		if(!succ) return null;

		/*
		 * Apply line defns
		 */
		String newComm = ListUtils.collapseTokens(streamToks, " ");

		if(debugMode) {
			System.out.println("\tCommand after stream commands: " + newComm);
		}

		for(Define dfn : lineDefns.toIterable()) {
			newComm = dfn.apply(newComm);
		}

		if(debugMode) {
			System.out.println("\tCommand after line defines: " + newComm);
		}

		/*
		 * Remove string literals.
		 */
		List<String> destringedParts = TokenUtils.removeDQuotedStrings(newComm);
		StringBuffer destringedCommand = new StringBuffer();

		for(String part : destringedParts) {
			/*
			 * Handle string literals
			 */
			if(part.startsWith("\"") && part.endsWith("\"")) {
				/*
				 * Get the actual string.
				 */
				String litName = "stringLiteral" + nextLiteral;
				String litVal = part.substring(1, part.length() - 1);

				/*
				 * Insert the string with its escape sequences
				 * interpreted.
				 */
				stringLiterals.put(litName, TokenUtils.descapeString(litVal));
				nextLiteral += 1;

				/*
				 * Place a ref. to the string in the command
				 */
				destringedCommand.append(" " + litName + " ");
			} else {
				destringedCommand.append(part);
			}
		}

		if(debugMode) {
			System.out.println("\tCommand after destringing: " + destringedCommand);

			/*
			 * Print the string table if it exists.
			 */
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
		String strang = destringedCommand.toString();
		IList<String> tokens = FunctionalStringTokenizer.fromString(strang).toList();

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
			} else
				return tk;
		});

		if(debugMode) {
			System.out.printf("\tCommand after removal of non-expanders: %s\n", tokens.toString());
		}

		/*
		 * Expand tokens
		 */
		IList<String> fullyExpandedTokens = tokens
				.flatMap((token) -> new FunctionalList<>(opExpander.split(token)));
		System.out.println("\tCommand after token expansion: " + fullyExpandedTokens.toString());

		/*
		 * Reinsert non-expanded tokens
		 */
		fullyExpandedTokens = fullyExpandedTokens.map(tk -> {
			if(tk.startsWith("nonExpandToken"))
				return nonExpandedTokens.get(tk);
			else
				return tk;
		});

		if(debugMode) {
			System.out.printf("\tCommand after non-expander reinsertion: %s\n",
					fullyExpandedTokens.toString());
		}

		return fullyExpandedTokens;
	}

	private void evaluateForest(IList<ITree<Node>> astForest) {
		if(debugMode) {
			System.out.println("\tParsed forest of asts");
		}

		int treeNo = 1;

		for(ITree<Node> ast : astForest) {
			if(debugMode) {
				System.out.printf("\t\tTree %d in forest:\n%s\n", treeNo, ast.toString());
			}

			if(debugMode && stepEval) {
				int step = 1;

				/*
				 * Evaluate it step by step
				 */
				for(Iterator<ITree<Node>> itr = eval.stepDebug(ast); itr.hasNext();) {
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

	/*
	 * Preshunt preshunt-marked groups of tokens.
	 */
	private boolean removePreshuntTokens(IList<Token> lexedTokens, IList<Token> preparedTokens) {
		/*
		 * Current nesting level of tokens.
		 */
		int curBraceCount = 0;

		/*
		 * Data storage.
		 */
		Deque<IList<Token>> bracedTokens = new LinkedList<>();
		IList<Token> curBracedTokens = null;

		for(Token tk : lexedTokens) {
			if(tk.type == Token.Type.OBRACE && tk.intValue == 2) {
				/*
				 * Open a preshunt group.
				 */
				curBraceCount += 1;

				if(curBraceCount != 1) {
					/*
					 * Push the old group onto the group
					 * stack.
					 */
					bracedTokens.push(curBracedTokens);
				}

				curBracedTokens = new FunctionalList<>();
			} else if(tk.type == Token.Type.CBRACE && tk.intValue == 2) {
				/*
				 * Close a preshunt group.
				 */
				if(curBraceCount == 0) {
					/*
					 * Error if there couldn't have been an
					 * opening.
					 */
					Errors.inst.printError(EK_ENG_NOOPENING);
					return false;
				}

				curBraceCount -= 1;

				IList<Token> preshuntTokens = new FunctionalList<>();

				/*
				 * Shunt preshunt group.
				 */
				boolean success = shunt.shuntTokens(curBracedTokens, preshuntTokens);

				if(debugMode) {
					System.out.println("\t\tPreshunted " + curBracedTokens + " into "
							+ preshuntTokens);
				}

				if(!success) return false;

				if(curBraceCount >= 1) {
					/*
					 * Add the preshunt group to the
					 * previous group.
					 */
					curBracedTokens = bracedTokens.pop();

					curBracedTokens.add(new Token(Token.Type.TOKGROUP, preshuntTokens));
				} else {
					/*
					 * Add the preshunt group to the token
					 * stream..
					 */
					preparedTokens.add(new Token(Token.Type.TOKGROUP, preshuntTokens));
				}
			} else {
				/*
				 * Add the token to the active preshunt group,
				 * if there is one..
				 */
				if(curBraceCount >= 1) {
					curBracedTokens.add(tk);
				} else {
					preparedTokens.add(tk);
				}
			}
		}

		if(curBraceCount > 0) {
			/*
			 * There was an unclosed group.
			 */
			Errors.inst.printError(EK_ENG_NOCLOSING);
			return false;
		}

		return true;
	}

	String getStringLiteral(int key) {
		return stringLits.get(key);
	}

	void addStringLiteral(int key, String val) {
		stringLits.put(key, val);
	}
}