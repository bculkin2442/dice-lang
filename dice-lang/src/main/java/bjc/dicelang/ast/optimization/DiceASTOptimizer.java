package bjc.dicelang.ast.optimization;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

import bjc.dicelang.IDiceExpression;
import bjc.dicelang.ast.nodes.DiceASTType;
import bjc.dicelang.ast.nodes.IDiceASTNode;
import bjc.dicelang.ast.nodes.LiteralDiceNode;
import bjc.dicelang.ast.nodes.OperatorDiceNode;

import static bjc.dicelang.ast.nodes.DiceASTType.*;

import bjc.utils.parserutils.AST;

/**
 * Optimize an AST
 * 
 * @author ben
 *
 */
public class DiceASTOptimizer {
	private static final class NestedArithmeticOperationCollapser
			implements BinaryOperator<AST<IDiceASTNode>> {
		private IDiceASTNode							type;
		private BiFunction<Integer, Integer, Integer>	valueCollapser;

		public NestedArithmeticOperationCollapser(IDiceASTNode type,
				BiFunction<Integer, Integer, Integer> valueCollapser) {
			this.type = type;
			this.valueCollapser = valueCollapser;
		}

		@Override
		public AST<IDiceASTNode> apply(AST<IDiceASTNode> leftAST,
				AST<IDiceASTNode> rightAST) {
			AST<IDiceASTNode> rightBranchOfLeftAST =
					leftAST.applyToRight((rightSideAST) -> rightSideAST);
			AST<IDiceASTNode> leftBranchOfLeftAST =
					leftAST.applyToRight((rightSideAST) -> rightSideAST);

			boolean leftContainsNestedConstant = DiceASTOptimizer
					.checkNodeType(rightBranchOfLeftAST, LITERAL)
					&& DiceASTOptimizer.isNodeConstant(leftAST);

			boolean isRightConstant =
					DiceASTOptimizer.checkNodeType(rightAST, LITERAL)
							&& DiceASTOptimizer.isNodeConstant(leftAST);

			if (leftContainsNestedConstant && isRightConstant) {
				int combinedValue = valueCollapser.apply(
						getNodeValue(rightBranchOfLeftAST),
						getNodeValue(rightAST));

				AST<IDiceASTNode> newRightBranch =
						new AST<>(new LiteralDiceNode(combinedValue));

				return new AST<>(type, leftBranchOfLeftAST,
						newRightBranch);
			}

			return new AST<>(type, leftAST, rightAST);
		}
	}

	private static final class ArithmeticOperationCollapser
			implements BinaryOperator<AST<IDiceASTNode>> {
		private IDiceASTNode							type;
		private BiFunction<Integer, Integer, Integer>	valueCollapser;
		private boolean									doSwap;

		public ArithmeticOperationCollapser(IDiceASTNode type,
				BiFunction<Integer, Integer, Integer> valueCollapser,
				boolean doSwap) {
			this.type = type;
			this.valueCollapser = valueCollapser;
			this.doSwap = doSwap;
		}

		@Override
		public AST<IDiceASTNode> apply(AST<IDiceASTNode> leftAST,
				AST<IDiceASTNode> rightAST) {
			boolean isLeftConstant =
					DiceASTOptimizer.checkNodeType(leftAST, LITERAL)
							&& DiceASTOptimizer.isNodeConstant(leftAST);

			boolean isRightConstant =
					DiceASTOptimizer.checkNodeType(rightAST, LITERAL)
							&& DiceASTOptimizer.isNodeConstant(leftAST);

			if (isLeftConstant) {
				if (isRightConstant) {
					int combinedValue = valueCollapser.apply(
							getNodeValue(leftAST), getNodeValue(rightAST));

					return new AST<>(new LiteralDiceNode(combinedValue));
				}

				if (doSwap) {
					return new AST<>(type, rightAST, leftAST);
				}
			}

			return new AST<>(type, leftAST, rightAST);
		}
	}

