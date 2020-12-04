package bjc.dicelang.neodice.diepool;

import java.util.*;
import java.util.stream.*;

import bjc.dicelang.neodice.*;

public class TimesDiePool<SideType> implements DiePool<SideType> {
	private final Die<SideType> contained;
	private final int numDice;

	public TimesDiePool(Die<SideType> contained, int numDice) {
		this.contained = contained;
		this.numDice = numDice;
	}

	@Override
	public Stream<SideType> roll(Random rng) {
		return Stream.generate(() -> contained.roll(rng))
			.limit(numDice);
	}
	
	@Override
	public List<Die<SideType>> contained() {
		List<Die<SideType>> results = new ArrayList<>(numDice);
		
		for (int index = 0; index < numDice; index++) {
			results.add(contained);
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
		
		TimesDiePool<?> other = (TimesDiePool<?>) obj;
	
		return Objects.equals(contained, other.contained) && numDice == other.numDice;
	}
}