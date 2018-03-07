package bjc.dicelang.scl.tokens;

import bjc.utils.funcdata.IList;

/**
 * A token representing an executable bunch of words.
 * 
 * @author student
 *
 */
public class WordsSCLToken extends WordListSCLToken {

	/**
	 * Create a new executable words token.
	 * 
	 * @param tokens
	 *            The tokens to use.
	 */
	public WordsSCLToken(IList<SCLToken> tokens) {
		super(false, tokens);
	}

	@Override
	public String toString() {
		return "WordsSCLToken [tokenVals=" + tokenVals + "]";
	}
}
