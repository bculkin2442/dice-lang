package bjc.dicelang.v2;

import bjc.utils.funcdata.FunctionalList;
import bjc.utils.funcdata.FunctionalMap;
import bjc.utils.funcdata.IList;
import bjc.utils.funcdata.IMap;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

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

	private final int MATH_PREC	= 20;
	private final int DICE_PREC	= 10;
	private final int EXPR_PREC	= 0;

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

		boolean unaryMode          = false;
		Deque<Token> unaryOps = new LinkedList<>();

		for(Token tk : tks.toIterable()) {
			if(unaryMode) {
				// @TODO finish unary mode
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
						System.out.printf("\tError: Unary operator %s is an"
								+ " adjective, not an adverb (can't be applied"
								+ " to operator %s)\n", currOperator, tk );
						
						return false;
					}

					returned.add(tk);
					returned.add(unaryOps.pop());
				} else if (unaryAdverbs.contains(currOperator.type)) {
					if(opStack.size() < 1) {
						System.out.printf("\tError: Unary operators %s is an adverb,"
								+ " but there is no operator to apply it to\n");
					}

					Token currOperand = opStack.peek();

					boolean isOp = ops.containsKey(currOperand.type) 
						|| unaryAdverbs.contains(currOperand.type)
						|| unaryAdjectives.contains(currOperand.type);

					if(!isOp) {
						System.out.printf("\tError: Unary operator %s is an adverb,"
								+ " not an  adjective (can't be applied to operand %s)\n",
								currOperator, tk);
						
						return false;
					}

					returned.add(tk);
					returned.add(unaryOps.pop());
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
						returned.add(opStack.pop());
					}

					opStack.push(tk);
				} else if(tk.type == OPAREN) {
					opStack.push(tk);
				} else if(tk.type == CPAREN) {
					Token currTk = opStack.peek();

					while(currTk.type != OPAREN && currTk.intValue != tk.intValue) {
						if(opStack.isEmpty()) {
							System.out.printf("\tError: Could not find matching parenthesis"
								+ " with matching level %d\n", tk.intValue);

							return false;
						}

						returned.add(opStack.pop());
					}
				} else {
					returned.add(tk);
				}
			}
		}

		// Flush leftover operators
		while(!opStack.isEmpty()) {
			returned.add(opStack.pop());
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
