package bjc.dicelang.v2.dice;

public class SimpleDie implements Die {
	private Die numDice;
	private Die diceSize;

	public SimpleDie(long nDice, long size) {
		numDice  = new ScalarDie(nDice);
		diceSize = new ScalarDie(size);
	}

	public SimpleDie(Die nDice, long size) {
		numDice  = nDice;
		diceSize = new ScalarDie(size);
	}

	public SimpleDie(long nDice, Die size) {
		numDice  = new ScalarDie(nDice);
		diceSize = size;
	}

	public SimpleDie(Die nDice, Die size) {
		numDice  = nDice;
		diceSize = size;
	}

	public boolean canOptimize() {
		if(diceSize.canOptimize() && (diceSize.optimize() <= 1)) {
			return numDice.canOptimize();
		} else return false;
	}

	public long optimize() {
		long optSize = diceSize.optimize();

		if(optSize == 0) return 0;
		else             return numDice.optimize();
	}

	public long roll() {
		long total = 0;

		long nDice = numDice.roll();
		long dSize = diceSize.roll();

		for(int i = 0; i < nDice; i++) {
			total += (Math.abs(DiceBox.rng.nextLong()) % dSize) + 1;
		}

		return total;
	}

	public long rollSingle() {
		return (Math.abs(DiceBox.rng.nextLong()) % diceSize.roll()) + 1;
	}

	public String toString() {
		return numDice + "d" + diceSize;
	}
}