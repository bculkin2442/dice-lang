package bjc.dicelang.neodice.die;

import java.util.*;
import java.util.function.*;

import bjc.dicelang.neodice.*;
import bjc.esodata.*;

public class RerollDie<SideType> implements IDie<SideType> {
	private final IDie<SideType> contained;
	
	private final Predicate<SideType>                    condition;
	private final Function<MinMaxList<SideType>, SideType> chooser;
	
	private final Comparator<SideType> comparer;
	
	private int limit = Integer.MAX_VALUE;
	
	private RerollDie(
			Comparator<SideType> comparer,
			IDie<SideType> contained,
			Predicate<SideType> condition,
			Function<MinMaxList<SideType>, SideType> chooser) {
		this.comparer = comparer;
		
		this.contained = contained;
		
		this.condition = condition;
		this.chooser   = chooser;
	}
	
	private RerollDie(
			Comparator<SideType> comparer,
			IDie<SideType> contained,
			Predicate<SideType> condition,
			Function<MinMaxList<SideType>, SideType> chooser,
			int limit) {
		this(comparer, contained, condition, chooser);
		
		this.limit = limit;
	}
	
	@Override
	public SideType roll(Random rng) {
		SideType roll = contained.roll(rng);

		MinMaxList<SideType> newRolls = new MinMaxList<>(comparer, roll);
		
		int rerollCount = 0;
		while (condition.test(roll) && rerollCount < limit) {
			roll = contained.roll(rng);
			newRolls.add(roll);
			
			rerollCount += 1;
		}
		
		return chooser.apply(newRolls);
	}

	public static <Side extends Comparable<Side>> IDie<Side> create(
			IDie<Side> contained,
			Predicate<Side> condition,
			Function<MinMaxList<Side>, Side> chooser) {
		return new RerollDie<>(Comparator.naturalOrder(), contained, condition, chooser);
	}
	
	public static <Side extends Comparable<Side>> IDie<Side> create(
			IDie<Side> contained,
			Predicate<Side> condition,
			Function<MinMaxList<Side>, Side> chooser,
			int limit) {
		return new RerollDie<>(Comparator.naturalOrder(), contained, condition, chooser, limit);
	}
	

	public static <Side> IDie<Side> create(
			Comparator<Side> comparer,
			IDie<Side> contained,
			Predicate<Side> condition,
			Function<MinMaxList<Side>, Side> chooser) {
		return new RerollDie<Side>(comparer, contained, condition, chooser);
	}
	
	public static <Side> IDie<Side> create(
			Comparator<Side> comparer,
			IDie<Side> contained,
			Predicate<Side> condition,
			Function<MinMaxList<Side>, Side> chooser,
			int limit) {
		return new RerollDie<Side>(comparer, contained, condition, chooser, limit);
	}
}