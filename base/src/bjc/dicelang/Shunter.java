package bjc.dicelang;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import bjc.utils.funcdata.FunctionalList;
import bjc.utils.funcdata.FunctionalMap;
import bjc.utils.funcdata.IList;
import bjc.utils.funcdata.IMap;

import static bjc.dicelang.Errors.ErrorKey.EK_SHUNT_INVSEP;
import static bjc.dicelang.Errors.ErrorKey.EK_SHUNT_NOGROUP;
import static bjc.dicelang.Errors.ErrorKey.EK_SHUNT_NOTADJ;
import static bjc.dicelang.Errors.ErrorKey.EK_SHUNT_NOTADV;
import static bjc.dicelang.Errors.ErrorKey.EK_SHUNT_NOTASSOC;
import static bjc.dicelang.Token.Type.*;

/**
 * Shunt a set of infix tokens to postfix tokens.
 *
 * @author EVE
 *
 */
public class Shunter {
	/* The binary operators and their priorities. */
	IMap<Token.Type, Integer> ops;

	/* Operators that are right-associative */
	Set<Token.Type> rightAssoc;

	/* Operators that aren't associative */
	Set<Token.Type> notAssoc;

	/*
	 * Unary operators that can only be applied to non-operator tokens and yield
	 * operator tokens.
	 */
	Set<Token.Type> unaryAdjectives;

	/*
	 * Unary operators that can only be applied to operator tokens and yield
	 * operator tokens.
	 */
	Set<Token.Type> unaryAdverbs;

	/*
	 * Unary operators that can only be applied to operator tokens and yield data
	 * tokens
	 */
	Set<Token.Type> unaryGerunds;

	/** Precedence for math operators. */
	public final int MATH_PREC = 30;
	/** Precedence for dice operators. */
	public final int DICE_PREC = 20;
	/** Precedence for string operators. */
	public final int STR_PREC = 10;
	/** Precedence for expression operators. */
	public final int EXPR_PREC = 0;

	/** Create a new shunter. */
	public Shunter() {
		/* Create op map. */
		ops = new FunctionalMap<>();

		/* Create association maps. */
		rightAssoc = new HashSet<>();
		notAssoc = new HashSet<>();

		/* Create unary maps. */
		unaryAdjectives = new HashSet<>();
		unaryAdverbs = new HashSet<>();
		unaryGerunds = new HashSet<>();

		/* Set up unary adverbs. */
		unaryAdverbs.add(COERCE);

		/* Setup operators. */
		/* Math operators. */
		ops.put(ADD, 0 + MATH_PREC);
		ops.put(SUBTRACT, 0 + MATH_PREC);

		ops.put(MULTIPLY, 1 + MATH_PREC);
		ops.put(IDIVIDE, 1 + MATH_PREC);
		ops.put(DIVIDE, 1 + MATH_PREC);

		/* Dice operators. */
		ops.put(DICEGROUP, 0 + DICE_PREC);

		ops.put(DICECONCAT, 1 + DICE_PREC);

		ops.put(DICELIST, 2 + DICE_PREC);

		/* String operators. */
		ops.put(STRCAT, 0 + STR_PREC);

		ops.put(STRREP, 1 + STR_PREC);

		/* Expression operators. */
		ops.put(LET, 0 + EXPR_PREC);

		ops.put(BIND, 1 + EXPR_PREC);
	}

	/**
	 * Shunt a set of tokens from infix to postfix.
	 *
	 * @param tks
	 *            The tokens to input.
	 *
	 * @param returned
	 *            The postfix tokens.
	 *
	 * @return Whether or not the shunt succeeded.
	 */
	public boolean shuntTokens(final IList<Token> tks, final IList<Token> returned) {
		/* Operator stack for normal and unary operators. */
		final Deque<Token> opStack = new LinkedList<>();
		final Deque<Token> unaryOps = new LinkedList<>();

		/* Currently returned lists. */
		final Deque<Token> currReturned = new LinkedList<>();

		/* Tokens to feed ahead of the current one. */
		final Deque<Token> feed = new LinkedList<>();

		for (final Token tk : tks.toIterable()) {
			boolean succ;

			/* Drain the feed queue. */
			while (feed.size() != 0) {
				succ = shuntToken(feed.poll(), opStack, unaryOps, currReturned, feed);

				if (!succ) {
					return false;
				}
			}

			succ = shuntToken(tk, opStack, unaryOps, currReturned, feed);

			if (!succ) {
				return false;
			}
		}

		/* Flush leftover operators. */
		while (!opStack.isEmpty()) {
			currReturned.addLast(opStack.pop());
		}

		/* Add the tokens to the returned list. */
		for (final Token tk : currReturned) {
			returned.add(tk);
		}

		return true;
	}

