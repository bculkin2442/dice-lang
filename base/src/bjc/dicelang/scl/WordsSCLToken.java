package bjc.dicelang.scl;

import bjc.utils.funcdata.IList;

public class WordsSCLToken extends WordListSCLToken {

	public WordsSCLToken(IList<SCLToken> tokens) {
		super(false, tokens);
	}

	@Override
	public String toString() {
		return "WordsSCLToken [tokenVals=" + tokenVals + "]";
	}
}
