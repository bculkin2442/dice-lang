package bjc.dicelang.expr;

import java.util.LinkedList;
import java.util.List;

import bjc.utils.funcdata.IList;
import bjc.utils.parserutils.splitter.ConfigurableTokenSplitter;

/**
 * Implements the lexer for simple expression operations.
 *
 * @author Ben Culkin
 */
public class Lexer {
	/*
	 * Splitter we use.
	 */
	private final ConfigurableTokenSplitter split;

	/**
	 * Create a new expression lexer.
	 */
	public Lexer() {
		split = new ConfigurableTokenSplitter(true);

		split.addSimpleDelimiters("(", ")");
		split.addSimpleDelimiters("+", "-", "*", "/");
	}

	/**
	 * Convert a string from a input command to a series of infix tokens.
	 *
	 * @param inp
	 *                The input command.
	 *
	 * @param tks
	 *                The token state.
	 *
	 * @return A series of infix tokens representing the command.
	 */
	public Token[] lexString(final String inp, final Tokens tks) {
		final String[] spacedTokens = inp.split("[ \t]");

		final List<Token> tokens = new LinkedList<>();

		for (final String spacedToken : spacedTokens) {
			final IList<String> splitTokens = split.split(spacedToken);
			final IList<Token> rawTokens = splitTokens.map(tok -> tks.lexToken(tok, spacedToken));

			rawTokens.forEach(tokens::add);
		}

		return tokens.toArray(new Token[0]);
	}
}
