package bjc.dicelang;

import static bjc.dicelang.Errors.ErrorKey.EK_PARSE_BINARY;
import static bjc.dicelang.Errors.ErrorKey.EK_PARSE_INVTOKEN;
import static bjc.dicelang.Errors.ErrorKey.EK_PARSE_NOCLOSE;
import static bjc.dicelang.Errors.ErrorKey.EK_PARSE_UNCLOSE;
import static bjc.dicelang.Errors.ErrorKey.EK_PARSE_UNOPERAND;
import static bjc.dicelang.Node.Type.BINOP;
import static bjc.dicelang.Node.Type.GROUP;
import static bjc.dicelang.Node.Type.OGROUP;
import static bjc.dicelang.Node.Type.TOKREF;
import static bjc.dicelang.Node.Type.UNARYOP;
import static bjc.dicelang.tokens.Token.Type.CBRACE;
import static bjc.dicelang.tokens.Token.Type.CBRACKET;

import java.util.Deque;
import java.util.LinkedList;

import bjc.dicelang.tokens.Token;
import bjc.data.Tree;
import bjc.data.SimpleTree;
import bjc.funcdata.ListEx;

/**
 * Parse a series of tree into tokens.
 *
 * @author EVE
 *
 */
public class Parser {
	/** Create a new parser. */
	public Parser() {

	}

	/**
	 * Parse a series of tokens to a forest of ASTs.
	 *
	 * @param tokens
	 *        The list of tokens to parse.
	 *
	 * @param results
	 *        The place to set results.
	 *
	 * @return Whether or not the parse was successful.
	 */
	public static boolean parseTokens(final ListEx<Token> tokens, final ListEx<Tree<Node>> results) {
		final Deque<Tree<Node>> working = new LinkedList<>();

		for(final Token tk : tokens) {
			switch(tk.type) {
			case OBRACKET:
			case OBRACE:
				/* Parse opening delims. */
				working.push(new SimpleTree<>(new Node(OGROUP, tk)));

				break;
			case CBRACKET:
			case CBRACE:
				/* Parse closing delims. */
				final boolean sc = parseClosingGrouper(working, tk);

				if(!sc) {
					return false;
				}

				break;
			case MULTIPLY:
			case DIVIDE:
			case IDIVIDE:
			case DICEGROUP:
			case DICECONCAT:
			case DICELIST:
			case STRCAT:
			case STRREP:
			case LET:
			case BIND:
				/* Parse binary operator. */
				if(working.size() < 2) {
					Errors.inst.printError(EK_PARSE_BINARY);
					return false;
				}

				handleBinaryNode(working, tk);
				break;
			case ADD:
			case SUBTRACT:
				/* Handle binary/unary operators. */
				if(working.size() == 0) {
					Errors.inst.printError(EK_PARSE_UNOPERAND, tk.toString());
					return false;
				} else if(working.size() == 1) {
					final Tree<Node> operand = working.pop();
					final Tree<Node> opNode = new SimpleTree<>(new Node(UNARYOP, tk.type));

					opNode.addChild(operand);

					working.push(opNode);
				} else {
					handleBinaryNode(working, tk);
				}

				break;
			case COERCE:
			case DICESCALAR:
			case DICEFUDGE:
				/* Handle unary operators. */
				if(working.size() == 0) {
					Errors.inst.printError(EK_PARSE_UNOPERAND, tk.toString());
				} else {
					final Tree<Node> operand = working.pop();
					final Tree<Node> opNode = new SimpleTree<>(new Node(UNARYOP, tk.type));

					opNode.addChild(operand);

					working.push(opNode);
				}

				break;
			case INT_LIT:
			case FLOAT_LIT:
			case STRING_LIT:
			case VREF:
			case DICE_LIT:
				/* Handle literals. */
				working.push(new SimpleTree<>(new Node(TOKREF, tk)));
				break;
			default:
				Errors.inst.printError(EK_PARSE_INVTOKEN, tk.type.toString());
				return false;
			}
		}

		/*
		 * Collect the remaining nodes as the roots of the trees in the
		 * AST forest.
		 */
		for(final Tree<Node> ast : working) {
			/* Make sure that the tree are well-formed */
			if(ast.containsMatching((val) -> {
				switch(val.type) {
				case OGROUP:
					return true;
				default:
					return false;
				}
			})) {
				System.out.printf("\tERROR: Malformed tree:\n%s\n", ast);

				return false;
			} else {
				results.add(ast);
			}
		}

		return true;
	}

	/* Handle a binary operator. */
	private static void handleBinaryNode(final Deque<Tree<Node>> working, final Token tk) {
		final Tree<Node> right = working.pop();
		final Tree<Node> left = working.pop();

		final Tree<Node> opNode = new SimpleTree<>(new Node(BINOP, tk.type));

		opNode.addChild(left);
		opNode.addChild(right);

		working.push(opNode);
	}

	/* Parse a closing delimiter. */
	private static boolean parseClosingGrouper(final Deque<Tree<Node>> working, final Token tk) {
		if(working.size() == 0) {
			Errors.inst.printError(EK_PARSE_NOCLOSE);
			return false;
		}

		Tree<Node> groupNode = null;

		switch(tk.type) {
		case CBRACE:
			groupNode = new SimpleTree<>(new Node(GROUP, Node.GroupType.CODE));
			break;
		case CBRACKET:
			groupNode = new SimpleTree<>(new Node(GROUP, Node.GroupType.ARRAY));
			break;
		default:
			Errors.inst.printError(EK_PARSE_UNCLOSE, tk.type.toString());
			return false;
		}

		Token matching = null;

		if(tk.type == CBRACKET) {
			matching = new Token(Token.Type.OBRACKET, tk.intValue);
		} else if(tk.type == CBRACE) {
			matching = new Token(Token.Type.OBRACE, tk.intValue);
		}

		final Tree<Node> matchNode = new SimpleTree<>(new Node(OGROUP, matching));

		if(!working.contains(matchNode)) {
			Errors.inst.printError(EK_PARSE_UNCLOSE, tk.toString(), matchNode.toString());

			System.out.println("\tCurrent forest is: ");

			int treeNo = 1;

			for(final Tree<Node> ast : working) {
				System.out.println("Tree " + treeNo++ + ": " + ast.toString());
			}

			return false;
		}

		final Deque<Tree<Node>> childs = new LinkedList<>();

		while(!working.peek().equals(matchNode)) {
			childs.push(working.pop());
		}

		/* Discard opener */
		working.pop();

		for(final Tree<Node> child : childs) {
			groupNode.addChild(child);
		}

		working.push(groupNode);

		return true;
	}
}
