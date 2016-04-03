package bjc.dicelang.ast;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

import bjc.dicelang.ComplexDice;
import bjc.dicelang.CompoundDice;
import bjc.dicelang.IDiceExpression;

import bjc.utils.data.Pair;
import bjc.utils.parserutils.AST;

/**
 * An implementation of {@link IDiceExpression} backed by an AST of
 * {@link IDiceASTNode}s
 * 
 * @author ben
 *
 */
public class DiceASTExpression implements IDiceExpression {

	/**
	 * Build the map of operations to use when collapsing the AST
	 * 
	 * @param enviroment
	 *            The enviroment to evaluate bindings and such against
	 * @return The operations to use when collapsing the AST
	 */
	private static Map<IDiceASTNode, IOperatorCollapser>
			buildOperations(Map<String, DiceASTExpression> enviroment) {
		Map<IDiceASTNode, IOperatorCollapser> operatorCollapsers =
				new HashMap<>();

		operatorCollapsers.put(OperatorDiceNode.ADD,
				(leftNode, rightNode) -> {
					return leftNode.merge((leftValue, leftAST) -> {
						return rightNode.merge((rightValue, rightAST) -> {
							return new Pair<>(leftValue + rightValue,
									new AST<>(OperatorDiceNode.ADD,
											leftAST, rightAST));
						});
					});

				});
		operatorCollapsers.put(OperatorDiceNode.SUBTRACT,
				(left, right) -> {
					return left.merge(
							(lval, last) -> right.merge((rval, rast) -> {
								return new Pair<>(lval - rval,
										new AST<>(
												OperatorDiceNode.SUBTRACT,
												last, rast));
							}));

				});
		operatorCollapsers.put(OperatorDiceNode.MULTIPLY,
				(left, right) -> {
					return left.merge(
							(lval, last) -> right.merge((rval, rast) -> {
								return new Pair<>(lval * rval,
										new AST<>(
												OperatorDiceNode.MULTIPLY,
												last, rast));
							}));

				});
		operatorCollapsers.put(OperatorDiceNode.DIVIDE, (left, right) -> {
			return left.merge((lval, last) -> right.merge((rval, rast) -> {
				return new Pair<>(lval / rval,
						new AST<>(OperatorDiceNode.DIVIDE, last, rast));
			}));
		});

		operatorCollapsers.put(OperatorDiceNode.ASSIGN, (left, right) -> {
			return left.merge((lval, last) -> right.merge((rval, rast) -> {
				String nam = last.collapse((nod) -> {
					return ((VariableDiceNode) nod).getVariable();
				}, (v) -> (lv, rv) -> null, (r) -> r);

				enviroment.put(nam,
						new DiceASTExpression(rast, enviroment));

				return new Pair<>(rval,
						new AST<>(OperatorDiceNode.ASSIGN, last, rast));
			}));
		});

		operatorCollapsers.put(OperatorDiceNode.COMPOUND,
				(left, right) -> {
					return left.merge(
							(lval, last) -> right.merge((rval, rast) -> {
								int ival = Integer
										.parseInt(Integer.toString(lval)
												+ Integer.toString(rval));

								return new Pair<>(ival,
										new AST<>(
												OperatorDiceNode.COMPOUND,
												last, rast));
							}));
				});
		operatorCollapsers.put(OperatorDiceNode.GROUP, (left, right) -> {
			return left.merge((lval, last) -> right.merge((rval, rast) -> {

				return new Pair<>(new ComplexDice(lval, rval).roll(),
						new AST<>(OperatorDiceNode.GROUP, last, rast));
			}));
		});

		return operatorCollapsers;
	}

	/**
	 * The AST this expression will evaluate
	 */
	private AST<IDiceASTNode>				ast;

	/**
	 * The enviroment to evaluate bindings and such against
	 */
	private Map<String, DiceASTExpression>	env;

	/**
	 * Create a new dice expression backed by an AST
	 * 
	 * @param ast
	 *            The AST backing this expression
	 * @param env
	 *            The enviroment to evaluate bindings against
	 */
	public DiceASTExpression(AST<IDiceASTNode> ast,
			Map<String, DiceASTExpression> env) {
		this.ast = ast;
		this.env = env;
	}

	/**
	 * Expand a leaf AST token into a pair for evaluation
	 * 
	 * @param tokn
	 *            The token to evaluate
	 * @return A pair consisting of the token's value and the AST it
	 *         represents
	 */
	private Pair<Integer, AST<IDiceASTNode>> evalLeaf(IDiceASTNode tokn) {
		if (tokn instanceof VariableDiceNode) {
			String varName = ((VariableDiceNode) tokn).getVariable();

			if (env.containsKey(varName)) {
				return new Pair<>(env.get(varName).roll(),
						new AST<>(tokn));
			} else {
				// Handle special case for defining variables
				return new Pair<>(0, new AST<>(tokn));
			}
		} else {
			LiteralDiceNode lnod = (LiteralDiceNode) tokn;
			String dat = lnod.getData();

			if (StringUtils.countMatches(dat, 'c') == 1
					&& !dat.equalsIgnoreCase("c")) {
				String[] strangs = dat.split("c");
				return new Pair<>(new CompoundDice(strangs).roll(),
						new AST<>(tokn));
			} else if (StringUtils.countMatches(dat, 'd') == 1
					&& !dat.equalsIgnoreCase("d")) {
				/*
				 * Handle dice groups
				 */
				return new Pair<>(ComplexDice.fromString(dat).roll(),
						new AST<>(tokn));
			} else {
				return new Pair<>(Integer.parseInt(dat), new AST<>(tokn));
			}
		}
	}

	/**
	 * Get the AST bound to this expression
	 * 
	 * @return the ast
	 */
	public AST<IDiceASTNode> getAst() {
		return ast;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see bjc.utils.dice.IDiceExpression#roll()
	 */
	@Override
	public int roll() {
		Map<IDiceASTNode, IOperatorCollapser> operations =
				buildOperations(env);

		return ast.collapse(this::evalLeaf, operations::get,
				(r) -> r.merge((left, right) -> left));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ast.toString();
	}
}
