package bjc.dicelang.expr;

import java.util.LinkedList;
import java.util.List;

import bjc.utils.parserutils.splitter.SimpleTokenSplitter;

/**
 * Implements the lexer for simple expression operations.
 *
 * @author Ben Culkin
 */
public class Lexer {
	/*
	 * Spliter we use
	 */
	private SimpleTokenSplitter split;

	/**
	 * Create a new expression lexer.
	 */
	public Lexer() {
		split = new SimpleTokenSplitter();

		split.addDelimiter("(", ")");
		split.addDelimiter("+", "-", "*", "/");
	}

	/**
	 * Convert a string from a input command to a series of infix tokens.
	 *
	 * @param inp
	 *                The input command.
	 * @param tks
	 *                The token state
	 *
	 * @return A series of infix tokens representing the command.
	 */
	public Token[] lexString(String inp, Tokens tks) {
		String[] spacedTokens = inp.split("[ \t]");

		List<Token> tokens = new LinkedList<>();

		for(String spacedToken : spacedTokens) {
			String[] rawTokens = split.split(spacedToken);

			for(String tok : rawTokens) {
				tokens.add(tks.lexToken(tok, spacedToken));
			}
		}

		return tokens.toArray(new Token[0]);
	}
}
