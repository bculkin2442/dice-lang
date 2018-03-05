package bjc.dicelang.scl.tokens;

import bjc.utils.funcdata.IList;

/**
 * Represents an array token.
 * 
 * @author student
 *
 */
public class ArraySCLToken extends WordListSCLToken {

	/**
	 * Create a new array token.
	 * 
	 * @param tokens
	 *            The tokens in the array.
	 */
	public ArraySCLToken(IList<SCLToken> tokens) {
		super(true, tokens);
	}

	@Override
	public String toString() {
		return "ArraySCLToken [tokenVals=" + tokenVals + "]";
	}
}
