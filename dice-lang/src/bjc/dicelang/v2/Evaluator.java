package bjc.dicelang.v2;

import bjc.utils.data.ITree;
import bjc.utils.data.Tree;
import bjc.utils.data.TopDownTransformResult;

import static bjc.dicelang.v2.Errors.ErrorKey.*;
import static bjc.dicelang.v2.Evaluator.Result.Type.*;

public class Evaluator {
	public static class Result {
		public static enum Type {
			FAILURE,
			INT, FLOAT, DICE, STRING
		}

		public final Type type;

		// These may or may not have values based
		// off of the result type
		public long intVal;
		public double floatVal;
		public DiceBox.DieExpression diceVal;
		public String stringVal;

		public Result(Type typ) {
			type = typ;
		}

		public Result(Type typ, long iVal) {
			this(typ);

			intVal = iVal;
		}

		public Result(Type typ, double dVal) {
			this(typ);

			floatVal = dVal;
		}

		public Result(Type typ, DiceBox.DieExpression dVal) {
			this(typ);

			diceVal = dVal;
		}

		public Result(Type typ, DiceBox.Die dVal) {
			this(typ);

			diceVal = new DiceBox.DieExpression(dVal);
		}

		public Result(Type typ, DiceBox.DieList dVal) {
			this(typ);

			diceVal = new DiceBox.DieExpression(dVal);
		}

		public Result(Type typ, String strang) {
			this(typ);

			stringVal = strang;
		}

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

	private final static Node FAIL = new Node(Node.Type.RESULT, new Result(Result.Type.FAILURE));

	private DiceLangEngine eng;

	public Evaluator(DiceLangEngine en) {
		eng = en;
	}

	public Result evaluate(ITree<Node> comm) {
		return comm.topDownTransform(this::pickEvaluationType, this::evaluateNode).getHead().resultVal;
	}

	private TopDownTransformResult pickEvaluationType(Node nd) {
		switch(nd.type) {
			default:
				return TopDownTransformResult.PUSHDOWN;
		}
	}

	private ITree<Node> evaluateNode(ITree<Node> ast) {
		switch(ast.getHead().type) {
			case UNARYOP:
				System.out.println("\tEVALUATOR ERROR: Unary operator evaluation isn't supported yet");
				return new Tree<>(FAIL);
			case BINOP:
				return evaluateBinaryOp(ast);
			case TOKREF:
				return evaluateTokenRef(ast.getHead().tokenVal);
			case ROOT:
				return ast.getChild(ast.getChildrenCount() - 1);
			default:
				Errors.inst.printError(EK_EVAL_INVNODE, ast.getHead().type.toString());
				return new Tree<>(FAIL);
		}
	}

	private ITree<Node> evaluateBinaryOp(ITree<Node> ast) {
		Token.Type binOp = ast.getHead().operatorType;

		if(ast.getChildrenCount() != 2) {
			Errors.inst.printError(EK_EVAL_INVBIN, Integer.toString(ast.getChildrenCount()));

			return new Tree<>(FAIL);
		}

		ITree<Node> left  = ast.getChild(0);
		ITree<Node> right = ast.getChild(1);

		switch(binOp) {
			case ADD:
			case SUBTRACT:
			case MULTIPLY:
			case DIVIDE:
			case IDIVIDE:
				return evaluateMathBinary(binOp, left.getHead().resultVal, right.getHead().resultVal);
			case DICEGROUP:
			case DICECONCAT:
			case DICELIST:
				return evaluateDiceBinary(binOp, left.getHead().resultVal, right.getHead().resultVal);
			default:
				Errors.inst.printError(EK_EVAL_UNBIN, binOp.toString());
				return new Tree<>(FAIL);
		}
	}

