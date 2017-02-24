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
	// applied to non-operator tokens and yield operator tokens
	private Set<Token.Type> unaryAdjectives;
	
	// Unary operators that can only be
	// applied to operator tokens and yield operator tokens
	private Set<Token.Type> unaryAdverbs;

	// Unary operators that can only be
	// applied to operator tokens and yield data tokens
	private Set<Token.Type> unaryGerunds;

	private final int MATH_PREC	 = 20;
	private final int DICE_PREC	 = 10;
	private final int EXPR_PREC	 = 0;

	public Shunter() {
		ops = new FunctionalMap<>();

		unaryAdjectives = new HashSet<>();
		unaryAdverbs    = new HashSet<>();
		unaryGerunds    = new HashSet<>();

		unaryAdjectives.add(COERCE);

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

	private boolean isUnary(Token ty) {
		switch(ty.type) {
			default:
				return false;
		}
	}

	private boolean isOp(Token tk) {
		Token.Type ty = tk.type;

		if(ops.containsKey(ty))          return true;
		if(unaryAdjectives.contains(ty)) return true;
		if(unaryAdverbs.contains(ty))    return true;
		if(unaryGerunds.contains(ty))    return true;
		if(ty == TAGOPR)                 return true;

		return false;
	}

	public boolean shuntTokens(IList<Token> tks, IList<Token> returned) {
		Deque<Token> opStack    = new LinkedList<>();

		boolean unaryMode     = false;
		Deque<Token> unaryOps = new LinkedList<>();

		Deque<Token> currReturned = new LinkedList<>();

		for(Token tk : tks.toIterable()) {
			if(unaryMode) {
				if(isUnary(tk)) {
					unaryOps.add(tk);
					continue;
				}

				Token unaryOp = unaryOps.pop();

				Token.Type unaryType = unaryOp.type;

				if(unaryAdjectives.contains(unaryType)) {
					if(isOp(tk)) {
						Errors.inst.printError(EK_SHUNT_NOTADV, unaryOp.toString(), tk.toString());
						return false;
					}

					Token newTok = new Token(TAGOPR);
					
					if(tk.type == TAGOP) {
						newTok.tokenValues = tk.tokenValues;
					} else {
						newTok.tokenValues = new FunctionalList<>();
					}

					newTok.tokenValues.add(unaryOp);
					opStack.push(newTok);
				} else if(unaryAdverbs.contains(unaryType)) {
					// @TODO finish implementing unary operators
					// 		 this will require adding a 'backfeed' to the shunter to catch
					//		 tokens we missed while parsing unary operators
				}
			}

			if(isUnary(tk)) {
				unaryMode = true;

				unaryOps.add(tk);
				continue;
			} else if(isOp(tk)) {
				while(!opStack.isEmpty() && isHigherPrec(tk, opStack.peek())) {
					currReturned.addLast(opStack.pop());
				}

				opStack.push(tk);
			} else if(tk.type == OPAREN || tk.type == OBRACE) {
				opStack.push(tk);

				if(tk.type == OBRACE) currReturned.addLast(tk);
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
					Errors.inst.printError(EK_SHUNT_NOGROUP, tk.toString(), matching.toString());
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

		// Flush leftover operators
		while(!opStack.isEmpty()) {
			currReturned.addLast(opStack.pop());
		}

		for(Token tk : currReturned) {
			returned.add(tk);
		}

		return true;
	}

	private boolean isHigherPrec(Token lft, Token rght) {
		Token.Type left  = lft.type;
		Token.Type right = rght.type;

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
