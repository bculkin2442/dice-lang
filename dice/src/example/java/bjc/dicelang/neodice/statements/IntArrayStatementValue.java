package bjc.dicelang.neodice.statements;

import static bjc.dicelang.neodice.statements.StatementValue.Type.*;

import java.util.*;

public class IntArrayStatementValue extends StatementValue {
	public final int[] values;
	
	public IntArrayStatementValue(int... values) {
		super(INT_ARRAY);
		
		this.values = values;
	}
	
	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		
		buffer.append('(');
		for (int index = 0; index < values.length; index++) {
			int value = values[index];
		
			buffer.append(value);
			if (index < values.length - 1) buffer.append(", ");
		}
		buffer.append(')');
		
		return buffer.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(values);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)                  return true;
		if (!super.equals(obj))           return false;
		if (getClass() != obj.getClass()) return false;

		IntArrayStatementValue other = (IntArrayStatementValue) obj;
		
		return Arrays.equals(values, other.values);
	}
}