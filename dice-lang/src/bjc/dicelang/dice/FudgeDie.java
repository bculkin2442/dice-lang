package bjc.dicelang.dice;

public class FudgeDie implements Die {
	private Die numDice;

	public FudgeDie(long nDice) {
		numDice = new ScalarDie(nDice);
	}

	public FudgeDie(Die nDice) {
		numDice = nDice;
	}

	public boolean canOptimize() {
		return numDice.canOptimize() && numDice.optimize() == 0;
	}

	public long optimize() {
		return 0;
	}

	public long roll() {
		long res = 0;
		
		long nDice = numDice.roll();

		for(int i = 0; i < nDice; i++) {
			res += rollSingle();
		}

		return res;
	}

	public long rollSingle() {
		return DiceBox.rng.nextInt(3) - 1;
	}

	public String toString() {
		return numDice + "dF";
	}
}
