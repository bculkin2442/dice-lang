package bjc.dicelang.neodice;

import java.util.*;

/**
 * Various static functions which create instances of DiePool.
 * 
 * @author Ben Culkin
 *
 */
public class DiePoolFactory {
	/**
	 * Create a die pool containing the provided dice.
	 * 
	 * @param dice The dice to put into the pool.
	 * 
	 * @return A pool which contains the provided dice.
	 */
	public static DiePool containing(Die... dice) {
		return new FixedDiePool(dice);
	}
}

final class FixedDiePool implements DiePool {
	private final Die[] dice;

	public FixedDiePool(Die[] dice) {
		this.dice = dice;
	}

	@Override
	public int[] roll(Random rng) {
		int[] results = new int[dice.length];
		
		for (int index = 0; index < dice.length; index++) {
			results[index] = dice[index].roll(rng);
		}
		
		return results;
	}

	@Override
	public Die[] contained() {
		return dice;
	}

	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		for (int i = 0; i < dice.length; i++) {
			Die die = dice[i];
			
			builder.append(die);
			
			// Don't add an extra trailing comma
			if (i < dice.length - 1) builder.append(", ");
		}
		
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(dice);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)                  return true;
		if (obj == null)                  return false;
		if (getClass() != obj.getClass()) return false;
		
		FixedDiePool other = (FixedDiePool) obj;
		
		return Arrays.equals(dice, other.dice);
	}
}