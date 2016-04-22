package bjc.dicelang.ast;

import bjc.dicelang.ast.nodes.IDiceASTNode;
import bjc.dicelang.ast.nodes.OperatorDiceNode;
import bjc.dicelang.ast.nodes.VariableDiceNode;
import bjc.utils.data.IHolder;
import bjc.utils.data.Identity;
import bjc.utils.funcdata.IFunctionalMap;
import bjc.utils.funcdata.ITree;
import bjc.utils.funcdata.TopDownTransformResult;
import bjc.utils.funcdata.Tree;

/**
 * Sanitize the references in an AST so that a variable that refers to
 * itself in its definition has the occurance of it replaced with its
 * previous definition
 * 
 * @author ben
 *
 */
public class DiceASTReferenceSanitizer {
	/**
	 * Sanitize the references in an AST
	 * 
	 * @param ast
	 * @param enviroment
	 * @return The sanitized AST
	 */
	public static ITree<IDiceASTNode> sanitize(ITree<IDiceASTNode> ast,
			IFunctionalMap<String, ITree<IDiceASTNode>> enviroment) {
		return ast.topDownTransform(
				DiceASTReferenceSanitizer::shouldSanitize, (subTree) -> {
					return doSanitize(subTree, enviroment);
				});
	}

	private static TopDownTransformResult shouldSanitize(
			IDiceASTNode node) {
		if (!node.isOperator()) {
			return TopDownTransformResult.SKIP;
		}

		switch (((OperatorDiceNode) node)) {
			case ASSIGN:
				return TopDownTransformResult.TRANSFORM;
			case ARRAY:
			case LET:
				return TopDownTransformResult.PASSTHROUGH;
			case ADD:
			case COMPOUND:
			case DIVIDE:
			case GROUP:
			case MULTIPLY:
			case SUBTRACT:
			default:
				return TopDownTransformResult.SKIP;
		}
	}

	private static ITree<IDiceASTNode> doSanitize(ITree<IDiceASTNode> ast,
			IFunctionalMap<String, ITree<IDiceASTNode>> enviroment) {
		if (ast.getChildrenCount() != 2) {
			throw new UnsupportedOperationException(
					"Assignment must have two arguments.");
		}

		ITree<IDiceASTNode> nameTree = ast.getChild(0);
		ITree<IDiceASTNode> valueTree = ast.getChild(1);

		if (!DiceASTUtils.containsSimpleVariable(nameTree)) {
			if (nameTree.getHead() == OperatorDiceNode.ARRAY) {
				IHolder<Boolean> allSimpleVariables = new Identity<>(true);

				nameTree.doForChildren((child) -> {
					if (allSimpleVariables.getValue()) {
						boolean isSimple = DiceASTUtils
								.containsSimpleVariable(child);

						allSimpleVariables.replace(isSimple);
					}
				});

				if (!allSimpleVariables.getValue()) {
					throw new UnsupportedOperationException(
							"Array assignment must be between variables and"
									+ " a expression/array of expressions");
				}

				if (valueTree.getHead() == OperatorDiceNode.ARRAY) {
					if (nameTree.getChildrenCount() != valueTree
							.getChildrenCount()) {
						throw new UnsupportedOperationException(
								"Array assignment between arrays must be"
										+ " between two arrays of equal length");
					}
				}
			} else {
				throw new UnsupportedOperationException(
						"Assignment must be between a variable and a expression");
			}
		}

		if (nameTree.getHead() == OperatorDiceNode.ARRAY) {
			if (valueTree.getHead() == OperatorDiceNode.ARRAY) {
				IHolder<Integer> childCounter = new Identity<>(0);

				ITree<IDiceASTNode> returnTree = new Tree<>(
						OperatorDiceNode.ARRAY);

				nameTree.doForChildren((child) -> {
					String variableName = child.transformHead((node) -> {
						return ((VariableDiceNode) node).getVariable();
					});

					ITree<IDiceASTNode> currentValue = valueTree
							.getChild(childCounter.getValue());

					ITree<IDiceASTNode> sanitizedSubtree = doSingleSanitize(
							ast, enviroment, child, currentValue,
							variableName);

					if (sanitizedSubtree == null) {
						ITree<IDiceASTNode> oldTree = new Tree<>(
								ast.getHead(), child, currentValue);

						returnTree.addChild(oldTree);
					} else {
						returnTree.addChild(sanitizedSubtree);
					}

					childCounter.transform((count) -> count + 1);
				});

				return returnTree;
			}

			ITree<IDiceASTNode> returnTree = new Tree<>(
					OperatorDiceNode.ARRAY);

			nameTree.doForChildren((child) -> {
				String variableName = child.transformHead(
						(node) -> ((VariableDiceNode) node).getVariable());

				ITree<IDiceASTNode> sanitizedChild = doSingleSanitize(ast,
						enviroment, child, valueTree, variableName);
				if (sanitizedChild == null) {
					ITree<IDiceASTNode> oldTree = new Tree<>(ast.getHead(),
							child, valueTree);

					returnTree.addChild(oldTree);
				} else {
					returnTree.addChild(sanitizedChild);
				}
			});

			return returnTree;
		}

		String variableName = nameTree.transformHead(
				(node) -> ((VariableDiceNode) node).getVariable());

		ITree<IDiceASTNode> sanitizedTree = doSingleSanitize(ast,
				enviroment, nameTree, valueTree, variableName);

		if (sanitizedTree == null) {
			return ast;
		}

		return sanitizedTree;
	}

	private static ITree<IDiceASTNode> doSingleSanitize(
			ITree<IDiceASTNode> ast,
			IFunctionalMap<String, ITree<IDiceASTNode>> enviroment,
			ITree<IDiceASTNode> nameTree, ITree<IDiceASTNode> valueTree,
			String variableName) {
		if (enviroment.containsKey(variableName)) {
			// @ is a meta-variable standing for the left side of an
			// assignment
			ITree<IDiceASTNode> oldVal = enviroment.put("@",
					enviroment.get(variableName));

			// We should always inline out references to last, because it
			// will always change
			ITree<IDiceASTNode> inlinedValue = DiceASTInliner
					.selectiveInline(valueTree, enviroment, variableName,
							"last", "@");

			if (oldVal != null) {
				enviroment.put("@", oldVal);
			} else {
				enviroment.remove("@");
			}

			return new Tree<>(ast.getHead(), nameTree, inlinedValue);
		}

		return null;
	}
}
