package bjc.dicelang.eval;

import bjc.dicelang.Node;
import bjc.utils.data.ITree;
import bjc.utils.data.Tree;

/**
 * Represents an evaluation ending in failure.
 * 
 * @author student
 *
 */
public class FailureEvaluatorResult extends EvaluatorResult {
	/**
	 * Original node data
	 */
	public ITree<Node> origVal;

	/**
	 * Create a new generic failure.
	 */
	public FailureEvaluatorResult() {
		super(Type.FAILURE);
	}

	/**
	 * Create a new failure result
	 * 
	 * @param orig
	 *            The tree that caused the failure.
	 */
	public FailureEvaluatorResult(final ITree<Node> orig) {
		super(Type.FAILURE);

		origVal = orig;
	}

	/**
	 * Create a new failure result
	 * 
	 * @param orig
	 *            The node that caused the failure.
	 */
	public FailureEvaluatorResult(final Node orig) {
		this(new Tree<>(orig));
	}
	
	/**
	 * Create a new failure result
	 * 
	 * @param right
	 *            The result that caused the failure.
	 */
	public FailureEvaluatorResult(EvaluatorResult right) {
		this(new Node(Node.Type.RESULT, right));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((origVal == null) ? 0 : origVal.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FailureEvaluatorResult other = (FailureEvaluatorResult) obj;
		if (origVal == null) {
			if (other.origVal != null)
				return false;
		} else if (!origVal.equals(other.origVal))
			return false;
		return true;
	}
}
