package bjc.dicelang.neodice;

import java.util.*;

/**
 * Various static functions which create instances of Die.
 * 
 * @author Ben Culkin
 *
 */
public class DieFactory {
	/**
	 * Create a simple polyhedral die with a fixed number of sides.
	 * 
	 * @param sides The number of sides for the die.
	 * 
	 * @return A die which returns a result from 1 to sides.
	 */
	public static Die polyhedral(int sides) {
		return new PolyhedralDie(sides);
	}
}

final class PolyhedralDie implements Die {
	private final int sides;

	public PolyhedralDie(int sides) {
		this.sides = sides;
	}

	@Override
	public int roll(Random rng) {
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