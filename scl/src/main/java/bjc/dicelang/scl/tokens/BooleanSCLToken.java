package bjc.dicelang.scl.tokens;

public class BooleanSCLToken extends SCLToken {
	/* Used for BLIT */
	public boolean boolVal;

	public BooleanSCLToken(boolean val) {
		super(Type.BLIT);

		boolVal = val;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (boolVal ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(!super.equals(obj)) return false;
		if(getClass() != obj.getClass()) return false;
		BooleanSCLToken other = (BooleanSCLToken) obj;
		if(boolVal != other.boolVal) return false;
		return true;
	}

	@Override
	public String toString() {
		return "BooleanSCLToken [boolVal=" + boolVal + "]";
	}
}
