package bjc.dicelang.neodice.die;

import java.util.*;
import java.util.function.*;

import bjc.dicelang.neodice.*;
import bjc.esodata.*;

public class RerollDie implements Die {
	private final Die          contained;
	
	private final IntPredicate                           condition;
	private final Function<MinMaxList<Integer>, Integer> chooser;
	
	private int limit = Integer.MAX_VALUE;
	
	
	public RerollDie(Die contained, IntPredicate condition,
			Function<MinMaxList<Integer>, Integer> chooser) {
		this.contained = contained;
		
		this.condition = condition;
		this.chooser   = chooser;
	}
	
	public RerollDie(Die contained, IntPredicate condition,
			Function<MinMaxList<Integer>, Integer> chooser, int limit) {
		this.contained = contained;
		
		this.condition = condition;
		this.chooser   = chooser;
		
		this.limit = limit;
	}
	
	@Override
	public int roll(Random rng) {
		int roll = contained.roll(rng);
		
		MinMaxList<Integer> newRolls = new MinMaxList<Integer>(
				Comparator.naturalOrder(), roll);
		
		int rerollCount = 0;
		while (condition.test(roll) && rerollCount < limit) {
			roll = contained.roll(rng);
			newRolls.add(roll);
			
			rerollCount += 1;
		}
		
		return chooser.apply(newRolls);
	}

	// No toString, because IntPredicate can't be converted to a string
}