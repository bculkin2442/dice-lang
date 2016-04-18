package bjc.dicelang.ast.optimization;

import bjc.dicelang.ast.nodes.DiceASTType;
import bjc.dicelang.ast.nodes.IDiceASTNode;
import bjc.dicelang.ast.nodes.OperatorDiceNode;
import bjc.utils.data.IHolder;
import bjc.utils.data.Identity;
import bjc.utils.funcdata.ITree;
import bjc.utils.funcdata.TopDownTransformResult;
import bjc.utils.funcdata.Tree;

/**
 * Condenses chained operations into a single level
 * 
 * @author ben
 *
 */
public class OperationCondenser {
	/**
	 * Condense chained similiar operations into a single level
	 * 
	 * @param ast
	 *            The AST to condense
	 * @return The condensed AST
	 */
	public static ITree<IDiceASTNode> condense(ITree<IDiceASTNode> ast) {
		return ast.topDownTransform(OperationCondenser::pickNode,
				OperationCondenser::doCondense);
	}

	private static TopDownTransformResult pickNode(IDiceASTNode node) {
		switch (node.getType()) {
			case LITERAL:
				return TopDownTransformResult.SKIP;
			case OPERATOR:
				return pickOperator((OperatorDiceNode) node);
			case VARIABLE:
				return TopDownTransformResult.SKIP;
			default:
				throw new UnsupportedOperationException(
						"Attempted to traverse unknown node type " + node);
		}
	}

	private static TopDownTransformResult
			pickOperator(OperatorDiceNode node) {
		switch (node) {
			case ADD:
			case MULTIPLY:
			case SUBTRACT:
			case DIVIDE:
			case COMPOUND:
				return TopDownTransformResult.PUSHDOWN;
			case ASSIGN:
			case GROUP:
			case LET:
				return TopDownTransformResult.PASSTHROUGH;
			default:
				throw new UnsupportedOperationException(
						"Attempted to traverse unknown operator " + node);
		}
	}

	private static ITree<IDiceASTNode>
			doCondense(ITree<IDiceASTNode> ast) {
		OperatorDiceNode operation =
				ast.transformHead((node) -> (OperatorDiceNode) node);

		IHolder<Boolean> canCondense = new Identity<>(true);

		ast.doForChildren((child) -> {
			if (canCondense.getValue()) {
				canCondense.replace(child.transformHead((node) -> {
					if (node.getType() == DiceASTType.OPERATOR) {
						if (operation.equals(node)) {
							return true;
						}

						return false;
					}

					return true;
				}));
			}
		});

		if (!canCondense.getValue()) {
			return ast;
		}

		ITree<IDiceASTNode> condensedAST = new Tree<>(operation);

		ast.doForChildren((child) -> {
			if (child.getHead().getType() == DiceASTType.OPERATOR) {
				child.doForChildren((subChild) -> {
					condensedAST.addChild(subChild);
				});
			} else {
				condensedAST.addChild(child);
			}
		});

		return condensedAST;
	}
}
