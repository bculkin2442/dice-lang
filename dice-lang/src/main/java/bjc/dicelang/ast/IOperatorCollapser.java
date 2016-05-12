package bjc.dicelang.ast;

import java.util.function.Function;

import bjc.utils.data.IPair;
import bjc.utils.funcdata.IList;
import bjc.utils.funcdata.ITree;

import bjc.dicelang.ast.nodes.IDiceASTNode;

/**
 * Alias for operator collapsers. Because 68-char types are too long
 * 
 * @author ben
 *
 */
public interface IOperatorCollapser extends
		Function<IList<IPair<IResult, ITree<IDiceASTNode>>>, IPair<IResult, ITree<IDiceASTNode>>> {
	// Just an alias
}
