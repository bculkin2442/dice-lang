package bjc.dicelang.eval;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.Consumer;

import bjc.dicelang.DiceLangEngine;
import bjc.dicelang.Errors;
import bjc.dicelang.Node;
import bjc.dicelang.dice.CompoundDie;
import bjc.dicelang.dice.Die;
import bjc.dicelang.dice.MathDie;
import bjc.dicelang.dice.ScalarDiceExpression;
import bjc.dicelang.dice.ScalarDie;
import bjc.dicelang.dice.SimpleDie;
import bjc.dicelang.dice.SimpleDieList;
import bjc.dicelang.tokens.DiceToken;
import bjc.dicelang.tokens.FloatToken;
import bjc.dicelang.tokens.Token;
import bjc.data.Tree;
import bjc.data.SingleIterator;
import bjc.data.TopDownTransformIterator;
import bjc.data.TopDownTransformResult;
import bjc.data.SimpleTree;

import static bjc.dicelang.Errors.ErrorKey.*;
import static bjc.dicelang.eval.EvaluatorResult.Type.*;

/*
 * @TODO 10/09/17 Ben Culkin :EvaluatorSplit
 * 
 * Type/sanity checking should be moved into a seperate stage, not part of
 * evaluation.
 */
/**
 * Evaluate DiceLang ASTs
 *
 * @author EVE
 *
 */
public class Evaluator {
	/* The steps of type coercion. */
	private static enum CoerceSteps {
		INTEGER, DOUBLE;
	}

	/* The context during iteration. */
	private static class Context {
		public Consumer<Iterator<Tree<Node>>> thunk;

		public boolean isDebug;

		public Context() {
			/* Empty block. */
		}
	}

	/* The engine we are connected to. */
	private final DiceLangEngine eng;

	/**
	 * Create a new evaluator.
	 *
	 * @param en
	 *            The engine.
	 */
	public Evaluator(final DiceLangEngine en) {
		eng = en;
	}

	/**
	 * Evaluate a AST.
	 *
	 * @param comm
	 *            The AST to evaluate.
	 *
	 * @return The result of the tree.
	 */
	public EvaluatorResult evaluate(final Tree<Node> comm) {
		final Context ctx = new Context();

		ctx.isDebug = false;
		ctx.thunk = itr -> {
			/*
			 * Deliberately finish the iterator, but ignore results. It's only for stepwise
			 * evaluation, but we don't know if stepping the iterator has side effects.
			 */
			while (itr.hasNext()) {
				itr.next();
			}
		};

		/* The result. */
		final Tree<Node> res = comm.topDownTransform(this::pickEvaluationType, node -> this.evaluateNode(node, ctx));

		return res.getHead().resultVal;
	}

	/*
	 * @NOTE
	 * 
	 * This is broken until stepwise top-down transforms are fixed.
	 * 
	 * Make it public once we know it works again.
	 */
	@SuppressWarnings("javadoc")
	public Iterator<Tree<Node>> stepDebug(final Tree<Node> comm) {
		final Context ctx = new Context();

		ctx.isDebug = true;

		return new TopDownTransformIterator<>(this::pickEvaluationType, (node, thnk) -> {
			ctx.thunk = thnk;

			return this.evaluateNode(node, ctx);
		}, comm);
	}

	/* Pick the way to evaluate a node. */
	private TopDownTransformResult pickEvaluationType(final Node nd) {
		switch (nd.type) {
		case UNARYOP:
			switch (nd.operatorType) {
			case COERCE:
				/* Coerce does special things to the tree. */
				return TopDownTransformResult.RTRANSFORM;
			default:
				return TopDownTransformResult.PUSHDOWN;
			}
		case GROUP:
			return TopDownTransformResult.PASSTHROUGH;
		default:
			return TopDownTransformResult.PUSHDOWN;
		}
	}

