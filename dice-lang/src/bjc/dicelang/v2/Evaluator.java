package bjc.dicelang.v2;

import bjc.utils.data.ITree;
import bjc.utils.data.Tree;
import bjc.utils.data.TopDownTransformResult;

public class Evaluator {
	public static class Result {
		public static enum Type {
			FAILURE,
			INT, FLOAT, DICE
		}

		public final Type type;

		// These may or may not have values based
		// off of the result type
		public long intVal;
		public double floatVal;
		public DiceBox.DieExpression diceVal;

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

		public String toString() {
			switch(type) {
				case INT:
					return type.toString() + "(" + intVal + ")";
				case FLOAT:
					return type.toString() + "(" + floatVal + ")";
				case DICE:
					return type.toString() + "(" + diceVal + ")";
				case FAILURE:
					return type.toString();
				default:
					return "Unknown result type " + type.toString();
			}
		}
	}

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
				return new Tree<>(new Node(Node.Type.RESULT, new Result(Result.Type.FAILURE)));
			case BINOP:
				return evaluateBinaryOp(ast);
			case TOKREF:
				return evaluateTokenRef(ast.getHead().tokenVal);
			default:
				System.out.println("\tERROR: Unknown node in evaluator: " + ast.getHead().type);
				return new Tree<>(new Node(Node.Type.RESULT, new Result(Result.Type.FAILURE)));
		}
	}

	private ITree<Node> evaluateBinaryOp(ITree<Node> ast) {
		Token.Type binOp = ast.getHead().operatorType;

		if(ast.getChildrenCount() != 2) {
			System.out.println("\tERROR: Binary operators only take two operands");
			return new Tree<>(new Node(Node.Type.RESULT, new Result(Result.Type.FAILURE)));
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
			default:
				System.out.println("\tERROR: Unknown binary operator: " + binOp);
				return new Tree<>(new Node(Node.Type.RESULT, new Result(Result.Type.FAILURE)));
		}
	}

	private ITree<Node> evaluateMathBinary(Token.Type op, Result left, Result right) {
		Result.Type resultType;

		if(left.type == Result.Type.DICE || right.type == Result.Type.DICE) {
			System.out.println("\tEVALUATOR ERROR: Math on dice isn't supported yet");
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
							System.out.println("\tERROR: Attempted divide by zero");
							res = new Result(Result.Type.FAILURE);
						} else {
							res = new Result(Result.Type.FLOAT, left.intVal / right.intVal);
						}
					} else {
						if(right.floatVal == 0) {
							System.out.println("\tERROR: Attempted divide by zero");
							res = new Result(Result.Type.FAILURE);
						} else {
							res = new Result(Result.Type.FLOAT, left.intVal / right.floatVal);
						}
					}
				} else {
					if(right.type == Result.Type.INT) {
						if(right.intVal == 0) {
							System.out.println("\tERROR: Attempted divide by zero");
							res = new Result(Result.Type.FAILURE);
						} else {
							res = new Result(Result.Type.FLOAT, left.floatVal / right.intVal);
						}
					} else {
						if(right.floatVal == 0) {
							System.out.println("\tERROR: Attempted divide by zero");
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
							System.out.println("\tERROR: Attempted divide by zero");
							res = new Result(Result.Type.FAILURE);
						} else {
							res = new Result(Result.Type.INT, (int) (left.intVal / right.intVal));
						}
					} else {
						if(right.floatVal == 0) {
							System.out.println("\tERROR: Attempted divide by zero");
							res = new Result(Result.Type.FAILURE);
						} else {
							res = new Result(Result.Type.INT, (int) (left.intVal / right.floatVal));
						}
					}
				} else {
					if(right.type == Result.Type.INT) {
						if(right.intVal == 0) {
							System.out.println("\tERROR: Attempted divide by zero");
							res = new Result(Result.Type.FAILURE);
						} else {
							res = new Result(Result.Type.INT, (int) (left.floatVal / right.intVal));
						}
					} else {
						if(right.floatVal == 0) {
							System.out.println("\tERROR: Attempted divide by zero");
							res = new Result(Result.Type.FAILURE);
						} else {
							res = new Result(Result.Type.INT, (int) (left.floatVal / right.floatVal));
						}
					}
				}
				break;
			default:
				System.out.println("\tERROR: Unknown math binary operator: " + op);
				return new Tree<>(new Node(Node.Type.RESULT, new Result(Result.Type.FAILURE)));
		}

		return new Tree<>(new Node(Node.Type.RESULT, res));
	}

	private ITree<Node> evaluateTokenRef(Token tk) {
		switch(tk.type) {
			case INT_LIT:
				return new Tree<>(new Node(Node.Type.RESULT, new Result(Result.Type.INT, tk.intValue)));
			case FLOAT_LIT:
				return new Tree<>(new Node(Node.Type.RESULT, new Result(Result.Type.FLOAT, tk.floatValue)));
			case DICE_LIT:
				return new Tree<>(new Node(Node.Type.RESULT, new Result(Result.Type.DICE, tk.diceValue)));
			default:
				System.out.println("\tERROR: Unknown token ref: " + tk.type);
				return new Tree<>(new Node(Node.Type.RESULT, new Result(Result.Type.FAILURE)));
		}
	}
}
