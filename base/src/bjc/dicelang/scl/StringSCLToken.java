package bjc.dicelang.scl;

public class StringSCLToken extends SCLToken {
	/* Used for SYMBOL & SLIT */
	public String stringVal;
	
	protected StringSCLToken(boolean isSymbol, String val) {
		if(isSymbol) {
			type = Type.SYMBOL;
		} else {
			type = Type.SLIT;
		}
		
		stringVal = val;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((stringVal == null) ? 0 : stringVal.hashCode());
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
		StringSCLToken other = (StringSCLToken) obj;
		if (stringVal == null) {
			if (other.stringVal != null)
				return false;
		} else if (!stringVal.equals(other.stringVal))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "StringSCLToken [stringVal=" + stringVal + "]";
	}
}
