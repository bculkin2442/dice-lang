package bjc.dicelang;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bjc.dicelang.eval.DiceEvaluatorResult;
import bjc.dicelang.eval.Evaluator;
import bjc.dicelang.eval.EvaluatorResult;
import bjc.dicelang.eval.FailureEvaluatorResult;
import bjc.dicelang.scl.StreamEngine;
import bjc.dicelang.tokens.Token;
import bjc.data.Tree;
import bjc.funcdata.FunctionalList;
import bjc.funcdata.FunctionalMap;
import bjc.funcdata.FunctionalStringTokenizer;
import bjc.funcdata.ListEx;
import bjc.funcdata.MapEx;
import bjc.utils.funcutils.ListUtils;
import bjc.utils.parserutils.TokenUtils;
import bjc.utils.parserutils.splitter.ConfigurableTokenSplitter;

import static bjc.dicelang.Errors.ErrorKey.*;
import static bjc.dicelang.tokens.Token.Type.*;

/**
 * Implements the orchestration necessary for processing DiceLang commands.
 *
 * @author Ben Culkin
 */
public class DiceLangEngine {
	/* Logger. */
	private static final Logger LOG = Logger.getLogger(DiceLangEngine.class.getName());

	/*
	 * The random fields that are package private instead of private-private are for
	 * the benefit of the tweaker, so that it can mess around with them.
	 */

	/* Split tokens around operators with regex */
	ConfigurableTokenSplitter opExpander;

	/* ID for generation. */
	int nextLiteral;

	/**
	 * Debug indicator.
	 */
	public boolean debugMode;
	/* Should we do shunting? */
	private boolean postfixMode;
	/* Should we reverse the token stream? */
	private boolean prefixMode;
	/* Should we do step-by-step evaluation? */
	private boolean stepEval;

	/* Shunter for token shunting. */
	Shunter shunt;
	/* Tokenizer for tokenizing. */
	Tokenizer tokenzer;
	/* Parser for tree construction. */
	Parser parsr;
	/* Evaluator for evaluating. */
	Evaluator eval;

	/* Tables for various things. */
	/**
	 * The symbol table.
	 */
	public final MapEx<Integer, String> symTable;

	/* String literal tables */
	private final MapEx<Integer, String> stringLits;
	private final MapEx<String, String> stringLiterals;

	/* Lists of defns. */
	private final ListEx<Define> lineDefns;
	private final ListEx<Define> tokenDefns;

	/* Are defns currently sorted by priority? */
	private boolean defnsSorted;

	/* Stream engine for processing streams. */
	StreamEngine streamEng;

	/**
	 * Create a new DiceLang engine.
	 */
	public DiceLangEngine() {
		/* Initialize defns. */
		lineDefns = new FunctionalList<>();
		tokenDefns = new FunctionalList<>();
		defnsSorted = true;

		/* Initialize tables. */
		symTable = new FunctionalMap<>();
		stringLits = new FunctionalMap<>();
		stringLiterals = new FunctionalMap<>();

		/* Initialize operator expander. */
		opExpander = new ConfigurableTokenSplitter(true);
		/* Add grouping operators */
		opExpander.addMultiDelimiters("(", ")");
		opExpander.addMultiDelimiters("[", "]");
		opExpander.addMultiDelimiters("{", "}");

		/* Add simple operators */
		opExpander.addSimpleDelimiters(":=");
		opExpander.addSimpleDelimiters("=>");
		opExpander.addSimpleDelimiters("//");
		opExpander.addSimpleDelimiters(".+.");
		opExpander.addSimpleDelimiters(".*.");
		opExpander.addSimpleDelimiters("+");
		opExpander.addSimpleDelimiters("-");
		opExpander.addSimpleDelimiters("*");
		opExpander.addSimpleDelimiters("/");

		opExpander.compile();

		/* Initialize literal IDs */
		nextLiteral = 1;

		/* Initial mode settings. */
		debugMode   = true;
		postfixMode = false;
		prefixMode  = false;
		stepEval    = false;

		/* Create components. */
		shunt = new Shunter();
		parsr = new Parser();

		streamEng = new StreamEngine();
		tokenzer  = new Tokenizer(this);
		eval      = new Evaluator(this);
	}

	/** Sort defns by priority. */
	public void sortDefns() {
		lineDefns.sort(null);
		tokenDefns.sort(null);

		defnsSorted = true;
	}

	/**
	 * Add a defn that's applied to lines.
	 *
	 * @param dfn
	 *            The defn to add.
	 */
	public void addLineDefine(final Define dfn) {
		lineDefns.add(dfn);

		defnsSorted = false;
	}

