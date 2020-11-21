package bjc.dicelang.neodice.diepool;

import java.util.*;

import bjc.dicelang.neodice.*;

public class DropFirstPool implements DiePool {
	private final int     number;
	private final DiePool pool;
	
	public DropFirstPool(DiePool pool, int number) {
		this.pool   = pool;
		this.number = number;
	}

	@Override
	public int[] roll(Random rng) {
		int[] rolls = pool.roll(rng);
		
		if (number >= rolls.length) {
			return new int[0];
		} else {
			int[] newRolls = new int[rolls.length - number];
			
			for (int index = number - 1; index < rolls.length; index++) {
				newRolls[index - number] = rolls[index];
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
		return String.format("%sdF%d", pool, number);
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
		
		DropFirstPool other = (DropFirstPool) obj;
	
		return number == other.number && Objects.equals(pool, other.pool);
	}
}