	/* Evaluate a node. */
	private Tree<Node> evaluateNode(final Tree<Node> ast, final Context ctx) {
		switch (ast.getHead().type) {
		case UNARYOP:
			return evaluateUnaryOp(ast, ctx);
		case BINOP:
			return evaluateBinaryOp(ast, ctx);
		case TOKREF:
			return evaluateTokenRef(ast.getHead().tokenVal, ctx);
		case ROOT:
			return ast.getChild(ast.getChildrenCount() - 1);
		case RESULT:
			return ast;
		default:
			Errors.inst.printError(EK_EVAL_INVNODE, ast.getHead().type.toString());
			return new SimpleTree<>(Node.FAIL(ast));
		}
	}

	/* Evaluate a unary operator. */
	private Tree<Node> evaluateUnaryOp(final Tree<Node> ast, final Context ctx) {
		/* Unary operators only take one operand. */
		if (ast.getChildrenCount() != 1) {
			Errors.inst.printError(EK_EVAL_UNUNARY, Integer.toString(ast.getChildrenCount()));
			return new SimpleTree<>(Node.FAIL(ast));
		}

		switch (ast.getHead().operatorType) {
		case COERCE:
			return doTypeCoercion(ast, ctx);

		case DICESCALAR: {
			final EvaluatorResult opr = ast.getChild(0).getHead().resultVal;

			if (opr.type != INT) {
				Errors.inst.printError(EK_EVAL_INVDCREATE, opr.type.toString());
			}

			IntegerEvaluatorResult irs = (IntegerEvaluatorResult) opr;

			ScalarDie die = new ScalarDie(irs.value);

			final EvaluatorResult sres = new DiceEvaluatorResult(die);

			return new SimpleTree<>(new Node(Node.Type.RESULT, sres));
		}
		case DICEFUDGE: {
			final EvaluatorResult oprn = ast.getChild(0).getHead().resultVal;

			if (oprn.type != INT) {
				Errors.inst.printError(EK_EVAL_INVDCREATE, oprn.type.toString());
			}

			IntegerEvaluatorResult irs = (IntegerEvaluatorResult) oprn;

			ScalarDie die = new ScalarDie(irs.value);

			final EvaluatorResult fres = new DiceEvaluatorResult(die);

			return new SimpleTree<>(new Node(Node.Type.RESULT, fres));
		}
		default: {
			Errors.inst.printError(EK_EVAL_INVUNARY, ast.getHead().operatorType.toString());

			return new SimpleTree<>(Node.FAIL(ast));
		}
		}
	}

	/*
	 * @TODO 10/09/17 Ben Culkin :CoerceRefactor
	 * 
	 * :EvaluatorSplit
	 * 
	 * Coercing should be moved to its own class, or at the very least its own
	 * method. When the evaluator splits, this node type'll be handled exclusively
	 * by the type-checker.
	 *
	 * Coerce also needs to be able to coerce things to dice and ratios (whenever
	 * they get added).
	 */
	private Tree<Node> doTypeCoercion(final Tree<Node> ast, final Context ctx) {
		final Tree<Node> toCoerce = ast.getChild(0);
		final Tree<Node> retVal = new SimpleTree<>(toCoerce.getHead());
		final Deque<Tree<Node>> children = new LinkedList<>();

		/* The current type we are coercing to. */
		CoerceSteps curLevel = CoerceSteps.INTEGER;

		for (int i = 0; i < toCoerce.getChildrenCount(); i++) {
			final Tree<Node> child = toCoerce.getChild(i);
			Tree<Node> nChild = null;

			/* Tell our thunk we processed a node. */
			if (ctx.isDebug) {
				/* Evaluate each step of the child. */
				final Iterator<Tree<Node>> nd = stepDebug(child);

				for (; nd.hasNext(); nChild = nd.next()) {
					ctx.thunk.accept(new SingleIterator<>(child));
				}
			} else {
				/* Evaluate the child. */
				nChild = new SimpleTree<>(new Node(Node.Type.RESULT, evaluate(child)));

				ctx.thunk.accept(new SingleIterator<>(nChild));
			}

			if (nChild == null) {
				Errors.inst.printError(EK_EVAL_INVNODE);
				return new SimpleTree<>(Node.FAIL(ast));
			}

			final Node childNode = nChild.getHead();
			final EvaluatorResult res = childNode.resultVal;

			/* Move up to coercing to a float. */
			if (res.type == FLOAT) {
				curLevel = CoerceSteps.DOUBLE;
			}

			children.add(nChild);
		}

		for (final Tree<Node> child : children) {
			final Node nd = child.getHead();
			final EvaluatorResult res = nd.resultVal;

			switch (res.type) {
			case INT:
				/*
				 * Coerce ints to doubles if we need to.
				 */
				if (curLevel == CoerceSteps.DOUBLE) {
					IntegerEvaluatorResult rs = (IntegerEvaluatorResult) res;

					nd.resultVal = new FloatEvaluatorResult(rs.value);
				}
			default:
				/* Do nothing */
				break;
			}

			retVal.addChild(child);
		}

		return retVal;
	}

