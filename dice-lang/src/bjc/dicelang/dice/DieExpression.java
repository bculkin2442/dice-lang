package bjc.dicelang.dice;

import java.util.Arrays;

/**
 * Represents either a die or a die list
 * 
 * @author Ben Culkin
 */
public class DieExpression {
	/**
	 * Is this expression a list?
	 */
	public final boolean isList;

	/**
	 * The scalar value in this expression, if there is one.
	 */
	public Die     scalar;
	/**
	 * The list value in this expression, if there is one.
	 */
	public DieList list;

	/**
	 * Create a scalar die expression.
	 *
	 * @param scal The scalar value of this expression.
	 */
	public DieExpression(Die scal) {
		isList = false;
		scalar = scal;
	}

	/**
	 * Create a list die expression.
	 *
	 * @param lst The list value of this expression.
	 */
	public DieExpression(DieList lst) {
		isList = true;
		list   = lst;
	}

	@Override
	public String toString() {
		if(isList) return list.toString();
		else       return scalar.toString();
	}

	/**
	 * Get the value of this expression as a string.
	 */
	public String value() {
		if(isList) return Arrays.toString(list.roll());
		else       return Long.toString(scalar.roll());
	}
}
