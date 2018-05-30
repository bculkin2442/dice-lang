package bjc.dicelang.dicev2;

import java.util.function.LongPredicate;

public class ComparePoints {
	public static LongPredicate isLess(long val) {
		return (arg) -> arg < val;
	}

	public static LongPredicate isEqual(long val) {
		return (arg) -> arg == val;
	}

	public static LongPredicate isGreater(long val) {
		return (arg) -> arg > val;
	}
}