	/**
	 * Add a defn that's applied to tokens.
	 *
	 * @param dfn
	 *            The defn to add.
	 */
	public void addTokenDefine(final Define dfn) {
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
	 *
	 * These are used for tokens that aren't expanded.
	 */
	private final Pattern nonExpandPattern = Pattern.compile("<<([^\\>]*(?:\\>(?:[^\\>])*)*)>>");

	/**
	 * Run a command to completion.
	 *
	 * @param command
	 *            The command to run
	 *
	 * @return Whether or not the command ran successfully
	 */
	public boolean runCommand(final String command) {
		/* Preprocess the command into tokens */
		/*
		 * @NOTE
		 * 
		 * Instead of strings, this should maybe use a RawToken class or something.
		 */
		final ListEx<String> preprocessedTokens = preprocessCommand(command);

		if (preprocessedTokens == null) {
			return false;
		}

		/* Lex the string tokens into token-tokens */
		final ListEx<Token> lexedTokens = lexTokens(preprocessedTokens);

		if (lexedTokens == null) {
			return false;
		}

		/* Parse the tokens into an AST forest */
		final ListEx<Tree<Node>> astForest = new FunctionalList<>();
		final boolean succ = Parser.parseTokens(lexedTokens, astForest);

		if (!succ) {
			return false;
		}

		/* Evaluate the AST forest */
		evaluateForest(astForest);
		return true;
	}

	/* Lex string tokens into token-tokens */
	private ListEx<Token> lexTokens(final ListEx<String> preprocessedTokens) {
		final ListEx<Token> lexedTokens = new FunctionalList<>();

		for (final String token : preprocessedTokens) {
			String newTok = token;

			/* Apply token defns */
			for (final Define dfn : tokenDefns.toIterable()) {
				/*
				 * @NOTE 
				 *
				 * What happens with a define that produces multiple tokens from one
				 * token?
				 *
				 * 	At the moment, nothing.
				 */
				newTok = dfn.apply(newTok);
			}

			/* Lex the token */
			final Token tk = tokenzer.lexToken(token, stringLiterals);

			if (debugMode) {
				LOG.finer(String.format("lexed token: %s\n", tk));
			}

			if (tk == null) {
				/* Ignore blank tokens */
				continue;
			} else if (tk == Token.NIL_TOKEN) {
				/* Fail on bad tokens */
				return null;
			} else {
				lexedTokens.add(tk);
			}
		}

		if (debugMode) {
			String msg = String.format("\tCommand after tokenization: %s\n", lexedTokens.toString());
			LOG.fine(msg);
			System.out.print(msg);
		}

		/* Preshunt preshunt-marked groups of tokens */
		ListEx<Token> shuntedTokens = lexedTokens;
		final ListEx<Token> preparedTokens = new FunctionalList<>();

		boolean succ = removePreshuntTokens(lexedTokens, preparedTokens);

		if (!succ) {
			return null;
		}

		if (debugMode && !postfixMode) {
			String msg = String.format("\tCommand after pre-shunter removal: %s\n", preparedTokens.toString());
			LOG.fine(msg);
			System.out.print(msg);
		}

		/* Only shunt if we're not in a special mode. */
		if (!postfixMode && !prefixMode) {
			/* Shunt the tokens */
			shuntedTokens = new FunctionalList<>();
			succ = shunt.shuntTokens(preparedTokens, shuntedTokens);

			if (!succ) {
				return null;
			}
		} else if (prefixMode) {
			/* Reverse directional tokens */
			/*
			 * @NOTE Merge these two operations into one iteration over the list?
			 */
			preparedTokens.reverse();
			shuntedTokens = preparedTokens.map(this::reverseToken);
		}

		if (debugMode && !postfixMode) {
			String msg = String.format("\tCommand after shunting: %s\n", shuntedTokens.toString());
			LOG.fine(msg);
			System.out.print(msg);
		}

		/* Expand token groups */
		final ListEx<Token> readyTokens = shuntedTokens.flatMap(tk -> {
			if (tk.type == Token.Type.TOKGROUP || tk.type == Token.Type.TAGOP || tk.type == Token.Type.TAGOPR) {
				String msg = String.format("Expanding token group to: %s\n", tk.tokenValues.toString());
				LOG.finer(msg);

				if(debugMode)
					System.out.print(msg);

				return tk.tokenValues;
			} else {
				return new FunctionalList<>(tk);
			}
		});

		if (debugMode && !postfixMode) {
			String msg = String.format("\tCommand after re-preshunting: %s\n", readyTokens.toString());
			LOG.fine(msg);
			System.out.print(msg);
		}

		return readyTokens;
	}

	/*
	 * Reverse orientation-sensitive tokens.
	 *
	 * These are things like (, {, and [
	 */
	private Token reverseToken(final Token tk) {
		switch (tk.type) {
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

	/* Preprocess a command into a list of string tokens. */
	private ListEx<String> preprocessCommand(final String command) {
		/* Sort the defines if they aren't sorted */
		if (!defnsSorted) {
			sortDefns();
		}

		/* Run the tokens through the stream engine */
		final ListEx<String> streamToks = new FunctionalList<>();
		final boolean succ             = streamEng.doStreams(command.split(" "), streamToks);

		if (!succ) {
			return null;
		}

		String newComm = ListUtils.collapseTokens(streamToks, " ");

		if (debugMode) {
			String msg = String.format("\tCommand after stream commands: %s\n", newComm);

			LOG.fine(msg);

			System.out.print(msg);
		}

		/* Apply line defns */
		for (final Define dfn : lineDefns.toIterable()) {
			newComm = dfn.apply(newComm);
		}

		if (debugMode) {
			String msg = String.format("\tCommand after line defines: %s\n", newComm);

			LOG.fine(msg);

			System.out.print(msg);
		}

		/* Remove string literals. */
		final List<String> destringedParts = TokenUtils.removeDQuotedStrings(newComm);
		final StringBuffer destringedCommand = new StringBuffer();

		for (final String part : destringedParts) {
			/* Handle string literals */
			if (part.startsWith("\"") && part.endsWith("\"")) {
				/* Get the actual string. */
				final String litName = "stringLiteral" + nextLiteral;
				final String litVal = part.substring(1, part.length() - 1);

				/*
				 * Insert the string with its escape sequences interpreted.
				 */
				final String descVal = TokenUtils.descapeString(litVal);
				stringLiterals.put(litName, descVal);

				if (debugMode) {
					String msg = String.format("Replaced string literal '%s' with literal no. %d", descVal, nextLiteral);

					System.out.printf("\t\tDEBUG(1): %s\n", msg);

					LOG.finer(msg);
				}

				nextLiteral += 1;

				/* Place a ref. to the string in the command */
				destringedCommand.append(" " + litName + " ");
			} else {
				destringedCommand.append(part);
			}
		}

		if (debugMode) {
			String msg = String.format("\tCommand after destringing: %s\n", destringedCommand);

			LOG.fine(msg);

			System.out.print(msg);

			/* Print the string table if it exists. */
			if (stringLiterals.size() > 0) {
				System.out.println("\tString literals in table");

				stringLiterals.forEach((key, val) -> {
					System.out.printf("\t\tName: (%s)\tValue: (%s)\n", key, val);
				});
			}
		}

		/* Split the command into tokens */
		final String strang  = destringedCommand.toString();
		ListEx<String> tokens = FunctionalStringTokenizer.fromString(strang).toList();

		/* Temporarily remove non-expanding tokens */
		final MapEx<String, String> nonExpandedTokens = new FunctionalMap<>();
		tokens = tokens.map(tk -> {
			final Matcher nonExpandMatcher = nonExpandPattern.matcher(tk);

			if (nonExpandMatcher.matches()) {
				final String tkName = "nonExpandToken" + nextLiteral++;
				nonExpandedTokens.put(tkName, nonExpandMatcher.group(1));
				String msg = String.format("Pulled non-expander '%s' to '%s'", nonExpandMatcher.group(1), tkName);

				if(debugMode)
					System.out.printf("\t\tDEBUG(1): %s\n", msg);

				LOG.finer(msg);

				return tkName;
			}

			return tk;
		});

		if (debugMode) {
			String msg = String.format("\tCommand after removal of non-expanders: %s\n", tokens.toString());

			LOG.fine(msg);

			System.out.print(msg);
		}

		/* Expand tokens */
		ListEx<String> fullyExpandedTokens = tokens.flatMap(opExpander::split);

		if (debugMode) {
			String msg = String.format("\tCommand after token expansion: %s\n", fullyExpandedTokens.toString());

			LOG.fine(msg);

			System.out.print(msg);
		}

		/* Reinsert non-expanded tokens */
		fullyExpandedTokens = fullyExpandedTokens.map(tk -> {
			if (tk.startsWith("nonExpandToken"))
				return nonExpandedTokens.get(tk).get();

			return tk;
		});

		if (debugMode) {
			String msg = String.format("\tCommand after non-expander reinsertion: %s\n",
					fullyExpandedTokens.toString());

			LOG.fine(msg);

			System.out.print(msg);
		}

		return fullyExpandedTokens;
	}

	/* Evaluate a forest of AST nodes. */
	private void evaluateForest(final ListEx<Tree<Node>> astForest) {
		int treeNo = 1;

		for (final Tree<Node> ast : astForest) {
			if (debugMode) {
				System.out.printf("\t\tTree %d in forest:\n%s\n", treeNo, ast.toString());
			}

			if (debugMode && stepEval) {
				/*
				 * @NOTE This is broken until stepwise top-down tree transforms are fixed.
				 */
				int step = 1;

				/* Evaluate it step by step */
				for (final Iterator<Tree<Node>> itr = eval.stepDebug(ast); itr.hasNext();) {
					final Tree<Node> nodeStep = itr.next();

					System.out.printf("\t\tStep %d: Node is %s", step, nodeStep);

					/* Don't evaluate null steps */
					if (nodeStep == null) {
						System.out.println();

						step += 1;
						continue;
					}

					/* Print out details for results */
					if (nodeStep.getHead().type == Node.Type.RESULT) {
						final EvaluatorResult res = nodeStep.getHead().resultVal;

						System.out.printf(" (result is %s", res);

						if (res.type == EvaluatorResult.Type.DICE) {
							String value = ((DiceEvaluatorResult) res).diceVal.value();

							System.out.printf(" (sample roll %s)", value);
						}

						if (res.type == EvaluatorResult.Type.FAILURE) {
							Tree<Node> otree = ((FailureEvaluatorResult) res).origVal;

							System.out.printf(" (original tree is %s)", otree);
						}

						System.out.printf(")");
					}

					/* Advance a step */
					System.out.println();
					step += 1;
				}
			} else {
				/* Evaluate it normally */
				final EvaluatorResult res = eval.evaluate(ast);

				if (debugMode) {
					System.out.printf("\t\tEvaluates to %s", res);

					if(res == null) {
						// Don't need to do anything in this case
					} else if (res.type == EvaluatorResult.Type.DICE) {
						String value = ((DiceEvaluatorResult) res).diceVal.value();

						System.out.println("\t\t (sample roll " + value + ")");
					}
				}
			}

			System.out.println();

			treeNo += 1;
		}
	}

	/* Preshunt preshunt-marked groups of tokens. */
	private boolean removePreshuntTokens(final ListEx<Token> lexedTokens, final ListEx<Token> preparedTokens) {
		/* Current nesting level of tokens. */
		int curBraceCount = 0;

		/* Data storage. */
		final Deque<ListEx<Token>> bracedTokens = new LinkedList<>();
		ListEx<Token> curBracedTokens = new FunctionalList<>();

		for (final Token tk : lexedTokens) {
			if (tk.type == Token.Type.OBRACE && tk.intValue == 2) {
				/* Open a preshunt group. */
				curBraceCount += 1;

				if (curBraceCount != 1) {
					/*
					 * Push the old group onto the group stack.
					 */
					bracedTokens.push(curBracedTokens);
				}

				curBracedTokens = new FunctionalList<>();
			} else if (tk.type == Token.Type.CBRACE && tk.intValue == 2) {
				/* Close a preshunt group. */
				if (curBraceCount == 0) {
					/*
					 * Error if there couldn't have been an opening.
					 */
					Errors.inst.printError(EK_ENG_NOOPENING);
					return false;
				}

				curBraceCount -= 1;

				final ListEx<Token> preshuntTokens = new FunctionalList<>();

				/* Shunt preshunt group. */
				final boolean success = shunt.shuntTokens(curBracedTokens, preshuntTokens);

				if (debugMode) {
					System.out.println("\t\tPreshunted " + curBracedTokens + " into " + preshuntTokens);
				}

				if (!success) {
					return false;
				}

				if (curBraceCount >= 1) {
					/*
					 * Add the preshunt group to the previous group.
					 */
					curBracedTokens = bracedTokens.pop();

					curBracedTokens.add(new Token(Token.Type.TOKGROUP, preshuntTokens));
				} else {
					/*
					 * Add the preshunt group to the token stream.
					 */
					preparedTokens.add(new Token(Token.Type.TOKGROUP, preshuntTokens));
				}
			} else {
				/*
				 * Add the token to the active preshunt group, if there is one..
				 */
				if (curBraceCount >= 1) {
					curBracedTokens.add(tk);
				} else {
					preparedTokens.add(tk);
				}
			}
		}

		if (curBraceCount > 0) {
			/* There was an unclosed group. */
			Errors.inst.printError(EK_ENG_NOCLOSING);
			return false;
		}

		return true;
	}

	/**
	 * Get a string literal from the string literal table.
	 * 
	 * @param key
	 *            The key for the literal.
	 * @return The literal value.
	 * 
	 */
	public String getStringLiteral(final int key) {
		return stringLits.get(key).get();
	}

	/* Add a string literal to the string literal table. */
	/*
	 * @NOTE
	 * 
	 * The string literal table should be abstracted into some kind of auto-numbered
	 * map thing.
	 */
	void addStringLiteral(final int key, final String val) {
		stringLits.put(key, val);
	}
}
