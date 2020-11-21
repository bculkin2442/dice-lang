package bjc.dicelang.neodice.statements;

import static bjc.dicelang.neodice.statements.StatementValue.Type.*;

import java.util.*;

public class ArrayStatementValue<ElementType extends StatementValue> extends StatementValue {
	public final Type          elementType;
	public final ElementType[] values;
	
	@SafeVarargs
	public ArrayStatementValue(Type elementType, ElementType... values) {
		super(ARRAY);
		
		this.elementType = elementType;
		this.values      = values;
	}
	
	@Override
	public String toString() {	
		StringBuilder buffer = new StringBuilder();
		
		buffer.append('(');
		for (int index = 0; index < values.length; index++) {
			ElementType value = values[index];
		
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
		result = prime * result + Objects.hash(elementType);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)                  return true;
		if (!super.equals(obj))           return false;
		if (getClass() != obj.getClass()) return false;
		
		ArrayStatementValue<?> other = (ArrayStatementValue<?>) obj;
	
		return elementType == other.elementType && Arrays.equals(values, other.values);
	}
}