	/* Shunt a token. */
	private boolean shuntToken(final Token tk, final Deque<Token> opStack, final Deque<Token> unaryStack,
			final Deque<Token> currReturned, final Deque<Token> feed) {
		/* Handle unary operators. */
		if (unaryStack.size() != 0) {
			if (isUnary(tk)) {
				unaryStack.add(tk);
				return true;
			}

			final Token unaryOp = unaryStack.pop();

			final Token.Type unaryType = unaryOp.type;

			if (unaryAdjectives.contains(unaryType)) {
				/*
				 * Handle unary adjectives that take a non-operator.
				 */
				if (isOp(tk)) {
					Errors.inst.printError(EK_SHUNT_NOTADV, unaryOp.toString(), tk.toString());
					return false;
				}

				final Token newTok = new Token(TAGOPR);

				if (tk.type == TAGOP) {
					newTok.tokenValues = tk.tokenValues;
				} else {
					newTok.tokenValues = new FunctionalList<>(tk);
				}

				newTok.tokenValues.add(unaryOp);
				opStack.push(newTok);

				return true;
			} else if (unaryAdverbs.contains(unaryType)) {
				/* Handle unary adverbs that take an operator. */
				if (!isOp(tk)) {
					Errors.inst.printError(EK_SHUNT_NOTADJ, unaryOp.toString(), tk.toString());
					return false;
				}

				final Token newTok = new Token(TAGOPR);

				if (tk.type == TAGOP) {
					newTok.tokenValues = tk.tokenValues;
				} else {
					newTok.tokenValues = new FunctionalList<>(tk);
				}

				newTok.tokenValues.add(unaryOp);
				opStack.push(newTok);

				return true;
			}
		}

		if (isUnary(tk)) {
			unaryStack.add(tk);
			return true;
		} else if (isOp(tk)) {
			/* Drain higher precedence operators. */
			while (!opStack.isEmpty() && isHigherPrec(tk, opStack.peek())) {
				final Token newOp = opStack.pop();

				if (tk.type == newOp.type && notAssoc.contains(tk.type)) {
					Errors.inst.printError(EK_SHUNT_NOTASSOC, tk.type.toString());
				}

				currReturned.addLast(newOp);
			}

			opStack.push(tk);
		} else if (tk.type == OPAREN || tk.type == OBRACE) {
			opStack.push(tk);

			if (tk.type == OBRACE) {
				currReturned.addLast(tk);
			}
		} else if (tk.type == CPAREN || tk.type == CBRACE) {
			/* Handle closing delimiter. */
			Token matching = null;

			switch (tk.type) {
			case CPAREN:
				matching = new Token(OPAREN, tk.intValue);
				break;
			case CBRACE:
				matching = new Token(OBRACE, tk.intValue);
				break;
			default:
				Errors.inst.printError(EK_SHUNT_NOGROUP);
				return false;
			}

			if (!opStack.contains(matching)) {
				Errors.inst.printError(EK_SHUNT_NOGROUP, tk.toString(), matching.toString());
				return false;
			}

			while (!opStack.peek().equals(matching)) {
				currReturned.addLast(opStack.pop());
			}

			if (tk.type == CBRACE) {
				currReturned.addLast(tk);
			}

			opStack.pop();
		} else if (tk.type == GROUPSEP) {
			/* Add a grouped token. */
			final IList<Token> group = new FunctionalList<>();

			while (currReturned.size() != 0 && !currReturned.peek().isGrouper()) {
				group.add(currReturned.pop());
			}

			while (opStack.size() != 0 && !opStack.peek().isGrouper()) {
				group.add(opStack.pop());
			}

			if (currReturned.size() == 0) {
				Errors.inst.printError(EK_SHUNT_INVSEP);
				return false;
			}

			currReturned.addLast(new Token(TOKGROUP, group));
		} else {
			currReturned.addLast(tk);
		}

		return true;
	}

	/* Check if an operator has higher precedence. */
	private boolean isHigherPrec(final Token lft, final Token rght) {
		final Token.Type left = lft.type;
		final Token.Type right = rght.type;

		boolean exists = ops.containsKey(right);

		if (rght.type == TAGOPR) {
			exists = true;
		}

		/* If it doesn't, the left is higher precedence. */
		if (!exists) {
			return false;
		}

		int rightPrecedence;
		int leftPrecedence;

		if (rght.type == TAGOPR) {
			rightPrecedence = (int) rght.intValue;
		} else {
			rightPrecedence = ops.get(right);
		}

		if (lft.type == TAGOPR) {
			leftPrecedence = (int) lft.intValue;
		} else {
			leftPrecedence = ops.get(left);
		}

		if (rightAssoc.contains(left)) {
			return rightPrecedence > leftPrecedence;
		}

		return rightPrecedence >= leftPrecedence;
	}

	/* Check if something is an operator. */
	private boolean isOp(final Token tk) {
		final Token.Type ty = tk.type;

		if (ops.containsKey(ty)) {
			return true;
		}

		if (unaryAdjectives.contains(ty)) {
			return true;
		}

		if (unaryAdverbs.contains(ty)) {
			return true;
		}

		if (unaryGerunds.contains(ty)) {
			return true;
		}

		if (ty == TAGOPR) {
			return true;
		}

		return false;
	}

	/* Check if something is a unary operator. */
	private boolean isUnary(final Token tk) {
		final Token.Type ty = tk.type;

		if (unaryAdjectives.contains(ty)) {
			return true;
		}

		if (unaryAdverbs.contains(ty)) {
			return true;
		}

		if (unaryGerunds.contains(ty)) {
			return true;
		}

		return false;
	}
}
