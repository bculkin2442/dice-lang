package bjc.dicelang.dice;

public interface DieList {
	boolean canOptimize();
	long[]  optimize();

	long[] roll();
}