package bjc.dicelang.dice;

import java.util.Arrays;

public class ListDiceExpression implements DiceExpression {

	/** The list value in this expression, if there is one. */
	public DieList list;

	/**
	 * Create a list die expression.
	 *
	 * @param lst
	 *            The list value of this expression.
	 */
	public ListDiceExpression(final DieList lst) {
		list = lst;
	}

	@Override
	public String value() {
		return Arrays.toString(list.roll());
	}

	@Override
	public boolean isList() {
		return true;
	}

	@Override
	public String toString() {
		return list.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((list == null) ? 0 : list.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ListDiceExpression other = (ListDiceExpression) obj;
		if (list == null) {
			if (other.list != null)
				return false;
		} else if (!list.equals(other.list))
			return false;
		return true;
	}
}
