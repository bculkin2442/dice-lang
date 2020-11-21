package bjc.dicelang.neodice.diepool;

import java.util.*;

import bjc.dicelang.neodice.*;

public class KeepFirstDiePool implements DiePool {
	private final int     number;
	private final DiePool pool;
	
	public KeepFirstDiePool(DiePool pool, int number) {
		this.pool   = pool;
		this.number = number;
	}

	@Override
	public int[] roll(Random rng) {
		int[] rolls = pool.roll(rng);
		
		if (rolls.length >= number) {
			return rolls;
		} else {
			int[] newRolls = new int[number];
			
			for (int index = 0; index < number; index++) {
				newRolls[index] = rolls[index];
			}
			
			return newRolls;
		}
	}
	
	@Override
	public Die[] contained() {
		return pool.contained();
	}
	
	@Override
	public String toString() {
		return String.format("%skF%d", pool, number);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(number, pool);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)                  return true;
		if (obj == null)                  return false;
		if (getClass() != obj.getClass()) return false;
		
		KeepFirstDiePool other = (KeepFirstDiePool) obj;
		
		return number == other.number && Objects.equals(pool, other.pool);
	}
}