package bjc.dicelang.scl.tokens;

/**
 * Represent all the types of a token.
 * 
 * @author student
 *
 */
public enum TokenType {
	/* Natural tokens. These come directly from strings */
	/**
	 * Integer literal.
	 */
	ILIT,
	/**
	 * Floating-point literal.
	 */
	FLIT,
	/**
	 * Boolean literal.
	 */
	BLIT,
	/**
	 * Single-quote.
	 */
	SQUOTE,
	/**
	 * Double-quote.
	 */
	DQUOTE,
	/**
	 * Open-bracket.
	 */
	OBRACKET,
	/**
	 * Open-brace.
	 */
	OBRACE,
	/**
	 * Symbol.
	 */
	SYMBOL,
	/**
	 * Word.
	 */
	WORD,

	/* Synthetic tokens. These are produced from special tokens. */
	/**
	 * String literal.
	 */
	SLIT,
	/**
	 * List of words.
	 */
	WORDS,
	/**
	 * List of data.
	 */
	ARRAY,
}