	private static Map<IDiceASTNode, BinaryOperator<AST<IDiceASTNode>>>
			buildConstantCollapsers() {
		Map<IDiceASTNode, BinaryOperator<AST<IDiceASTNode>>> operatorCollapsers =
				new HashMap<>();

		operatorCollapsers.put(OperatorDiceNode.ADD,
				new ArithmeticOperationCollapser(OperatorDiceNode.ADD,
						(leftVal, rightVal) -> leftVal + rightVal, true));

		operatorCollapsers.put(OperatorDiceNode.MULTIPLY,
				new ArithmeticOperationCollapser(OperatorDiceNode.MULTIPLY,
						(leftVal, rightVal) -> leftVal * rightVal, true));

		operatorCollapsers.put(OperatorDiceNode.SUBTRACT,
				new ArithmeticOperationCollapser(OperatorDiceNode.SUBTRACT,
						(leftVal, rightVal) -> leftVal - rightVal, false));

		operatorCollapsers.put(OperatorDiceNode.DIVIDE,
				new ArithmeticOperationCollapser(OperatorDiceNode.DIVIDE,
						(leftVal, rightVal) -> leftVal / rightVal, false));

		return operatorCollapsers;
	}

	private static Map<IDiceASTNode, BinaryOperator<AST<IDiceASTNode>>>
			buildNestedConstantCollapsers() {
		Map<IDiceASTNode, BinaryOperator<AST<IDiceASTNode>>> operatorCollapsers =
				new HashMap<>();

		operatorCollapsers.put(OperatorDiceNode.ADD,
				new NestedArithmeticOperationCollapser(
						OperatorDiceNode.ADD,
						(leftVal, rightVal) -> leftVal + rightVal));

		operatorCollapsers.put(OperatorDiceNode.MULTIPLY,
				new NestedArithmeticOperationCollapser(
						OperatorDiceNode.MULTIPLY,
						(leftVal, rightVal) -> leftVal * rightVal));

		return operatorCollapsers;
	}

	private static AST<IDiceASTNode> collapseLeaf(IDiceASTNode leaf) {
		// Can't optimize a simple reference
		if (leaf.getType() == VARIABLE) {
			return new AST<>(leaf);
		} else if (leaf.getType() == LITERAL) {
			LiteralDiceNode node = (LiteralDiceNode) leaf;

			return new AST<>(optimizeLiteral(node, node.toExpression()));
		} else {
			throw new UnsupportedOperationException(
					"Found leaf operator. This isn't supported");
		}
	}

	private static IDiceASTNode optimizeLiteral(LiteralDiceNode node,
			IDiceExpression leaf) {
		if (leaf.canOptimize()) {
			return new LiteralDiceNode(Integer.toString(leaf.optimize()));
		} else {
			return node;
		}
	}

	private static AST<IDiceASTNode> finishTree(AST<IDiceASTNode> tree) {
		return tree;
	}

	/**
	 * Optimize a tree of expressions into a simpler form
	 * 
	 * @param tree
	 *            The tree to optimize
	 * @return The optimized tree
	 */
	public static AST<IDiceASTNode> optimizeTree(AST<IDiceASTNode> tree) {
		AST<IDiceASTNode> astWithConstantsFolded =
				tree.collapse(DiceASTOptimizer::collapseLeaf,
						buildConstantCollapsers()::get,
						DiceASTOptimizer::finishTree);

		AST<IDiceASTNode> astWithNestedConstantsFolded =
				astWithConstantsFolded.collapse(
						DiceASTOptimizer::collapseLeaf,
						buildNestedConstantCollapsers()::get,
						DiceASTOptimizer::finishTree);

		return astWithNestedConstantsFolded;
	}

	private static boolean checkNodeType(AST<IDiceASTNode> ast,
			DiceASTType type) {
		return ast.applyToHead((node) -> node.getType()) == type;
	}

	private static boolean isNodeConstant(AST<IDiceASTNode> ast) {
		return ast.applyToHead(
				(node) -> ((LiteralDiceNode) node).isConstant());
	}

	private static int getNodeValue(AST<IDiceASTNode> ast) {
		return ast.applyToHead(
				(node) -> ((LiteralDiceNode) node).toConstant());
	}
}
