package bjc.dicelang;

@SuppressWarnings("javadoc")
public class Node {
	public static enum Type {
		ROOT, TOKREF, UNARYOP, BINOP, GROUP, OGROUP, RESULT
	}

	public static enum GroupType {
		ARRAY, CODE
	}

	public final Type type;

	// These can have or not have values based of the node type
	public Token		tokenVal;
	public Token.Type	operatorType;
	public GroupType	groupType;
	public EvaluatorResult	resultVal;

	public Node(Type typ) {
		type = typ;
	}

	public Node(Type typ, Token tokenVl) {
		this(typ);

		tokenVal = tokenVl;
	}

	public Node(Type typ, Token.Type opType) {
		this(typ);

		operatorType = opType;
	}

	public Node(Type typ, GroupType grupType) {
		this(typ);

		groupType = grupType;
	}

	public Node(Type typ, EvaluatorResult res) {
		this(typ);

		resultVal = res;
	}

	@Override
	public String toString() {
		switch(type) {
		case UNARYOP:
		case BINOP:
			return "(" + type.name() + " : " + operatorType + ")";
		case OGROUP:
		case TOKREF:
			return "(" + type.name() + " : " + tokenVal + ")";
		case GROUP:
			return "(" + type.name() + " : " + groupType + ")";
		case RESULT:
			return "(" + type.name() + " : " + resultVal + ")";
		default:
			return "Unknown node type " + type;
		}
	}

	@Override
	public boolean equals(Object other) {
		if(!(other instanceof Node)) return false;

		Node otk = (Node) other;

		if(otk.type != type) return false;

		switch(type) {
		case OGROUP:
			return tokenVal.equals(otk.tokenVal);
		default:
			return true;
		}
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
