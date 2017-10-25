package bjc.dicelang.expr;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains per-instance state for token parsing.
 *
 * @author EVE
 *
 */
public class Tokens {
	/* Contains mappings from variable references to string names. */
	private final Map<Integer, String> symTab;
	/* Reverse index into the symbol table. */
	private final Map<String, Integer> revSymTab;

	/** Read-only view on the symbol table. */
	public final Map<Integer, String> symbolTable;

	/* Next index into the symbol table. */
	private int nextSym;

	/* Mapping from literal tokens to token types. */
	private final Map<String, TokenType> litTokens;

	/** Create a new set of tokens. */
	public Tokens() {
		/* Create tables. */
		symTab = new HashMap<>();
		revSymTab = new HashMap<>();

		/* Init public view. */
		symbolTable = Collections.unmodifiableMap(symTab);

		/* Set sym ID. */
		nextSym = 0;

		/* 
		 * Setup literal mappings.
		 *
		 * @NOTE
		 * 	Should this be a static member?
		 */
		litTokens = new HashMap<>();
		litTokens.put("+", TokenType.ADD);
		litTokens.put("-", TokenType.SUBTRACT);
		litTokens.put("*", TokenType.MULTIPLY);
		litTokens.put("/", TokenType.DIVIDE);
		litTokens.put("(", TokenType.OPAREN);
		litTokens.put(")", TokenType.CPAREN);
	}

	/**
	 * Convert the string representation of a token into a token.
	 *
	 * @param tok
	 *                The string representation of the token.
	 * @param raw
	 *                The original string the token came from.
	 *
	 * @return The token the string represents.
	 */
	public Token lexToken(final String tok, final String raw) {
		if (litTokens.containsKey(tok)) {
			/* Return matching literal token. */
			return new Token(litTokens.get(tok), raw, this);
		}

		/* Its a variable reference. */
		return parseVRef(tok, raw);
	}

	/* Parse a variable reference. */
	private Token parseVRef(final String tok, final String raw) {
		final Token tk = new Token(TokenType.VREF, raw, this);

		if (revSymTab.containsKey(tok)) {
			/* Reuse the entry if it exists. */
			tk.intValue = revSymTab.get(tok);
		} else {
			/* Create a new entry. */
			tk.intValue = nextSym;

			/* Record it. */
			symTab.put(nextSym, tok);
			revSymTab.put(tok, nextSym);

			/* Next ID. */
			nextSym += 1;
		}

		return tk;
	}
}
