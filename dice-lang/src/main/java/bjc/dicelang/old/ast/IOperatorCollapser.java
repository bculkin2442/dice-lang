package bjc.dicelang.old.ast;

import java.util.function.BinaryOperator;

import bjc.dicelang.ast.nodes.IDiceASTNode;
import bjc.utils.data.Pair;
import bjc.utils.parserutils.AST;

/**
 * Alias for operator collapsers. Because 68-char types are too long
 * 
 * @author ben
 *
 */
public interface IOperatorCollapser
		extends BinaryOperator<Pair<Integer, AST<IDiceASTNode>>> {
	// Just an alias
}
