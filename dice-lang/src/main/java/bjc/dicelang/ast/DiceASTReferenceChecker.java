package bjc.dicelang.ast;

import java.util.function.Consumer;

import bjc.utils.data.GenHolder;

/**
 * Check if the specified node references a particular variable
 * 
 * @author ben
 *
 */
public final class DiceASTReferenceChecker
		implements Consumer<IDiceASTNode> {
	/**
	 * This is true if the specified node references the set variable
	 */
	private GenHolder<Boolean>	referencesVariable;

	private String				varName;

	/**
	 * Create a new reference checker
	 * 
	 * @param referencesVar
	 *            The holder of whether the variable is referenced or
	 *            not
	 * @param varName
	 *            The variable to check for references in
	 */
	public DiceASTReferenceChecker(GenHolder<Boolean> referencesVar,
			String varName) {
		this.referencesVariable = referencesVar;
		this.varName = varName;
	}

	@Override
	public void accept(IDiceASTNode astNode) {
		if (!referencesVariable.unwrap(bool -> bool)) {
			if (isDirectReferenceToLast(astNode)) {
				referencesVariable.transform((bool) -> false);
			}
		}
	}

	/**
	 * Check if a given AST node directly references the meta-variable
	 * last
	 * 
	 * @param astNode
	 *            The node to check
	 * @return Whether or not the node directly references last
	 */
	private boolean isDirectReferenceToLast(IDiceASTNode astNode) {
		if (astNode.getType() == DiceASTType.VARIABLE) {
			VariableDiceNode node = (VariableDiceNode) astNode;

			return node.getVariable().equals(varName);
		} else {
			return false;
		}
	}
}