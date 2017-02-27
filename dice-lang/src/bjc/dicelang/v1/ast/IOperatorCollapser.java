package bjc.dicelang.v1.ast;

import java.util.function.Function;

import bjc.dicelang.v1.ast.nodes.IDiceASTNode;
import bjc.utils.data.IPair;
import bjc.utils.data.ITree;
import bjc.utils.funcdata.IList;

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
