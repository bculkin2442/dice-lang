package bjc.dicelang.eval;

import bjc.dicelang.Node;
import bjc.utils.data.ITree;
import bjc.utils.data.Tree;

public class FailureEvaluatorResult extends EvaluatorResult {
	/**
	 * Original node data
	 */
	public ITree<Node> origVal;

	public FailureEvaluatorResult() {
		super(Type.FAILURE);
	}

	public FailureEvaluatorResult(final ITree<Node> orig) {
		super(Type.FAILURE);

		origVal = orig;
	}

	public FailureEvaluatorResult(final Node orig) {
		this(new Tree<>(orig));
	}

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
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		FailureEvaluatorResult other = (FailureEvaluatorResult) obj;
		if(origVal == null) {
			if(other.origVal != null) return false;
		} else if(!origVal.equals(other.origVal)) return false;
		return true;
	}
}
