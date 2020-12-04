package bjc.dicelang.neodice.die;

import java.util.*;

import bjc.dicelang.neodice.*;

public class PolyhedralDie implements Die<Integer> {
	private final int sides;

	public PolyhedralDie(int sides) {
		this.sides = sides;
	}

	@Override
	public Integer roll(Random rng) {
		// Dice are one-based, not zero-based.
		return rng.nextInt(sides) + 1;
	}

	@Override
	public String toString() {
		return String.format("d%d", sides);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + sides;		
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)                  return true;
		if (obj == null)                  return false;
		if (getClass() != obj.getClass()) return false;
		
		PolyhedralDie other = (PolyhedralDie) obj;
		
		if (sides != other.sides) return false;
		else                      return true;
	}
	
	
}