	private ITree<Node> evaluateDiceBinary(Token.Type op, Result left, Result right) {
		Result res = null;

		switch(op) {
			case DICEGROUP:
				if(left.type == DICE && !left.diceVal.isList) {
					if(right.type == DICE && !right.diceVal.isList) {
						res = new Result(DICE, new DiceBox.SimpleDie(left.diceVal.scalar,
									right.diceVal.scalar));
					} else if (right.type == INT) {
						res = new Result(DICE, new DiceBox.SimpleDie(left.diceVal.scalar, right.intVal));
					} else {
						Errors.inst.printError(EK_EVAL_INVDGROUP, right.type.toString());
						return new Tree<>(FAIL);
					}
				} else if(left.type == INT) {
					if(right.type == DICE && !right.diceVal.isList) {
						res = new Result(DICE, new DiceBox.SimpleDie(left.intVal, right.diceVal.scalar));
					} else if (right.type == INT) {
						res = new Result(DICE, new DiceBox.SimpleDie(left.intVal, right.intVal));
					} else {
						Errors.inst.printError(EK_EVAL_INVDGROUP, right.type.toString());
						return new Tree<>(FAIL);
					}
				} else {
					Errors.inst.printError(EK_EVAL_INVDGROUP, left.type.toString());
					return new Tree<>(FAIL);
				}
			case DICECONCAT:
				if(left.type != DICE || left.diceVal.isList) {
					Errors.inst.printError(EK_EVAL_INVDICE, left.type.toString());
					return new Tree<>(FAIL);
				} else if(right.type != DICE || right.diceVal.isList) {
					Errors.inst.printError(EK_EVAL_INVDICE, right.type.toString());
					return new Tree<>(FAIL);
				} else {
					res = new Result(DICE, new DiceBox.CompoundDie(left.diceVal.scalar,
								right.diceVal.scalar));
				}
				break;
			case DICELIST:
				if(left.type != DICE || left.diceVal.isList) {
					Errors.inst.printError(EK_EVAL_INVDICE, left.type.toString());
					return new Tree<>(FAIL);
				} else if(right.type != DICE || right.diceVal.isList) {
					Errors.inst.printError(EK_EVAL_INVDICE, right.type.toString());
					return new Tree<>(FAIL);
				} else {
					res = new Result(DICE, new DiceBox.SimpleDieList(left.diceVal.scalar,
								right.diceVal.scalar));
				}
				break;
			default:
				Errors.inst.printError(EK_EVAL_UNDICE, op.toString());
				return new Tree<>(FAIL);
		}

		return new Tree<>(new Node(Node.Type.RESULT, res));
	}