	/* Evaluate a binary operator. */
	private static Tree<Node> evaluateBinaryOp(final Tree<Node> ast, final Context ctx) {
		final Token.Type binOp = ast.getHead().operatorType;

		/* Binary operators always have two children. */
		if (ast.getChildrenCount() != 2) {
			Errors.inst.printError(EK_EVAL_INVBIN, Integer.toString(ast.getChildrenCount()), ast.toString());

			return new SimpleTree<>(Node.FAIL(ast));
		}

		final Tree<Node> left = ast.getChild(0);
		final Tree<Node> right = ast.getChild(1);

		final EvaluatorResult leftRes = left.getHead().resultVal;
		final EvaluatorResult rightRes = right.getHead().resultVal;

		switch (binOp) {
		case ADD:
		case SUBTRACT:
		case MULTIPLY:
		case DIVIDE:
		case IDIVIDE:
			return evaluateMathBinary(binOp, leftRes, rightRes, ctx);
		case DICEGROUP:
		case DICECONCAT:
		case DICELIST:
			return evaluateDiceBinary(binOp, leftRes, rightRes, ctx);
		case STRCAT:
		case STRREP:
			return evaluateStringBinary(binOp, leftRes, rightRes, ctx);
		default:
			Errors.inst.printError(EK_EVAL_UNBIN, binOp.toString());
			return new SimpleTree<>(Node.FAIL(ast));
		}
	}

	/* Evaluate a binary operator on strings. */
	private static Tree<Node> evaluateStringBinary(final Token.Type op, final EvaluatorResult left,
			final EvaluatorResult right, final Context ctx) {
		if (left.type != STRING) {
			Errors.inst.printError(EK_EVAL_INVSTRING, left.type.toString());
			return new SimpleTree<>(Node.FAIL(left));
		}

		final String strang = ((StringEvaluatorResult) left).stringVal;

		switch (op) {
		case STRCAT: {
			if (right.type != STRING) {
				Errors.inst.printError(EK_EVAL_UNSTRING, right.type.toString());
				return new SimpleTree<>(Node.FAIL(right));
			}

			final String strung = ((StringEvaluatorResult) right).stringVal;
			final EvaluatorResult cres = new StringEvaluatorResult(strang + strung);

			return new SimpleTree<>(new Node(Node.Type.RESULT, cres));
		}
		case STRREP: {
			if (right.type != INT) {
				Errors.inst.printError(EK_EVAL_INVSTRING, right.type.toString());
				return new SimpleTree<>(Node.FAIL(right));
			}

			String res = strang;
			final long count = ((IntegerEvaluatorResult) right).value;

			for (long i = 1; i < count; i++) {
				res += strang;
			}

			return new SimpleTree<>(new Node(Node.Type.RESULT, new StringEvaluatorResult(res)));
		}
		default:
			Errors.inst.printError(EK_EVAL_UNSTRING, op.toString());
			return new SimpleTree<>(Node.FAIL());
		}
	}

