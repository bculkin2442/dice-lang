package bjc.dicelang.ast;

import java.util.function.Function;

import bjc.dicelang.ast.nodes.IDiceASTNode;
import bjc.utils.data.IPair;
import bjc.utils.funcdata.IFunctionalList;
import bjc.utils.funcdata.ITree;

/**
 * Alias for operator collapsers. Because 68-char types are too long
 * 
 * @author ben
 *
 */
public interface IOperatorCollapser extends
		Function<IFunctionalList<IPair<IResult, ITree<IDiceASTNode>>>,
							IPair<IResult, ITree<IDiceASTNode>>> {
	// Just an alias
}