	private ITree<Node> evaluateMathBinary(Token.Type op, Result left, Result right) {
		if(left.type == Result.Type.DICE || right.type == Result.Type.DICE) {
			System.out.println("\tEVALUATOR ERROR: Math on dice isn't supported yet");
			return new Tree<>(FAIL);
		} else if(left.type == Result.Type.STRING || right.type == Result.Type.STRING) {
			System.out.println("\tERROR: Math operators don't work on strings");
			return new Tree<>(FAIL);
		} else if(left.type == Result.Type.FAILURE || right.type == Result.Type.FAILURE) {
			return new Tree<>(FAIL);
		}

		Result res = null;

		switch(op) {
			case ADD:
				if(left.type == Result.Type.INT) {
					if(right.type == Result.Type.INT) {
						res = new Result(Result.Type.INT, left.intVal + right.intVal);
					} else {
						res = new Result(Result.Type.FLOAT, left.intVal + right.floatVal);
					}
				} else {
					if(right.type == Result.Type.INT) {
						res = new Result(Result.Type.FLOAT, left.floatVal + right.intVal);
					} else {
						res = new Result(Result.Type.FLOAT, left.floatVal + right.floatVal);
					}
				}
				break;
			case SUBTRACT:
				if(left.type == Result.Type.INT) {
					if(right.type == Result.Type.INT) {
						res = new Result(Result.Type.INT, left.intVal - right.intVal);
					} else {
						res = new Result(Result.Type.FLOAT, left.intVal - right.floatVal);
					}
				} else {
					if(right.type == Result.Type.INT) {
						res = new Result(Result.Type.FLOAT, left.floatVal - right.intVal);
					} else {
						res = new Result(Result.Type.FLOAT, left.floatVal - right.floatVal);
					}
				}
				break;
			case MULTIPLY:
				if(left.type == Result.Type.INT) {
					if(right.type == Result.Type.INT) {
						res = new Result(Result.Type.INT, left.intVal * right.intVal);
					} else {
						res = new Result(Result.Type.FLOAT, left.intVal * right.floatVal);
					}
				} else {
					if(right.type == Result.Type.INT) {
						res = new Result(Result.Type.FLOAT, left.floatVal * right.intVal);
					} else {
						res = new Result(Result.Type.FLOAT, left.floatVal * right.floatVal);
					}
				}
				break;
			case DIVIDE:
				if(left.type == Result.Type.INT) {
					if(right.type == Result.Type.INT) {
						if(right.intVal == 0) {
							Errors.inst.printError(EK_EVAL_DIVZERO);
							res = new Result(Result.Type.FAILURE);
						} else {
							res = new Result(Result.Type.FLOAT, left.intVal / right.intVal);
						}
					} else {
						if(right.floatVal == 0) {
							Errors.inst.printError(EK_EVAL_DIVZERO);
							res = new Result(Result.Type.FAILURE);
						} else {
							res = new Result(Result.Type.FLOAT, left.intVal / right.floatVal);
						}
					}
				} else {
					if(right.type == Result.Type.INT) {
						if(right.intVal == 0) {
							Errors.inst.printError(EK_EVAL_DIVZERO);
							res = new Result(Result.Type.FAILURE);
						} else {
							res = new Result(Result.Type.FLOAT, left.floatVal / right.intVal);
						}
					} else {
						if(right.floatVal == 0) {
							Errors.inst.printError(EK_EVAL_DIVZERO);
							res = new Result(Result.Type.FAILURE);
						} else {
							res = new Result(Result.Type.FLOAT, left.floatVal / right.floatVal);
						}
					}
				}
				break;
			case IDIVIDE:
				if(left.type == Result.Type.INT) {
					if(right.type == Result.Type.INT) {
						if(right.intVal == 0) {
							Errors.inst.printError(EK_EVAL_DIVZERO);
							res = new Result(Result.Type.FAILURE);
						} else {
							res = new Result(Result.Type.INT, (int) (left.intVal / right.intVal));
						}
					} else {
						if(right.floatVal == 0) {
							Errors.inst.printError(EK_EVAL_DIVZERO);
							res = new Result(Result.Type.FAILURE);
						} else {
							res = new Result(Result.Type.INT, (int) (left.intVal / right.floatVal));
						}
					}
				} else {
					if(right.type == Result.Type.INT) {
						if(right.intVal == 0) {
							Errors.inst.printError(EK_EVAL_DIVZERO);
							res = new Result(Result.Type.FAILURE);
						} else {
							res = new Result(Result.Type.INT, (int) (left.floatVal / right.intVal));
						}
					} else {
						if(right.floatVal == 0) {
							Errors.inst.printError(EK_EVAL_DIVZERO);
							res = new Result(Result.Type.FAILURE);
						} else {
							res = new Result(Result.Type.INT, (int) (left.floatVal / right.floatVal));
						}
					}
				}
				break;
			default:
				Errors.inst.printError(EK_EVAL_UNMATH, op.toString());
				return new Tree<>(FAIL);
		}

		return new Tree<>(new Node(Node.Type.RESULT, res));
	}

	private ITree<Node> evaluateTokenRef(Token tk) {
		Result res = null;

		switch(tk.type) {
			case INT_LIT:
				res = new Result(Result.Type.INT, tk.intValue);
				break;
			case FLOAT_LIT:
				res = new Result(Result.Type.FLOAT, tk.floatValue);
				break;
			case DICE_LIT:
				res = new Result(Result.Type.DICE, tk.diceValue);
				break;
			case STRING_LIT:
				res = new Result(Result.Type.STRING, eng.stringLits.get((int)(tk.intValue)));
				break;
			default:
				Errors.inst.printError(EK_EVAL_UNTOK, tk.type.toString());
				res = new Result(Result.Type.FAILURE);
		}

		return new Tree<>(new Node(Node.Type.RESULT, res));
	}
}
