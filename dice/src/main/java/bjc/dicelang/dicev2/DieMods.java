package bjc.dicelang.dicev2;

public class DieMods {
	public Die sum(Die... dice) {
		return new SumDieMod(dice);
	}
}
