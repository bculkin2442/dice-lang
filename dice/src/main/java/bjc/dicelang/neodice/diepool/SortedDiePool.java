package bjc.dicelang.neodice.diepool;

import java.util.*;

import bjc.dicelang.neodice.*;

public class SortedDiePool implements DiePool {
	private final boolean isDescending;
	private final DiePool pool;

	public SortedDiePool(DiePool pool, boolean isDescending) {
		this.pool = pool;
		this.isDescending = isDescending;
	}

	@Override
	public int[] roll(Random rng) {
		int[] rolls = pool.roll(rng);
		
		Arrays.sort(rolls);
	
		if (isDescending) {
			int[] newRolls = new int[rolls.length];
			
			int newIndex = newRolls.length;
			for (int index = 0; index < rolls.length; index++) {
				newRolls[newIndex--] = rolls[index];
			}
			
			return newRolls;
		} else {
			return rolls;
		}
	}
	
	@Override
	public Die[] contained() {
		return pool.contained();
	}

	@Override
	public String toString() {
		return String.format("%s (sorted %s)", pool, 
				isDescending ? " descending" : "ascending");
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(isDescending, pool);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)                  return true;
		if (obj == null)                  return false;
		if (getClass() != obj.getClass()) return false;
		
		SortedDiePool other = (SortedDiePool) obj;
	
		return isDescending == other.isDescending
			   && Objects.equals(pool, other.pool);
	}
}