package bjc.dicelang.v1.ast;

/**
 * A dummy result
 *
 * @author ben
 *
 */
public class DummyResult implements IResult {
	/*
	 * The reason why this result is a dummy
	 */
	private String dummyData;

	/**
	 * Create a new dummy result with a reason
	 *
	 * @param data
	 *                The reason why the result is a dummy
	 */
	public DummyResult(String data) {
		dummyData = data;
	}

	/**
	 * Get the data in this dummy
	 *
	 * @return The reason why this result is a dummy
	 */
	public String getData() {
		return dummyData;
	}

	@Override
	public ResultType getType() {
		return ResultType.DUMMY;
	}

	@Override
	public String toString() {
		return "Dummy with reason " + dummyData;
	}
}
