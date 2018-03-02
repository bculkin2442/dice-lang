package bjc.dicelang;

import bjc.dicelang.eval.EvaluatorResult;
import bjc.dicelang.eval.FailureEvaluatorResult;
import bjc.dicelang.tokens.Token;
import bjc.utils.data.ITree;

/*
 * @TODO 10/09/17 Ben Culkin :NodeReorg
 * 
 * Same thing, different class. Split into subclasses based off of the type
 * values.
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
	public Token tokenVal;
	public Token.Type operatorType;
	public GroupType groupType;
	public EvaluatorResult resultVal;

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
	public boolean equals(final Object other) {
		if(!(other instanceof Node)) {
			return false;
		}

		final Node otk = (Node) other;

		if(otk.type != type) {
			return false;
		}

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

	public static Node FAIL(final EvaluatorResult res) {
		Node nd = new Node(Type.RESULT, res);

		EvaluatorResult eres = new FailureEvaluatorResult(nd);

		return new Node(Type.RESULT, eres);
	}

	public static Node FAIL(final Node orig) {
		FailureEvaluatorResult res = new FailureEvaluatorResult(orig);

		return new Node(Type.RESULT, res);
	}

	public static Node FAIL(final ITree<Node> orig) {
		FailureEvaluatorResult res = new FailureEvaluatorResult(orig);

		return new Node(Type.RESULT, res);
	}

	/* Create a failing node. */
	public static Node FAIL() {
		FailureEvaluatorResult res = new FailureEvaluatorResult();

		return new Node(Type.RESULT, res);
	}
}
