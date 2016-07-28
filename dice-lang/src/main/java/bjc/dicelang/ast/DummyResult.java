package bjc.dicelang.ast;

public class DummyResult implements IResult {
	/*
	 * The reason why this result is a dummy
	 */
	private String dummyData;

	public DummyResult(String data) {
		dummyData = data;
	}

	@Override
	public ResultType getType() {
		return ResultType.DUMMY;
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
	public String toString() {
		return "Dummy with reason " + dummyData;
	}
}
