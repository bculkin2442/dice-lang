package bjc.dicelang.neodice.diepool;

import java.util.*;
import java.util.function.*;

import bjc.dicelang.neodice.*;

public class FilteredDiePool implements DiePool {
	private final DiePool pool;
	private final IntPredicate filter;
	
	public FilteredDiePool(DiePool pool, IntPredicate filter) {
		this.pool = pool;
		this.filter = filter;
	}
	
	@Override
	public int[] roll(Random rng) {
		int[] rolls = pool.roll(rng);
		
		return Arrays.stream(rolls).filter(filter).toArray();
	}

	@Override
	public Die[] contained() {
		return pool.contained();
	}
	
	// No toString, since there isn't any sensible to output the filter
	
	@Override
	public int hashCode() {
		return Objects.hash(filter, pool);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)                  return true;
		if (obj == null)                  return false;
		if (getClass() != obj.getClass()) return false;
		
		FilteredDiePool other = (FilteredDiePool) obj;
		
		return Objects.equals(filter, other.filter) 
			   && Objects.equals(pool, other.pool);
	}
}