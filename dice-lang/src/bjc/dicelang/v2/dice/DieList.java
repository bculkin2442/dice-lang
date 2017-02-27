package bjc.dicelang.v2.dice;

public interface DieList {
	boolean canOptimize();
	long[]  optimize();

	long[] roll();
}