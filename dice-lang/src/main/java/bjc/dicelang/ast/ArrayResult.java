package bjc.dicelang.ast;

import bjc.utils.funcdata.IFunctionalList;

/**
 * Represents a result that is an array of other results
 * 
 * @author ben
 *
 *         TODO finish implementing me
 */
public class ArrayResult implements IResult {
	private IFunctionalList<IResult> arrayContents;

	/**
	 * Create a new array-valued result
	 * 
	 * @param results
	 *            The results in the array
	 */
	public ArrayResult(IFunctionalList<IResult> results) {
		this.arrayContents = results;
	}

	@Override
	public ResultType getType() {
		return ResultType.ARRAY;
	}

	/**
	 * Get the value of this result
	 * 
	 * @return The value of this result
	 */
	public IFunctionalList<IResult> getValue() {
		return arrayContents;
	}

	@Override
	public String toString() {
		return arrayContents.toString();
	}
}
