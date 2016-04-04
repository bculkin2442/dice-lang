package bjc.dicelang.ast;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import bjc.dicelang.BindingDiceExpression;
import bjc.dicelang.ComplexDice;
import bjc.dicelang.CompoundDice;
import bjc.dicelang.OperatorDiceExpression;
import bjc.dicelang.DiceExpressionType;
import bjc.dicelang.IDiceExpression;
import bjc.dicelang.ReferenceDiceExpression;
import bjc.dicelang.ScalarDie;
import bjc.dicelang.ast.nodes.DiceASTType;
import bjc.dicelang.ast.nodes.IDiceASTNode;
import bjc.dicelang.ast.nodes.LiteralDiceNode;
import bjc.dicelang.ast.nodes.OperatorDiceNode;
import bjc.dicelang.ast.nodes.VariableDiceNode;
import bjc.utils.parserutils.AST;

/**
 * Flatten an {@link AST} of {@link IDiceASTNode} into a
 * {@link IDiceExpression}
 * 
 * @author ben
 *
 */
public class DiceASTFlattener {
	private static final class NodeCollapser
			implements Function<IDiceASTNode, IDiceExpression> {
		private Map<String, IDiceExpression> enviroment;

		public NodeCollapser(Map<String, IDiceExpression> env) {
			this.enviroment = env;
		}

		@Override
		public IDiceExpression apply(IDiceASTNode nod) {
			if (nod.getType() == DiceASTType.LITERAL) {
				return expFromLiteral((LiteralDiceNode) nod);
			} else if (nod.getType() == DiceASTType.VARIABLE) {
				String varName = ((VariableDiceNode) nod).getVariable();

				return new ReferenceDiceExpression(varName, enviroment);
			} else {
				throw new UnsupportedOperationException(
						"Attempted to flatten something that can't be"
								+ " flattened. The culprit is " + nod);
			}
		}

		/**
		 * Create a dice expression from a literal token
		 * 
		 * @param tok
		 *            The token to convert to an expression
		 * @return The dice expression represented by the token
		 */
		private static IDiceExpression
				expFromLiteral(LiteralDiceNode tok) {
			String data = tok.getData();

			if (data.equals("")) {
				throw new UnsupportedOperationException(
						"Can't convert a blank token into a literal");
			}

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
	}

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
			return new OperatorDiceExpression(right, left,
					DiceExpressionType.ADD);
		});
		opCollapsers.put(OperatorDiceNode.SUBTRACT, (left, right) -> {
			return new OperatorDiceExpression(right, left,
					DiceExpressionType.SUBTRACT);
		});
		opCollapsers.put(OperatorDiceNode.MULTIPLY, (left, right) -> {
			return new OperatorDiceExpression(right, left,
					DiceExpressionType.MULTIPLY);
		});
		opCollapsers.put(OperatorDiceNode.DIVIDE, (left, right) -> {
			return new OperatorDiceExpression(right, left,
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

		return ast.collapse(new NodeCollapser(env), opCollapsers::get,
				(r) -> r);
	}
}
