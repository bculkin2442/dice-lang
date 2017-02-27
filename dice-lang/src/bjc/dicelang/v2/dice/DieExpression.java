package bjc.dicelang.v2.dice;

import java.util.Arrays;

public class DieExpression {
	public final boolean isList;

	public Die     scalar;
	public DieList list;

	public DieExpression(Die scal) {
		isList = false;
		scalar = scal;
	}

	public DieExpression(DieList lst) {
		isList = true;
		list   = lst;
	}

	public String toString() {
		if(isList) return list.toString();
		else       return scalar.toString();
	}

	public String value() {
		if(isList) return Arrays.toString(list.roll());
		else       return Long.toString(scalar.roll());
	}
}