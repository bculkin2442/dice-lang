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
	/*
	 * @NOTE
	 * 	Why does this method return an array, and not the list of
	 * 	tokens?
	 */
	/**
	 * Convert a infix series of tokens to a postfix series of tokens.
	 *
	 * @param infixTokens
	 *                The tokens in infix order.
	 *
	 * @return The tokens in postfix order.
	 */
	public static Token[] shuntTokens(final Token[] infixTokens) {
		/* The returned tokens. */
		final List<Token> postfixTokens = new ArrayList<>(infixTokens.length);

		/* The current stack of operators. */
		final Deque<Token> opStack = new LinkedList<>();

		/* Shunt each token. */
		for (final Token tok : infixTokens) {
			/* Handle operators. */
			if (tok.typ.isOperator) {
				Token curOp = opStack.peek();

				/*
				 * Check if an operator is higher priority,
				 * respecting their left associativity.
				 *
				 * @NOTE
				 * 	Should this be factored out into a
				 * 	method?
				 */
				int leftPriority = tok.typ.operatorPriority;
				int rightPriority;

				if (curOp == null) {
					rightPriority = 0;
				} else {
					rightPriority = curOp.typ.operatorPriority;
				}

				boolean isHigherPrec = leftPriority >= rightPriority;

				/*
				 * Pop all operators that are lower precedence
				 * than us.
				 */
				while (!opStack.isEmpty() && isHigherPrec) {
					postfixTokens.add(opStack.pop());
					curOp = opStack.peek();

					leftPriority = tok.typ.operatorPriority;
					if (curOp == null) {
						rightPriority = 0;
					} else {
						rightPriority = curOp.typ.operatorPriority;
					}

					isHigherPrec = leftPriority >= rightPriority;
				}

				opStack.push(tok);
			} else if (tok.typ == TokenType.OPAREN) {
				opStack.push(tok);
			} else if (tok.typ == TokenType.CPAREN) {
				Token curOp = opStack.peek();

				/*
				 * Pop things until we find the matching
				 * parenthesis.
				 */
				while (curOp.typ != TokenType.OPAREN) {
					final Token tk = opStack.pop();
					postfixTokens.add(tk);
					curOp = opStack.peek();
				}

				if (!opStack.isEmpty()) {
					opStack.pop();
				}
			} else {
				postfixTokens.add(tok);
			}
		}

		/*
		 * Flush remaining operators.
		 */
		while (!opStack.isEmpty()) {
			postfixTokens.add(opStack.pop());
		}

		return postfixTokens.toArray(new Token[0]);
	}
}
