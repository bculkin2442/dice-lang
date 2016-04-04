package bjc.dicelang.ast;

import java.util.Map;
import java.util.function.Consumer;

import bjc.utils.data.IHolder;

/**
 * Check if the specified node references a particular variable
 * 
 * @author ben
 *
 */
public final class DiceASTDefinedChecker
		implements Consumer<IDiceASTNode> {
	/**
	 * This is true if the specified node references the set variable
	 */
	private IHolder<Boolean>				referencesVariable;

	private Map<String, DiceASTExpression>	enviroment;

	/**
	 * Create a new reference checker
	 * 
	 * @param referencesVar
	 *            The holder of whether the variable is referenced or not
	 * @param env
	 *            The enviroment to check undefinedness against
	 */
	public DiceASTDefinedChecker(IHolder<Boolean> referencesVar,
			Map<String, DiceASTExpression> env) {
		this.referencesVariable = referencesVar;
		this.enviroment = env;
	}

	@Override
	public void accept(IDiceASTNode astNode) {
		referencesVariable.transform((bool) -> checkUndefined(astNode));
	}

	/**
	 * Check if a given AST node references an undefined variable
	 * 
	 * @param astNode
	 *            The node to check
	 * @return Whether or not the node directly the variable
	 */
	private boolean checkUndefined(IDiceASTNode astNode) {
		if (astNode.getType() == DiceASTType.VARIABLE) {
			VariableDiceNode node = (VariableDiceNode) astNode;

			return !enviroment.containsKey(node.getVariable());
		} else {
			return false;
		}
	}
}