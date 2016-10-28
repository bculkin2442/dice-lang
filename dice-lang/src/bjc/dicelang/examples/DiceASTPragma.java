package bjc.dicelang.examples;

import java.util.function.BiConsumer;

import bjc.utils.data.ITree;
import bjc.utils.funcdata.FunctionalStringTokenizer;
import bjc.utils.funcdata.IMap;

import bjc.dicelang.ast.nodes.IDiceASTNode;

/**
 * Alias for the type of a 'pragma' or special language command
 * 
 * To explain it, a pragma is a function that takes a tokenizer with the rest
 * of the line, and an enviroment that contains variable bindings
 * @author ben
 *
 */
public interface DiceASTPragma extends
		BiConsumer<FunctionalStringTokenizer, IMap<String, ITree<IDiceASTNode>>> {
	// Just an alias
}
