package bjc.dicelang.examples;

import java.util.function.BiConsumer;

import bjc.dicelang.ast.nodes.IDiceASTNode;
import bjc.utils.funcdata.FunctionalStringTokenizer;
import bjc.utils.funcdata.IFunctionalMap;
import bjc.utils.funcdata.ITree;

/**
 * Alias for the type of a 'pragma' or special language command
 * 
 * @author ben
 *
 */
public interface DiceASTPragma extends
		BiConsumer<FunctionalStringTokenizer, IFunctionalMap<String, ITree<IDiceASTNode>>> {
	// Just an alias
}
