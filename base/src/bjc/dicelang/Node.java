package bjc.dicelang;

import static bjc.dicelang.EvaluatorResult.Type.FAILURE;

import bjc.utils.data.ITree;

/*
 * @TODO 10/09/17 Ben Culkin :NodeReorg
 * 	Same thing, different class. Split into subclasses based off of the type
 * 	values.
 */
/**
 * Represents a node in the AST.
 *
 * @author Ben Culkin
 */
public class Node {
	public static enum Type {
		ROOT, TOKREF, UNARYOP, BINOP, GROUP, OGROUP, RESULT
	}

	public static enum GroupType {
		ARRAY, CODE
	}

	public final Type type;

	// These can have or not have values based of the node type
	public Token            tokenVal;
	public Token.Type       operatorType;
	public GroupType        groupType;
	public EvaluatorResult  resultVal;

	public Node(final Type typ) {
		type = typ;
	}

	public Node(final Type typ, final Token tokenVl) {
		this(typ);

		tokenVal = tokenVl;
	}

	public Node(final Type typ, final Token.Type opType) {
		this(typ);

		operatorType = opType;
	}

	public Node(final Type typ, final GroupType grupType) {
		this(typ);

		groupType = grupType;
	}

	public Node(final Type typ, final EvaluatorResult res) {
		this(typ);

		resultVal = res;
	}

	@Override
	public String toString() {
		switch (type) {
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
	public boolean equals(final Object other) {
		if (!(other instanceof Node)) {
			return false;
		}

		final Node otk = (Node) other;

		if (otk.type != type) {
			return false;
		}

		switch (type) {
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

	static Node FAIL(final EvaluatorResult res) {
		EvaluatorResult eres = new EvaluatorResult(FAILURE, new Node(Type.RESULT, res));
		return new Node(Type.RESULT, eres);
	}

	static Node FAIL(final Node orig) {
		return new Node(Type.RESULT, new EvaluatorResult(FAILURE, orig));
	}

	static Node FAIL(final ITree<Node> orig) {
		return new Node(Type.RESULT, new EvaluatorResult(FAILURE, orig));
	}

	/* @TODO 10/09/17 Ben Culkin :NodeFAIL
	 * 	These methods should be moved to Node.
	 */
	/* Create a failing node. */
	static Node FAIL() {
		return new Node(Type.RESULT, new EvaluatorResult(FAILURE));
	}
}
