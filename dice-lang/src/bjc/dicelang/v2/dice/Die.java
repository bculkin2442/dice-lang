package bjc.dicelang.v2.dice;

public interface Die {
	boolean canOptimize();
	long    optimize();

	long roll();
	long rollSingle();
}