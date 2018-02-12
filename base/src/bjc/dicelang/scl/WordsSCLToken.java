package bjc.dicelang.scl;

import bjc.utils.funcdata.IList;

public class WordsSCLToken extends SCLToken {
	/* Used for WORDS & ARRAY */
	public IList<SCLToken> tokenVals;

	public WordsSCLToken(boolean isArray, IList<SCLToken> tokens) {
		if(isArray) {
			type = Type.ARRAY;
		} else {
			type = Type.WORDS;
		}
		
		tokenVals = tokens;
	}
}
