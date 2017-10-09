package bjc.dicelang;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.Consumer;

import bjc.dicelang.dice.CompoundDie;
import bjc.dicelang.dice.FudgeDie;
import bjc.dicelang.dice.MathDie;
import bjc.dicelang.dice.ScalarDie;
import bjc.dicelang.dice.SimpleDie;
import bjc.dicelang.dice.SimpleDieList;
import bjc.utils.data.ITree;
import bjc.utils.data.SingleIterator;
import bjc.utils.data.TopDownTransformIterator;
import bjc.utils.data.TopDownTransformResult;
import bjc.utils.data.Tree;

import static bjc.dicelang.Errors.ErrorKey.*;
import static bjc.dicelang.EvaluatorResult.Type.DICE;
import static bjc.dicelang.EvaluatorResult.Type.FAILURE;
import static bjc.dicelang.EvaluatorResult.Type.FLOAT;
import static bjc.dicelang.EvaluatorResult.Type.INT;
import static bjc.dicelang.EvaluatorResult.Type.STRING;


/* @TODO 10/09/17 Ben Culkin :EvaluatorSplit
 * 	Type/sanity checking should be moved into a seperate stage, not part of
 * 	evaluation.
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
		public Consumer<Iterator<ITree<Node>>> thunk;

		public boolean isDebug;

		public Context() {
			/* Empty block. */
		}
	}

	/* @TODO 10/09/17 Ben Culkin :NodeFAIL
	 * 	These methods should be moved to Node.
	 */
	/* Create a failing node. */
	private static Node FAIL() {
		return new Node(Node.Type.RESULT, new EvaluatorResult(FAILURE));
	}

	private static Node FAIL(final ITree<Node> orig) {
		return new Node(Node.Type.RESULT, new EvaluatorResult(FAILURE, orig));
	}

	private static Node FAIL(final Node orig) {
		return new Node(Node.Type.RESULT, new EvaluatorResult(FAILURE, orig));
	}

	private static Node FAIL(final EvaluatorResult res) {
		EvaluatorResult eres = new EvaluatorResult(FAILURE, new Node(Node.Type.RESULT, res));
		return new Node(Node.Type.RESULT, eres);
	}

	/* The engine we are connected to. */
	private final DiceLangEngine eng;

	/**
	 * Create a new evaluator.
	 *
	 * @param en
	 *                The engine.
	 */
	public Evaluator(final DiceLangEngine en) {
		eng = en;
	}

	/**
	 * Evaluate a AST.
	 *
	 * @param comm
	 *                The AST to evaluate.
	 *
	 * @return The result of the tree.
	 */
	public EvaluatorResult evaluate(final ITree<Node> comm) {
		final Context ctx = new Context();

		ctx.isDebug = false;
		ctx.thunk = itr -> {
			/*
			 * Deliberately finish the iterator, but ignore results.
			 * It's only for stepwise evaluation, but we don't know
			 * if stepping the iterator has side effects. 
			 */
			while (itr.hasNext()) {
				itr.next();
			}
		};

		/* The result. */
		final ITree<Node> res = comm.topDownTransform(
				this::pickEvaluationType,
				node -> this.evaluateNode(node, ctx));

		return res.getHead().resultVal;
	}

	/* @NOTE
	 * 	This is broken until stepwise top-down transforms are fixed. */
	public Iterator<ITree<Node>> stepDebug(final ITree<Node> comm) {
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

		default:
			return TopDownTransformResult.PUSHDOWN;
		}
	}

	/* Evaluate a node. */
	private ITree<Node> evaluateNode(final ITree<Node> ast, final Context ctx) {
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
			return new Tree<>(FAIL(ast));
		}
	}

	/* Evaluate a unary operator. */
	private ITree<Node> evaluateUnaryOp(final ITree<Node> ast, final Context ctx) {
		/* Unary operators only take one operand. */
		if (ast.getChildrenCount() != 1) {
			Errors.inst.printError(EK_EVAL_UNUNARY, Integer.toString(ast.getChildrenCount()));
			return new Tree<>(FAIL(ast));
		}

		switch (ast.getHead().operatorType) {
			/* 
			 * @TODO 10/09/17 Ben Culkin :CoerceRefactor :EvaluatorSplit
			 * 	Coercing should be moved to its own class, or at the
			 * 	very least its own method. When the evaluator splits,
			 * 	this node type'll be handled exclusively by the
			 * 	type-checker.
			 *
			 * 	Coerce also needs to be able to coerce things to
			 * 	dice and ratios (whenever they get added).
			 */
		case COERCE:
			final ITree<Node> toCoerce = ast.getChild(0);
			final ITree<Node> retVal = new Tree<>(toCoerce.getHead());
			final Deque<ITree<Node>> children = new LinkedList<>();

			/* The current type we are coercing to. */
			CoerceSteps curLevel = CoerceSteps.INTEGER;

			for (int i = 0; i < toCoerce.getChildrenCount(); i++) {
				final ITree<Node> child = toCoerce.getChild(i);
				ITree<Node> nChild = null;

				/* Tell our thunk we processed a node. */
				if (ctx.isDebug) {
					/* Evaluate each step of the child. */
					final Iterator<ITree<Node>> nd = stepDebug(child);

					for (; nd.hasNext(); nChild = nd.next()) {
						ctx.thunk.accept(new SingleIterator<>(child));
					}
				} else {
					/* Evaluate the child. */
					nChild = new Tree<>(new Node(Node.Type.RESULT, evaluate(child)));

					ctx.thunk.accept(new SingleIterator<>(nChild));
				}

				if (nChild == null) {
					Errors.inst.printError(EK_EVAL_INVNODE);
					return new Tree<>(FAIL(ast));
				}

				final Node childNode = nChild.getHead();
				final EvaluatorResult res = childNode.resultVal;

				/* Move up to coercing to a float. */
				if (res.type == FLOAT) {
					curLevel = CoerceSteps.DOUBLE;
				}

				children.add(nChild);
			}

			for (final ITree<Node> child : children) {
				final Node nd = child.getHead();
				final EvaluatorResult res = nd.resultVal;

				switch (res.type) {
				case INT:
					/* Coerce ints to doubles if we need to. */
					if (curLevel == CoerceSteps.DOUBLE) {
						nd.resultVal = new EvaluatorResult(FLOAT, (double) res.intVal);
					}
				default:
					/* Do nothing */
					break;
				}

				retVal.addChild(child);
			}

			return retVal;
		case DICESCALAR:
			final EvaluatorResult opr = ast.getChild(0).getHead().resultVal;

			if (opr.type != INT) {
				Errors.inst.printError(EK_EVAL_INVDCREATE, opr.type.toString());
			}

			final EvaluatorResult sres = new EvaluatorResult(DICE, new ScalarDie(opr.intVal));
			return new Tree<>(new Node(Node.Type.RESULT, sres));
		case DICEFUDGE:
			final EvaluatorResult oprn = ast.getChild(0).getHead().resultVal;

			if (oprn.type != INT) {
				Errors.inst.printError(EK_EVAL_INVDCREATE, oprn.type.toString());
			}

			final EvaluatorResult fres = new EvaluatorResult(DICE, new ScalarDie(oprn.intVal));
			return new Tree<>(new Node(Node.Type.RESULT, fres));
		default:
			Errors.inst.printError(EK_EVAL_INVUNARY, ast.getHead().operatorType.toString());
			return new Tree<>(FAIL(ast));
		}
	}

	/* Evaluate a binary operator. */
	private static ITree<Node> evaluateBinaryOp(final ITree<Node> ast, final Context ctx) {
		final Token.Type binOp = ast.getHead().operatorType;

		/* Binary operators always have two children. */
		if (ast.getChildrenCount() != 2) {
			Errors.inst.printError(EK_EVAL_INVBIN, Integer.toString(ast.getChildrenCount()),
					ast.toString());

			return new Tree<>(FAIL(ast));
		}

		final ITree<Node> left  = ast.getChild(0);
		final ITree<Node> right = ast.getChild(1);

		final EvaluatorResult leftRes  = left.getHead().resultVal;
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
			return new Tree<>(FAIL(ast));
		}
	}

	/* Evaluate a binary operator on strings. */
	private static ITree<Node> evaluateStringBinary(final Token.Type op,
			final EvaluatorResult left,
			final EvaluatorResult right, final Context ctx) {
		if (left.type != STRING) {
			Errors.inst.printError(EK_EVAL_INVSTRING, left.type.toString());
			return new Tree<>(FAIL(left));
		}

		final String strang = left.stringVal;

		switch (op) {
		case STRCAT:
			if (right.type != STRING) {
				Errors.inst.printError(EK_EVAL_UNSTRING, right.type.toString());
				return new Tree<>(FAIL(right));
			}

			final String strung = right.stringVal;
			final EvaluatorResult cres = new EvaluatorResult(STRING, strang + strung);

			return new Tree<>(new Node(Node.Type.RESULT, cres));
		case STRREP:
			if (right.type != INT) {
				Errors.inst.printError(EK_EVAL_INVSTRING, right.type.toString());
				return new Tree<>(FAIL(right));
			}

			String res = strang;
			final long count = right.intVal;

			for (long i = 1; i < count; i++) {
				res += strang;
			}

			return new Tree<>(new Node(Node.Type.RESULT, new EvaluatorResult(STRING, res)));
		default:
			Errors.inst.printError(EK_EVAL_UNSTRING, op.toString());
			return new Tree<>(FAIL());
		}
	}

	/* Evaluate dice binary operators. */
	private static ITree<Node> evaluateDiceBinary(final Token.Type op,
			final EvaluatorResult left,
			final EvaluatorResult right, final Context ctx) {
		EvaluatorResult res = null;

		switch (op) {
			/*
			 * @TODO 10/09/17 Ben Culkin :DiceSimplify
			 * 	Figure out some way to simplify this sort of
			 * 	thing.
			 */
		case DICEGROUP:
			if (left.type == DICE && !left.diceVal.isList) {
				if (right.type == DICE && !right.diceVal.isList) {
					Die simple = new SimpleDie(
							left.diceVal.scalar,
							right.diceVal.scalar);

					res = new EvaluatorResult(DICE, simple);
				} else if (right.type == INT) {
					res = new EvaluatorResult(DICE,
							new SimpleDie(left.diceVal.scalar, right.intVal));
				} else {
					Errors.inst.printError(EK_EVAL_INVDGROUP, right.type.toString());
					return new Tree<>(FAIL(right));
				}
			} else if (left.type == INT) {
				if (right.type == DICE && !right.diceVal.isList) {
					res = new EvaluatorResult(DICE,
							new SimpleDie(left.intVal, right.diceVal.scalar));
				} else if (right.type == INT) {
					res = new EvaluatorResult(DICE, new SimpleDie(left.intVal, right.intVal));
				} else {
					Errors.inst.printError(EK_EVAL_INVDGROUP, right.type.toString());
					return new Tree<>(FAIL(right));
				}
			} else {
				Errors.inst.printError(EK_EVAL_INVDGROUP, left.type.toString());
				return new Tree<>(FAIL(left));
			}

		case DICECONCAT:
			if (left.type != DICE || left.diceVal.isList) {
				Errors.inst.printError(EK_EVAL_INVDICE, left.type.toString());
				return new Tree<>(FAIL(left));
			} else if (right.type != DICE || right.diceVal.isList) {
				Errors.inst.printError(EK_EVAL_INVDICE, right.type.toString());
				return new Tree<>(FAIL(right));
			} else {
				res = new EvaluatorResult(DICE,
						new CompoundDie(left.diceVal.scalar, right.diceVal.scalar));
			}

			break;

		case DICELIST:
			if (left.type != DICE || left.diceVal.isList) {
				Errors.inst.printError(EK_EVAL_INVDICE, left.type.toString());
				return new Tree<>(FAIL(left));
			} else if (right.type != DICE || right.diceVal.isList) {
				Errors.inst.printError(EK_EVAL_INVDICE, right.type.toString());
				return new Tree<>(FAIL(right));
			} else {
				res = new EvaluatorResult(DICE,
						new SimpleDieList(left.diceVal.scalar, right.diceVal.scalar));
			}

			break;

		default:
			Errors.inst.printError(EK_EVAL_UNDICE, op.toString());
			return new Tree<>(FAIL());
		}

		return new Tree<>(new Node(Node.Type.RESULT, res));
	}

	/* Evaluate a binary math operator. */
	private static ITree<Node> evaluateMathBinary(final Token.Type op,
			final EvaluatorResult left,
			final EvaluatorResult right, final Context ctx) {
		if (left.type == STRING || right.type == STRING) {
			Errors.inst.printError(EK_EVAL_STRINGMATH);
			return new Tree<>(FAIL());
		} else if (left.type == FAILURE || right.type == FAILURE) {
			return new Tree<>(FAIL());
		} else if (left.type == INT && right.type != INT) {
			Errors.inst.printError(EK_EVAL_MISMATH);
			return new Tree<>(FAIL(right));
		} else if (left.type == FLOAT && right.type != FLOAT) {
			Errors.inst.printError(EK_EVAL_MISMATH);
			return new Tree<>(FAIL(right));
		} else if (left.type == DICE && right.type != DICE) {
			Errors.inst.printError(EK_EVAL_MISMATH);
			return new Tree<>(FAIL(right));
		} else if (right.type == INT && left.type != INT) {
			Errors.inst.printError(EK_EVAL_MISMATH);
			return new Tree<>(FAIL(left));
		} else if (right.type == FLOAT && left.type != FLOAT) {
			Errors.inst.printError(EK_EVAL_MISMATH);
			return new Tree<>(FAIL(left));
		} else if (right.type == DICE && left.type != DICE) {
			Errors.inst.printError(EK_EVAL_MISMATH);
			return new Tree<>(FAIL(left));
		}

		EvaluatorResult res = null;

		switch (op) {
		case ADD:
			if (left.type == INT) {
				res = new EvaluatorResult(INT, left.intVal + right.intVal);
			} else if (left.type == DICE) {
				if (left.diceVal.isList) {
					Errors.inst.printError(EK_EVAL_INVDICE, left.toString());
					return new Tree<>(FAIL(left));
				} else if (right.diceVal.isList) {
					Errors.inst.printError(EK_EVAL_INVDICE, right.toString());
					return new Tree<>(FAIL(right));
				}

				res = new EvaluatorResult(DICE, new MathDie(MathDie.MathOp.ADD, left.diceVal.scalar,
							right.diceVal.scalar));
			} else {
				res = new EvaluatorResult(FLOAT, left.floatVal + right.floatVal);
			}

			break;

		case SUBTRACT:
			if (left.type == INT) {
				res = new EvaluatorResult(INT, left.intVal - right.intVal);
			} else if (left.type == DICE) {
				if (left.diceVal.isList) {
					Errors.inst.printError(EK_EVAL_INVDICE, left.toString());
					return new Tree<>(FAIL(left));
				} else if (right.diceVal.isList) {
					Errors.inst.printError(EK_EVAL_INVDICE, right.toString());
					return new Tree<>(FAIL(right));
				}

				res = new EvaluatorResult(DICE, new MathDie(MathDie.MathOp.SUBTRACT,
							left.diceVal.scalar, right.diceVal.scalar));
			} else {
				res = new EvaluatorResult(FLOAT, left.floatVal - right.floatVal);
			}

			break;

		case MULTIPLY:
			if (left.type == INT) {
				res = new EvaluatorResult(INT, left.intVal * right.intVal);
			} else if (left.type == DICE) {
				if (left.diceVal.isList) {
					Errors.inst.printError(EK_EVAL_INVDICE, left.toString());
					return new Tree<>(FAIL(left));
				} else if (right.diceVal.isList) {
					Errors.inst.printError(EK_EVAL_INVDICE, right.toString());
					return new Tree<>(FAIL(right));
				}

				res = new EvaluatorResult(DICE, new MathDie(MathDie.MathOp.MULTIPLY,
							left.diceVal.scalar, right.diceVal.scalar));
			} else {
				res = new EvaluatorResult(FLOAT, left.floatVal * right.floatVal);
			}

			break;

		case DIVIDE:
			if (left.type == INT) {
				if (right.intVal == 0) {
					Errors.inst.printError(EK_EVAL_DIVZERO);
					res = new EvaluatorResult(FAILURE, right);
				} else {
					res = new EvaluatorResult(FLOAT, left.intVal / right.intVal);
				}
			} else if (left.type == FLOAT) {
				if (right.floatVal == 0) {
					Errors.inst.printError(EK_EVAL_DIVZERO);
					res = new EvaluatorResult(FAILURE, right);
				} else {
					res = new EvaluatorResult(FLOAT, left.floatVal / right.floatVal);
				}
			} else {
				Errors.inst.printError(EK_EVAL_DIVDICE);
				return new Tree<>(FAIL());
			}

			break;

		case IDIVIDE:
			if (left.type == INT) {
				if (right.intVal == 0) {
					Errors.inst.printError(EK_EVAL_DIVZERO);
					res = new EvaluatorResult(FAILURE, right);
				} else {
					res = new EvaluatorResult(INT, (int) (left.intVal / right.intVal));
				}
			} else if (left.type == FLOAT) {
				if (right.floatVal == 0) {
					Errors.inst.printError(EK_EVAL_DIVZERO);
					res = new EvaluatorResult(FAILURE, right);
				} else {
					res = new EvaluatorResult(INT, (int) (left.floatVal / right.floatVal));
				}
			} else {
				Errors.inst.printError(EK_EVAL_DIVDICE);
				return new Tree<>(FAIL());
			}

			break;

		default:
			Errors.inst.printError(EK_EVAL_UNMATH, op.toString());
			return new Tree<>(FAIL());
		}

		return new Tree<>(new Node(Node.Type.RESULT, res));
	}

	/* Evaluate a token reference. */
	private ITree<Node> evaluateTokenRef(final Token tk, final Context ctx) {
		EvaluatorResult res = null;

		switch (tk.type) {
		case INT_LIT:
			res = new EvaluatorResult(INT, tk.intValue);
			break;
		case FLOAT_LIT:
			res = new EvaluatorResult(FLOAT, tk.floatValue);
			break;
		case DICE_LIT:
			res = new EvaluatorResult(DICE, tk.diceValue);
			break;
		case STRING_LIT:
			res = new EvaluatorResult(STRING, eng.getStringLiteral((int) tk.intValue));
			break;
		default:
			Errors.inst.printError(EK_EVAL_UNTOK, tk.type.toString());
			res = new EvaluatorResult(FAILURE);
		}

		return new Tree<>(new Node(Node.Type.RESULT, res));
	}
}
