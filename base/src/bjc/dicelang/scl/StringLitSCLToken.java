package bjc.dicelang.scl;

public class StringLitSCLToken extends StringSCLToken {

	public StringLitSCLToken(String val) {
		super(false, val);
	}

	@Override
	public String toString() {
		return "StringLitSCLToken [stringVal=" + stringVal + "]";
	}
}
