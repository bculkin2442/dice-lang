package bjc.dicelang.dice;

public interface Die {
	boolean canOptimize();
	long    optimize();

	long roll();
	long rollSingle();
}