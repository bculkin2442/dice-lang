package bjc.dicelang.scl.tokens;

import bjc.utils.funcdata.IList;

public class ArraySCLToken extends WordListSCLToken {

	public ArraySCLToken(IList<SCLToken> tokens) {
		super(true, tokens);
	}

	@Override
	public String toString() {
		return "ArraySCLToken [tokenVals=" + tokenVals + "]";
	}
}