	/* Evaluate dice binary operators. */
	private static Tree<Node> evaluateDiceBinary(final Token.Type op, final EvaluatorResult left,
			final EvaluatorResult right, final Context ctx) {
		EvaluatorResult res = null;

		switch (op) {
		/*
		 * @TODO 10/09/17 Ben Culkin :DiceSimplify
		 * 
		 * Figure out some way to simplify this sort of thing.
		 * 
		 * ADDENDA: Replace the .diceVal.isList() with .isList()
		 */
		case DICEGROUP: {
			if (left.type == DICE && !((DiceEvaluatorResult) left).diceVal.isList()) {
				Die lhs = ((ScalarDiceExpression) ((DiceEvaluatorResult) left).diceVal).scalar;

				if (right.type == DICE && !((DiceEvaluatorResult) right).diceVal.isList()) {
					Die rhs = ((ScalarDiceExpression) ((DiceEvaluatorResult) right).diceVal).scalar;

					Die simple = new SimpleDie(lhs, rhs);

					res = new DiceEvaluatorResult(simple);
				} else if (right.type == INT) {
					res = new DiceEvaluatorResult(new SimpleDie(lhs, ((IntegerEvaluatorResult) right).value));
				} else {
					Errors.inst.printError(EK_EVAL_INVDGROUP, right.type.toString());
					return new SimpleTree<>(Node.FAIL(right));
				}
			} else if (left.type == INT) {
				IntegerEvaluatorResult irs = (IntegerEvaluatorResult) left;

				if (right.type == DICE && !((DiceEvaluatorResult) right).diceVal.isList()) {
					Die rhs = ((ScalarDiceExpression) ((DiceEvaluatorResult) right).diceVal).scalar;

					res = new DiceEvaluatorResult(new SimpleDie(irs.value, rhs));
				} else if (right.type == INT) {
					res = new DiceEvaluatorResult(new SimpleDie(irs.value, ((IntegerEvaluatorResult) right).value));
				} else {
					Errors.inst.printError(EK_EVAL_INVDGROUP, right.type.toString());
					return new SimpleTree<>(Node.FAIL(right));
				}
			} else {
				Errors.inst.printError(EK_EVAL_INVDGROUP, left.type.toString());
				return new SimpleTree<>(Node.FAIL(left));
			}
		}
		case DICECONCAT: {
			if (left.type != DICE || ((DiceEvaluatorResult) left).diceVal.isList()) {
				Errors.inst.printError(EK_EVAL_INVDICE, left.type.toString());
				return new SimpleTree<>(Node.FAIL(left));
			} else if (right.type != DICE || ((DiceEvaluatorResult) right).diceVal.isList()) {
				Errors.inst.printError(EK_EVAL_INVDICE, right.type.toString());
				return new SimpleTree<>(Node.FAIL(right));
			} else {
				Die lhs = ((ScalarDiceExpression) ((DiceEvaluatorResult) left).diceVal).scalar;
				Die rhs = ((ScalarDiceExpression) ((DiceEvaluatorResult) right).diceVal).scalar;

				res = new DiceEvaluatorResult(new CompoundDie(lhs, rhs));
			}

			break;
		}
		case DICELIST: {
			if (left.type != DICE || ((DiceEvaluatorResult) left).diceVal.isList()) {
				Errors.inst.printError(EK_EVAL_INVDICE, left.type.toString());
				return new SimpleTree<>(Node.FAIL(left));
			} else if (right.type != DICE || ((DiceEvaluatorResult) right).diceVal.isList()) {
				Errors.inst.printError(EK_EVAL_INVDICE, right.type.toString());
				return new SimpleTree<>(Node.FAIL(right));
			} else {
				Die lhs = ((ScalarDiceExpression) ((DiceEvaluatorResult) left).diceVal).scalar;
				Die rhs = ((ScalarDiceExpression) ((DiceEvaluatorResult) right).diceVal).scalar;

				res = new DiceEvaluatorResult(new SimpleDieList(lhs, rhs));
			}

			break;
		}
		default:
			Errors.inst.printError(EK_EVAL_UNDICE, op.toString());
			return new SimpleTree<>(Node.FAIL());
		}

		return new SimpleTree<>(new Node(Node.Type.RESULT, res));
	}

