package bjc.dicelang.ast;

import java.util.function.BinaryOperator;

import bjc.dicelang.ast.nodes.IDiceASTNode;
import bjc.utils.data.IPair;
import bjc.utils.parserutils.AST;

/**
 * Alias for operator collapsers. Because 68-char types are too long
 * 
 * @author ben
 *
 */
public interface IOperatorCollapser
		extends BinaryOperator<IPair<Integer, AST<IDiceASTNode>>> {
	// Just an alias
}
