package bjc.dicelang.v2;

import bjc.utils.funcdata.FunctionalList;
import bjc.utils.funcdata.FunctionalMap;
import bjc.utils.funcdata.IList;
import bjc.utils.funcdata.IMap;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import static bjc.dicelang.v2.Errors.ErrorKey.*;
import static bjc.dicelang.v2.Token.Type.*;

public class Shunter {
	// The binary operators and their
	// priorities
	private IMap<Token.Type, Integer> ops;

	// Unary operators that can only be
	// applied to non-operator tokens
	private Set<Token.Type> unaryAdjectives;
	
	// Unary operators that con only be
	// applied to operator tokens
	private Set<Token.Type> unaryAdverbs;

	private final int MATH_PREC	 = 20;
	private final int DICE_PREC	 = 10;
	private final int EXPR_PREC	 = 0;

	public Shunter() {
		ops = new FunctionalMap<>();

		unaryAdjectives = new HashSet<>();
		unaryAdverbs    = new HashSet<>();

		ops.put(ADD,      0 + MATH_PREC);
		ops.put(SUBTRACT, 0 + MATH_PREC);

		ops.put(MULTIPLY, 1 + MATH_PREC);
		ops.put(IDIVIDE,  1 + MATH_PREC);
		ops.put(DIVIDE,   1 + MATH_PREC);

		ops.put(DICEGROUP,  0 + DICE_PREC);
		ops.put(DICECONCAT, 1 + DICE_PREC);
		ops.put(DICELIST,   2 + DICE_PREC);

		ops.put(LET,  0 + EXPR_PREC);
		ops.put(BIND, 1 + EXPR_PREC);
	}

	public boolean shuntTokens(IList<Token> tks, IList<Token> returned) {
		Deque<Token> opStack    = new LinkedList<>();

		boolean unaryMode     = false;
		Deque<Token> unaryOps = new LinkedList<>();

		Deque<Token> currReturned = new LinkedList<>();

		for(Token tk : tks.toIterable()) {
			if(unaryMode) {
				if(unaryAdjectives.contains(tk.type) || unaryAdverbs.contains(tk.type)) {
					unaryOps.push(tk);
					continue;
				}

				Token currOperator = unaryOps.peek();

				if(unaryAdjectives.contains(currOperator.type)) {
					boolean isOp = ops.containsKey(tk.type) 
						|| unaryAdverbs.contains(tk.type)
						|| unaryAdjectives.contains(tk.type);

					if(isOp) {
						Errors.inst.printError(EK_SHUNT_NOTADV, currOperator.toString(), tk.toString());
						return false;
					}

					currReturned.addLast(tk);
					currReturned.addLast(unaryOps.pop());
				} else if (unaryAdverbs.contains(currOperator.type)) {
					if(opStack.size() < 1) {
						Errors.inst.printError(EK_SHUNT_NOOP, currOperator.toString());
						return false;
					}

					Token currOperand = opStack.peek();

					boolean isOp = ops.containsKey(currOperand.type) 
						|| unaryAdverbs.contains(currOperand.type)
						|| unaryAdjectives.contains(currOperand.type);

					if(!isOp) {
						Errors.inst.printError(EK_SHUNT_NOTADJ,
								currOperator.toString(),
								tk.toString());
						
						return false;
					}

					currReturned.addLast(tk);
					currReturned.addLast(unaryOps.pop());
				}

				if(unaryOps.isEmpty()) unaryMode = false;
			} else {
				if(unaryAdjectives.contains(tk.type) || unaryAdverbs.contains(tk.type)) {
					unaryMode = true;

					unaryOps.add(tk);
					continue;
				} else if(ops.containsKey(tk.type)) {
					while(!opStack.isEmpty() 
							&& isHigherPrec(tk.type, opStack.peek().type)) {
						currReturned.addLast(opStack.pop());
					}

					opStack.push(tk);
				} else if(tk.type == OPAREN || tk.type == OBRACE) {
					opStack.push(tk);

					if(tk.type == OBRACE)
						currReturned.addLast(tk);
				} else if(tk.type == CPAREN || tk.type == CBRACE) {
					Token matching = null;

					switch(tk.type) {
						case CPAREN:
							matching = new Token(OPAREN, tk.intValue);
							break;
						case CBRACE:
							matching = new Token(OBRACE, tk.intValue);
							break;
						default:
							break;
					}

					if(!opStack.contains(matching)) {
						Errors.inst.printError(EK_SHUNT_NOGROUP,
								tk.toString(), matching.toString());
						return false;
					}

					while(!opStack.peek().equals(matching)) {
						currReturned.addLast(opStack.pop());
					}

					if(tk.type == CBRACE) {
						currReturned.addLast(tk);
					}

					opStack.pop();
				} else if(tk.type == GROUPSEP) {
					IList<Token> group = new FunctionalList<>();

					while(currReturned.size() != 0 && !currReturned.peek().isGrouper()) {
						group.add(currReturned.pop());
					}

					while(opStack.size() != 0 && !opStack.peek().isGrouper()) {
						group.add(opStack.pop());
					}

					if(currReturned.size() == 0) {
						Errors.inst.printError(EK_SHUNT_INVSEP);
						return false;
					}

					currReturned.addLast(new Token(TOKGROUP, group));
				} else {
					currReturned.addLast(tk);
				}
			}
		}

		// Flush leftover operators
		while(!opStack.isEmpty()) {
			currReturned.addLast(opStack.pop());
		}

		for(Token tk : currReturned) {
			returned.add(tk);
		}

		return true;
	}

	private boolean isHigherPrec(Token.Type left, Token.Type right) {
		boolean exists = ops.containsKey(right);

		// If it doesn't, the left is higher precedence.
		if (!exists) {
			return false;
		}

		int rightPrecedence = ops.get(right);
		int leftPrecedence  = ops.get(left);

		return rightPrecedence >= leftPrecedence;
	}
}
