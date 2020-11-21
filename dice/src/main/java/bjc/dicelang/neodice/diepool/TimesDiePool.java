package bjc.dicelang.neodice.diepool;

import java.util.*;

import bjc.dicelang.neodice.*;

public class TimesDiePool implements DiePool {
	private final Die contained;
	private final int numDice;

	public TimesDiePool(Die contained, int numDice) {
		this.contained = contained;
		this.numDice = numDice;
	}

	@Override
	public int[] roll(Random rng) {
		int[] results = new int[numDice];
		
		for (int index = 0; index < numDice; index++) {
			results[index] = contained.roll(rng);
		}
		
		return results;
	}
	
	@Override
	public Die[] contained() {
		Die[] results = new Die[numDice];
		
		for (int index = 0; index < numDice; index++) {
			results[index] = contained;
		}
		
		return results;
	}

	@Override
	public String toString() {
		return String.format("%d%s", numDice, contained);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(contained, numDice);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)                  return true;
		if (obj == null)                  return false;
		if (getClass() != obj.getClass()) return false;
		
		TimesDiePool other = (TimesDiePool) obj;
	
		return Objects.equals(contained, other.contained) && numDice == other.numDice;
	}
}