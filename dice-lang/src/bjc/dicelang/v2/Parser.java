package bjc.dicelang.v2;

import java.util.Deque;
import java.util.LinkedList;

import bjc.utils.data.ITree;
import bjc.utils.data.Tree;
import bjc.utils.funcdata.IList;

import static bjc.dicelang.v2.Errors.ErrorKey.*;
import static bjc.dicelang.v2.Node.Type.*;
import static bjc.dicelang.v2.Token.Type.*;

public class Parser {
	public Parser() {

	}

	public boolean parseTokens(IList<Token> tokens,
			IList<ITree<Node>> results) {
		Deque<ITree<Node>> working = new LinkedList<>();

		for(Token tk : tokens) {
			switch(tk.type) {
				case OBRACKET:
				case OBRACE:
					working.push(new Tree<>(new Node(OGROUP, tk)));
					break;
				case CBRACKET:
				case CBRACE:
					if(working.size() == 0) {
						Errors.inst.printError(EK_PARSE_NOCLOSE);
						return false;
					}
					
					ITree<Node> groupNode = null;
					switch(tk.type) {
						case CBRACE:
							groupNode = new Tree<>(new Node(GROUP, Node.GroupType.CODE));
							break;
						case CBRACKET:
							groupNode = new Tree<>(new Node(GROUP, Node.GroupType.ARRAY));
							break;
						default:
							break;
					}
					Token matching = null;

					if(tk.type == CBRACKET) {
						matching = new Token(Token.Type.OBRACKET, tk.intValue);
					} else if(tk.type == CBRACE) {
						matching = new Token(Token.Type.OBRACE, tk.intValue);
					}

					ITree<Node> matchNode = new Tree<>(new Node(OGROUP, matching));

					if(!working.contains(matchNode)) {
						Errors.inst.printError(EK_PARSE_UNCLOSE, tk.toString(), matchNode.toString());

						System.out.println("\tCurrent forest is: ");

						int treeNo = 1;
						for(ITree<Node> ast : working) {
							System.out.println("Tree " + treeNo++ + ": " + ast.toString());
						}

						return false;
					} else {
						Deque<ITree<Node>> childs = new LinkedList<>();

						while(!working.peek().equals(matchNode)) {
							childs.push(working.pop());
						}

						// Discard opener
						working.pop();

						for(ITree<Node> child : childs) {
							groupNode.addChild(child);
						}

						working.push(groupNode);
					}
					break;
				case LET:
				case BIND:
					if(working.size() < 2) {
						Errors.inst.printError(EK_PARSE_BINARY);
						return false;
					} else {
						ITree<Node> right = working.pop();
						ITree<Node> left  = working.pop();

						ITree<Node> opNode = new Tree<>(new Node(BINOP, tk.type));
						
						working.push(opNode);
					}
					break;
				case ADD:
				case SUBTRACT:
				case MULTIPLY:
				case DIVIDE:
				case IDIVIDE:
				case DICEGROUP:
				case DICECONCAT:
				case DICELIST:
					if(working.size() == 0) {
						Errors.inst.printError(EK_PARSE_UNOPERAND, tk.toString());
						return false;
					} else if(working.size() == 1) {
						ITree<Node> operand = working.pop();

						ITree<Node> opNode  = new Tree<>(new Node(UNARYOP, tk.type));
						
						opNode.addChild(operand);

						working.push(opNode);
					} else {
						ITree<Node> right = working.pop();
						ITree<Node> left  = working.pop();

						ITree<Node> opNode = new Tree<>(new Node(BINOP, tk.type));
						
						opNode.addChild(left);
						opNode.addChild(right);

						working.push(opNode);
					}
					break;
				case COERCE:
					if(working.size() == 0) {
						Errors.inst.printError(EK_PARSE_UNOPERAND, tk.toString());
					} else {
						ITree<Node> operand = working.pop();
						ITree<Node> opNode  = new Tree<>(new Node(UNARYOP, tk.type));

						opNode.addChild(operand);

						working.push(opNode);
					}
					break;
				case INT_LIT:
				case FLOAT_LIT:
				case STRING_LIT:
				case VREF:
				case DICE_LIT:
					working.push(new Tree<>(new Node(TOKREF, tk)));
					break;
				default:
					Errors.inst.printError(EK_PARSE_INVTOKEN, tk.type.toString());
					return false;
			}
		}

		for(ITree<Node> ast : working) {
			results.add(ast);
		}

		return true;
	}
}
