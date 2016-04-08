package bjc.dicelang.old.ast;

import java.util.function.Consumer;

import bjc.dicelang.ast.nodes.DiceASTType;
import bjc.dicelang.ast.nodes.IDiceASTNode;
import bjc.dicelang.ast.nodes.VariableDiceNode;
import bjc.utils.data.IHolder;

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
	private IHolder<Boolean>	referencesVariable;

	private String				varName;

	/**
	 * Create a new reference checker
	 * 
	 * @param referencesVar
	 *            The holder of whether the variable is referenced or not
	 * @param varName
	 *            The variable to check for references in
	 */
	public DiceASTReferenceChecker(IHolder<Boolean> referencesVar,
			String varName) {
		this.referencesVariable = referencesVar;
		this.varName = varName;
	}

	@Override
	public void accept(IDiceASTNode astNode) {
		referencesVariable.transform((bool) -> isDirectReference(astNode));
	}

	/**
	 * Check if a given AST node directly references the specified variable
	 * 
	 * @param astNode
	 *            The node to check
	 * @return Whether or not the node directly the variable
	 */
	private boolean isDirectReference(IDiceASTNode astNode) {
		if (astNode.getType() == DiceASTType.VARIABLE) {
			VariableDiceNode node = (VariableDiceNode) astNode;

			return node.getVariable().equals(varName);
		} else {
			return false;
		}
	}
}