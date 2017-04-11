package bjc.dicelang.expr;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * Converts a infix series of tokens into a prefix series of tokens.
 *
 * @author Ben Culkin
 */
public class Shunter {
	/**
	 * Convert a infix series of tokens to a postfix series of tokens.
	 *
	 * @param infixTokens
	 *                The tokens in infix order.
	 *
	 * @return The tokens in postfix order.
	 */
	public static Token[] shuntTokens(Token[] infixTokens) {
		List<Token> postfixTokens = new ArrayList<>(infixTokens.length);

		Deque<Token> opStack = new LinkedList<>();

		/*
		 * Shunt each token.
		 */
		for(Token tok : infixTokens) {
			/*
			 * Handle operators.
			 */
			if(tok.type.isOperator) {
				Token curOp = opStack.peek();

				/*
				 * Check if an operator is higher priority,
				 * respecting their left associativity.
				 */
				int leftPriority = tok.type.operatorPriority;

				int rightPriority;
				if(curOp == null) {
					rightPriority = 0;
				} else {
					rightPriority = curOp.type.operatorPriority;
				}

				boolean isHigherPrec = leftPriority >= rightPriority;

				/*
				 * Pop all operators that are lower precedence
				 * than us.
				 */
				while(!opStack.isEmpty() && isHigherPrec) {
					postfixTokens.add(opStack.pop());

					curOp = opStack.peek();

					leftPriority = tok.type.operatorPriority;

					if(curOp == null) {
						rightPriority = 0;
					} else {
						rightPriority = curOp.type.operatorPriority;
					}

					isHigherPrec = leftPriority >= rightPriority;
				}

				opStack.push(tok);
			} else if(tok.type == TokenType.OPAREN) {
				opStack.push(tok);
			} else if(tok.type == TokenType.CPAREN) {
				Token curOp = opStack.peek();

				/*
				 * Pop things until we find the matching
				 * parenthesis.
				 */
				while(curOp.type != TokenType.OPAREN) {
					Token tk = opStack.pop();

					postfixTokens.add(tk);

					curOp = opStack.peek();
				}

				if(!opStack.isEmpty()) {
					opStack.pop();
				}
			} else {
				postfixTokens.add(tok);
			}
		}

		/*
		 * Flush remaining operators.
		 */
		while(!opStack.isEmpty()) {
			postfixTokens.add(opStack.pop());
		}

		return postfixTokens.toArray(new Token[0]);
	}
}