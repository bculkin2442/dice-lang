package bjc.dicelang.v2;

import bjc.utils.funcdata.FunctionalList;
import bjc.utils.funcdata.FunctionalMap;
import bjc.utils.funcdata.IList;
import bjc.utils.funcdata.IMap;

import java.util.HashSet;
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

		ops.put(LET,  0 + EXPR_PREC);
		ops.put(BIND, 1 + EXPR_PREC);
	}

	public IList<Token> shuntTokens(IList<Token> tks) {
		IList<Token> returned = new FunctionalList<>();

		for(Token tk : tks.toIterable()) {

		}

		return returned;
	}
}