	/* Evaluate a binary math operator. */
	private static Tree<Node> evaluateMathBinary(final Token.Type op, final EvaluatorResult left,
			final EvaluatorResult right, final Context ctx) {
		if (left.type == STRING || right.type == STRING) {
			Errors.inst.printError(EK_EVAL_STRINGMATH);
			return new SimpleTree<>(Node.FAIL());
		} else if (left.type == FAILURE || right.type == FAILURE) {
			return new SimpleTree<>(Node.FAIL());
		} else if (left.type == INT && right.type != INT) {
			Errors.inst.printError(EK_EVAL_MISMATH);
			return new SimpleTree<>(Node.FAIL(right));
		} else if (left.type == FLOAT && right.type != FLOAT) {
			Errors.inst.printError(EK_EVAL_MISMATH);
			return new SimpleTree<>(Node.FAIL(right));
		} else if (left.type == DICE && right.type != DICE) {
			Errors.inst.printError(EK_EVAL_MISMATH);
			return new SimpleTree<>(Node.FAIL(right));
		} else if (right.type == INT && left.type != INT) {
			Errors.inst.printError(EK_EVAL_MISMATH);
			return new SimpleTree<>(Node.FAIL(left));
		} else if (right.type == FLOAT && left.type != FLOAT) {
			Errors.inst.printError(EK_EVAL_MISMATH);
			return new SimpleTree<>(Node.FAIL(left));
		} else if (right.type == DICE && left.type != DICE) {
			Errors.inst.printError(EK_EVAL_MISMATH);
			return new SimpleTree<>(Node.FAIL(left));
		}

		EvaluatorResult res = null;

		switch (op) {
		case ADD: {
			if (left.type == INT) {
				long lval = ((IntegerEvaluatorResult) left).value;
				long rval = ((IntegerEvaluatorResult) right).value;

				res = new IntegerEvaluatorResult(lval + rval);
			} else if (left.type == DICE) {
				if (((DiceEvaluatorResult) left).diceVal.isList()) {
					Errors.inst.printError(EK_EVAL_INVDICE, left.toString());
					return new SimpleTree<>(Node.FAIL(left));
				} else if (((DiceEvaluatorResult) right).diceVal.isList()) {
					Errors.inst.printError(EK_EVAL_INVDICE, right.toString());
					return new SimpleTree<>(Node.FAIL(right));
				}

				Die lhs = ((ScalarDiceExpression) ((DiceEvaluatorResult) left).diceVal).scalar;
				Die rhs = ((ScalarDiceExpression) ((DiceEvaluatorResult) right).diceVal).scalar;

				res = new DiceEvaluatorResult(new MathDie(MathDie.MathOp.ADD, lhs, rhs));
			} else {
				res = new FloatEvaluatorResult(
						((FloatEvaluatorResult) left).floatVal + ((FloatEvaluatorResult) right).floatVal);
			}

			break;
		}
		case SUBTRACT: {
			if (left.type == INT) {
				long lval = ((IntegerEvaluatorResult) left).value;
				long rval = ((IntegerEvaluatorResult) right).value;

				res = new IntegerEvaluatorResult(lval - rval);
			} else if (left.type == DICE) {
				if (((DiceEvaluatorResult) left).diceVal.isList()) {
					Errors.inst.printError(EK_EVAL_INVDICE, left.toString());
					return new SimpleTree<>(Node.FAIL(left));
				} else if (((DiceEvaluatorResult) right).diceVal.isList()) {
					Errors.inst.printError(EK_EVAL_INVDICE, right.toString());
					return new SimpleTree<>(Node.FAIL(right));
				}

				Die lhs = ((ScalarDiceExpression) ((DiceEvaluatorResult) left).diceVal).scalar;
				Die rhs = ((ScalarDiceExpression) ((DiceEvaluatorResult) right).diceVal).scalar;

				res = new DiceEvaluatorResult(new MathDie(MathDie.MathOp.SUBTRACT, lhs, rhs));
			} else {
				res = new FloatEvaluatorResult(
						((FloatEvaluatorResult) left).floatVal - ((FloatEvaluatorResult) right).floatVal);
			}

			break;
		}
		case MULTIPLY: {
			if (left.type == INT) {
				long lval = ((IntegerEvaluatorResult) left).value;
				long rval = ((IntegerEvaluatorResult) right).value;

				res = new IntegerEvaluatorResult(lval * rval);
			} else if (left.type == DICE) {
				if (((DiceEvaluatorResult) left).diceVal.isList()) {
					Errors.inst.printError(EK_EVAL_INVDICE, left.toString());
					return new SimpleTree<>(Node.FAIL(left));
				} else if (((DiceEvaluatorResult) right).diceVal.isList()) {
					Errors.inst.printError(EK_EVAL_INVDICE, right.toString());
					return new SimpleTree<>(Node.FAIL(right));
				}

				Die lhs = ((ScalarDiceExpression) ((DiceEvaluatorResult) left).diceVal).scalar;
				Die rhs = ((ScalarDiceExpression) ((DiceEvaluatorResult) right).diceVal).scalar;

				res = new DiceEvaluatorResult(new MathDie(MathDie.MathOp.MULTIPLY, lhs, rhs));
			} else {
				res = new FloatEvaluatorResult(
						((FloatEvaluatorResult) left).floatVal * ((FloatEvaluatorResult) right).floatVal);
			}

			break;
		}
		case DIVIDE: {
			if (left.type == INT) {
				long lval = ((IntegerEvaluatorResult) left).value;
				long rval = ((IntegerEvaluatorResult) right).value;

				if (rval == 0) {
					Errors.inst.printError(EK_EVAL_DIVZERO);
					res = new FailureEvaluatorResult(right);
				} else {
					res = new FloatEvaluatorResult(lval / rval);
				}
			} else if (left.type == FLOAT) {
				if (((FloatEvaluatorResult) right).floatVal == 0) {
					Errors.inst.printError(EK_EVAL_DIVZERO);
					res = new FailureEvaluatorResult(right);
				} else {
					res = new FloatEvaluatorResult(
							((FloatEvaluatorResult) left).floatVal / ((FloatEvaluatorResult) right).floatVal);
				}
			} else {
				Errors.inst.printError(EK_EVAL_DIVDICE);
				return new SimpleTree<>(Node.FAIL());
			}

			break;
		}
		case IDIVIDE: {
			if (left.type == INT) {
				long lval = ((IntegerEvaluatorResult) left).value;
				long rval = ((IntegerEvaluatorResult) right).value;

				if (rval == 0) {
					Errors.inst.printError(EK_EVAL_DIVZERO);
					res = new FailureEvaluatorResult(right);
				} else {
					res = new IntegerEvaluatorResult((int) (lval / rval));
				}
			} else if (left.type == FLOAT) {
				if (((FloatEvaluatorResult) right).floatVal == 0) {
					Errors.inst.printError(EK_EVAL_DIVZERO);
					res = new FailureEvaluatorResult(right);
				} else {
					res = new IntegerEvaluatorResult(
							(int) (((FloatEvaluatorResult) left).floatVal / ((FloatEvaluatorResult) right).floatVal));
				}
			} else {
				Errors.inst.printError(EK_EVAL_DIVDICE);
				return new SimpleTree<>(Node.FAIL());
			}

			break;
		}
		default:
			Errors.inst.printError(EK_EVAL_UNMATH, op.toString());
			return new SimpleTree<>(Node.FAIL());
		}

		return new SimpleTree<>(new Node(Node.Type.RESULT, res));
	}

	/* Evaluate a token reference. */
	private Tree<Node> evaluateTokenRef(final Token tk, final Context ctx) {
		EvaluatorResult res = null;

		switch (tk.type) {
		case INT_LIT:
			res = new IntegerEvaluatorResult(tk.intValue);
			break;
		case FLOAT_LIT:
			res = new FloatEvaluatorResult(((FloatToken) tk).floatValue);
			break;
		case DICE_LIT:
			res = new DiceEvaluatorResult(((DiceToken) tk).diceValue);
			break;
		case STRING_LIT:
			res = new StringEvaluatorResult(eng.getStringLiteral((int) tk.intValue));
			break;
		default:
			Errors.inst.printError(EK_EVAL_UNTOK, tk.type.toString());

			res = new EvaluatorResult(FAILURE);
		}

		return new SimpleTree<>(new Node(Node.Type.RESULT, res));
	}
}
