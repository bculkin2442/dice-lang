package bjc.dicelang.scl;

import bjc.utils.funcdata.IList;

public class WordListSCLToken extends SCLToken {
	/* Used for WORDS & ARRAY */
	public IList<SCLToken> tokenVals;

	protected WordListSCLToken(boolean isArray, IList<SCLToken> tokens) {
		if(isArray) {
			type = Type.ARRAY;
		} else {
			type = Type.WORDS;
		}
		
		tokenVals = tokens;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((tokenVals == null) ? 0 : tokenVals.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		WordListSCLToken other = (WordListSCLToken) obj;
		if (tokenVals == null) {
			if (other.tokenVals != null)
				return false;
		} else if (!tokenVals.equals(other.tokenVals))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "WordsSCLToken [tokenVals=" + tokenVals + "]";
	}
}
