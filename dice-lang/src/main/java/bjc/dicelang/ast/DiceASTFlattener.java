package bjc.dicelang.ast;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BinaryOperator;

import org.apache.commons.lang3.StringUtils;

import bjc.dicelang.BindingDiceExpression;
import bjc.dicelang.ComplexDice;
import bjc.dicelang.CompoundDice;
import bjc.dicelang.CompoundDiceExpression;
import bjc.dicelang.DiceExpressionType;
import bjc.dicelang.IDiceExpression;
import bjc.dicelang.ReferenceDiceExpression;
import bjc.dicelang.ScalarDie;
import bjc.utils.parserutils.AST;

/**
 * Flatten an {@link AST} of {@link IDiceASTNode} into a
 * {@link IDiceExpression}
 * 
 * @author ben
 *
 */
public class DiceASTFlattener {
	/**
	 * Build the operations to use for tree flattening
	 * 
	 * @param env
	 *            The enviroment the tree will be flattened against
	 * @return The operations needed for tree flattening
	 */
	private static Map<IDiceASTNode, BinaryOperator<IDiceExpression>>
			buildOperations(Map<String, IDiceExpression> env) {
		Map<IDiceASTNode, BinaryOperator<IDiceExpression>> opCollapsers =
				new HashMap<>();
		opCollapsers.put(OperatorDiceNode.ADD, (left, right) -> {
			return new CompoundDiceExpression(right, left,
					DiceExpressionType.ADD);
		});
		opCollapsers.put(OperatorDiceNode.SUBTRACT, (left, right) -> {
			return new CompoundDiceExpression(right, left,
					DiceExpressionType.SUBTRACT);
		});
		opCollapsers.put(OperatorDiceNode.MULTIPLY, (left, right) -> {
			return new CompoundDiceExpression(right, left,
					DiceExpressionType.MULTIPLY);
		});
		opCollapsers.put(OperatorDiceNode.DIVIDE, (left, right) -> {
			return new CompoundDiceExpression(right, left,
					DiceExpressionType.DIVIDE);
		});
		opCollapsers.put(OperatorDiceNode.ASSIGN, (left, right) -> {
			return new BindingDiceExpression(left, right, env);
		});
		opCollapsers.put(OperatorDiceNode.COMPOUND, (left, right) -> {
			return new CompoundDice(left, right);
		});
		opCollapsers.put(OperatorDiceNode.GROUP, (left, right) -> {
			return new ComplexDice(left, right);
		});

		return opCollapsers;
	}

	/**
	 * Create a dice expression from a literal token
	 * 
	 * @param tok
	 *            The token to convert to an expression
	 * @return The dice expression represented by the token
	 */
	private static IDiceExpression expFromLiteral(LiteralDiceNode tok) {
		String data = tok.getData();

		if (StringUtils.countMatches(data, 'c') == 1
				&& !data.equalsIgnoreCase("c")) {
			String[] strangs = data.split("c");

			return new CompoundDice(ComplexDice.fromString(strangs[0]),
					ComplexDice.fromString(strangs[1]));
		} else if (StringUtils.countMatches(data, 'd') == 1
				&& !data.equalsIgnoreCase("d")) {
			return ComplexDice.fromString(data);
		} else {
			return new ScalarDie(Integer.parseInt(data));
		}
	}

	/**
	 * Flatten a AST into a dice expression
	 * 
	 * @param ast
	 *            The AST to flatten
	 * @param env
	 *            The enviroment to flatten against
	 * @return The AST, flattened into a dice expression
	 */
	public static IDiceExpression flatten(AST<IDiceASTNode> ast,
			Map<String, IDiceExpression> env) {
		Map<IDiceASTNode, BinaryOperator<IDiceExpression>> opCollapsers =
				buildOperations(env);

		return ast.collapse((nod) -> {
			if (nod instanceof LiteralDiceNode) {
				return expFromLiteral((LiteralDiceNode) nod);
			} else {
				return new ReferenceDiceExpression(
						((VariableDiceNode) nod).getVariable(), env);
			}
		} , opCollapsers::get, (r) -> r);
	}
}
