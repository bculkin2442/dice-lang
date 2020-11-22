package bjc.dicelang.neodice.diepool;

import java.util.*;
import java.util.stream.*;

import bjc.dicelang.neodice.*;

public class FixedDiePool<SideType> implements IDiePool<SideType> {
	private final List<IDie<SideType>> dice;

	public FixedDiePool(List<IDie<SideType>> dice) {
		this.dice = dice;
	}
	
	@SafeVarargs
	public FixedDiePool(IDie<SideType>...dice) {
		this.dice = new ArrayList<>(dice.length);
		for (IDie<SideType> die : dice) {
			this.dice.add(die);
		}
	}

	@Override
	public Stream<SideType> roll(Random rng) {
		return dice.stream().map((die) -> die.roll(rng));
	}

	@Override
	public List<IDie<SideType>> contained() {
		return dice;
	}

	
	@Override
	public String toString() {
		return dice.stream()
			.map(IDie<SideType>::toString)
			.collect(Collectors.joining(", "));
	}

	@Override
	public int hashCode() {
		return Objects.hash(dice);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)                  return true;
		if (obj == null)                  return false;
		if (getClass() != obj.getClass()) return false;
	
		FixedDiePool<?> other = (FixedDiePool<?>) obj;
		
		return Objects.equals(dice, other.dice);
	}
}