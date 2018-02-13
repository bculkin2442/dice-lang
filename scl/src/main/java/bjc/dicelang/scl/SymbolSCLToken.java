package bjc.dicelang.scl;

public class SymbolSCLToken extends StringSCLToken {

	public SymbolSCLToken(String val) {
		super(true, val);
	}

	@Override
	public String toString() {
		return "SymbolSCLToken [stringVal=" + stringVal + "]";
	}
}
