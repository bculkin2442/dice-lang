package bjc.dicelang;

import bjc.dicelang.dice.Die;
import bjc.dicelang.dice.DieExpression;
import bjc.dicelang.dice.DieList;
import bjc.utils.data.ITree;
import bjc.utils.data.Tree;

public class EvaluatorResult {
	public static enum Type {
		FAILURE, INT, FLOAT, DICE, STRING
	}

	public final EvaluatorResult.Type type;

	// These may or may not have values based
	// off of the result type
	public long		intVal;
	public double		floatVal;
	public DieExpression	diceVal;
	public String		stringVal;

	// Original node data
	public ITree<Node> origVal;

	public EvaluatorResult(EvaluatorResult.Type typ) {
		type = typ;
	}

	public EvaluatorResult(EvaluatorResult.Type typ, ITree<Node> orig) {
		this(typ);

		origVal = orig;
	}

	public EvaluatorResult(EvaluatorResult.Type typ, Node orig) {
		this(typ, new Tree<>(orig));
	}

	public EvaluatorResult(EvaluatorResult.Type typ, EvaluatorResult orig) {
		this(typ, new Node(Node.Type.RESULT, orig));
	}

	public EvaluatorResult(EvaluatorResult.Type typ, long iVal) {
		this(typ);

		intVal = iVal;
	}

	public EvaluatorResult(EvaluatorResult.Type typ, double dVal) {
		this(typ);

		floatVal = dVal;
	}

	public EvaluatorResult(EvaluatorResult.Type typ, DieExpression dVal) {
		this(typ);

		diceVal = dVal;
	}

	public EvaluatorResult(EvaluatorResult.Type typ, Die dVal) {
		this(typ);

		diceVal = new DieExpression(dVal);
	}

	public EvaluatorResult(EvaluatorResult.Type typ, DieList dVal) {
		this(typ);

		diceVal = new DieExpression(dVal);
	}

	public EvaluatorResult(EvaluatorResult.Type typ, String strang) {
		this(typ);

		stringVal = strang;
	}

	@Override
	public String toString() {
		switch(type) {
		case INT:
			return type.toString() + "(" + intVal + ")";
		case FLOAT:
			return type.toString() + "(" + floatVal + ")";
		case DICE:
			return type.toString() + "(" + diceVal + ")";
		case STRING:
			return type.toString() + "(" + stringVal + ")";
		case FAILURE:
			return type.toString();
		default:
			return "Unknown result type " + type.toString();
		}
	}
}