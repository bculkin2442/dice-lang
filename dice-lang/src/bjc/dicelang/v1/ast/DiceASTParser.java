package bjc.dicelang.v1.ast;

import java.util.Deque;
import java.util.InputMismatchException;
import java.util.function.Function;
import java.util.function.Predicate;

import bjc.dicelang.v1.IDiceExpression;
import bjc.dicelang.v1.ast.nodes.DiceLiteralNode;
import bjc.dicelang.v1.ast.nodes.DiceLiteralType;
import bjc.dicelang.v1.ast.nodes.IDiceASTNode;
import bjc.dicelang.v1.ast.nodes.ILiteralDiceNode;
import bjc.dicelang.v1.ast.nodes.IntegerLiteralNode;
import bjc.dicelang.v1.ast.nodes.OperatorDiceNode;
import bjc.dicelang.v1.ast.nodes.VariableDiceNode;
import bjc.utils.data.ITree;
import bjc.utils.funcdata.FunctionalList;
import bjc.utils.funcdata.FunctionalMap;
import bjc.utils.funcdata.IList;
import bjc.utils.funcdata.IMap;
import bjc.utils.data.Tree;
import bjc.utils.funcutils.StringUtils;
import bjc.utils.parserutils.TreeConstructor;

/**
 * Parse a string expression into AST form. Doesn't do anything else
 * 
 * @author ben
 *
 */
public class DiceASTParser {
		private static IDiceASTNode convertLeafNode(String leafNode) {
				DiceLiteralType literalType = ILiteralDiceNode
						.getLiteralType(leafNode);

				if (literalType != null) {
						switch (literalType) {
								case DICE:
										return new DiceLiteralNode(
														IDiceExpression.toExpression(leafNode));
								case INTEGER:
										return new IntegerLiteralNode(
														Integer.parseInt(leafNode));
								default:
										throw new InputMismatchException(
														"Cannot convert string '" + leafNode
														+ "' into a literal.");
						}
				}

				if (leafNode.matches("[+-]?\\d*\\.\\d+")) {
						throw new InputMismatchException(
										"Floating point literals are not supported");
				}

				return new VariableDiceNode(leafNode);
		}

		private static IDiceASTNode convertOperatorNode(String operatorNode) {
				try {
						return OperatorDiceNode.fromString(operatorNode);
				} catch (IllegalArgumentException iaex) {
						InputMismatchException imex = new InputMismatchException(
										"Attempted to parse invalid operator " + operatorNode);

						imex.initCause(iaex);

						throw imex;
				}
		}

		/**
		 * Create an AST from a list of tokens
		 * 
		 * @param tokens
		 *            The list of tokens to convert
		 * @return An AST built from the tokens
		 */
		public static ITree<IDiceASTNode> createFromString(
						IList<String> tokens) {
				// Mark arrays as special operators
				Predicate<String> specialPicker = (operator) -> {
						if (StringUtils.containsOnly(operator, "\\[") ||
										StringUtils.containsOnly(operator, "\\]")) {
								return true;
										}

						return false;
				};

				// Here is the map for holding special operators
				IMap<String, Function<Deque<ITree<String>>, ITree<String>>> operators = new FunctionalMap<>();

				// Handle open [
				operators.put("[", (queuedTrees) -> {
						// Just put in a [
						Tree<String> openArray = new Tree<>("[");

						return openArray;
				});

				operators.put("]", (queuedTrees) -> {
						// Parse closing an array
						return parseCloseArray(queuedTrees);
				});

				ITree<String> rawTokens = TreeConstructor.constructTree(tokens,
								(token) -> {
										return isOperatorNode(token);
								}, specialPicker, operators::get);

				ITree<IDiceASTNode> tokenizedTree = rawTokens.rebuildTree(
								DiceASTParser::convertLeafNode,
								DiceASTParser::convertOperatorNode);

				return tokenizedTree;
						}

		private static boolean isOperatorNode(String token) {
				if (StringUtils.containsOnly(token, "\\[")) {
						return true;
				} else if (StringUtils.containsOnly(token, "\\]")) {
						return true;
				}

				if (token.equals("[]")) {
						// This is a synthetic operator, constructed by [ and ]
						return true;
				}

				try {
						OperatorDiceNode.fromString(token);
						return true;
				} catch (IllegalArgumentException iaex) {
						// We don't care about details
						return false;
				}
		}

		private static ITree<String> parseCloseArray(
						Deque<ITree<String>> queuedTrees) {
				IList<ITree<String>> children = new FunctionalList<>();

				while (shouldContinuePopping(queuedTrees)) {
						children.add(queuedTrees.pop());
				}

				queuedTrees.pop();

				children.reverse();

				ITree<String> arrayTree = new Tree<>("[]", children);

				return arrayTree;
						}

		private static boolean shouldContinuePopping(
						Deque<ITree<String>> queuedTrees) {
				String peekToken = queuedTrees.peek().getHead();

				return !peekToken.equals("[");
						}
}