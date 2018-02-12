package bjc.dicelang.expr;

import java.util.LinkedList;
import java.util.List;

import bjc.utils.funcdata.IList;
import bjc.utils.parserutils.splitter.ConfigurableTokenSplitter;

/*
 * @TODO 10/08/18 :IntExpressions
 * 	Add support for integer constants, and maybe floating-point ones as well
 * 	if you feel like. Heck, you could even go for ratio constants and things
 * 	as well.
 */
/**
 * Implements the lexer for simple expression operations.
 *
 * @author Ben Culkin
 */
public class Lexer {
	/* Splitter we use. */
	private final ConfigurableTokenSplitter split;

	/** Create a new expression lexer. */
	public Lexer() {
		split = new ConfigurableTokenSplitter(true);

		split.addSimpleDelimiters("(", ")");
		split.addSimpleDelimiters("+", "-", "*", "/");
	}

	/**
	 * Convert a string from a input command to a series of infix tokens.
	 *
	 * @param inp
	 *            The input command.
	 *
	 * @param tks
	 *            The token state.
	 *
	 * @return A series of infix tokens representing the command.
	 */
	public Token[] lexString(final String inp, final Tokens tks) {
		/* Split tokens on whitespace. */
		final String[] spacedTokens = inp.split("[ \t]");
		/* Tokens to return. */
		final List<Token> tokens = new LinkedList<>();

		/* Process each token. */
		for (final String spacedToken : spacedTokens) {
			/* Split on operators. */
			final IList<String> splitTokens = split.split(spacedToken);
			/* Convert strings to tokens. */
			final IList<Token> rawTokens = splitTokens.map(tok -> tks.lexToken(tok, spacedToken));

			/* Add tokens to results. */
			rawTokens.forEach(tokens::add);
		}

		return tokens.toArray(new Token[0]);
	}
}
