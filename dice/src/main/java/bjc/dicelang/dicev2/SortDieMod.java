package bjc.dicelang.dicev2;

import bjc.utils.funcutils.ListUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

/**
 * Die mod which sorts its results.
 * @author Ben Culkin
 *
 */
public class SortDieMod extends Die {
	/**
	 * Die to sort.
	 */
	public final Die die;
	
	/**
	 * Sorter to use.
	 */
	public Comparator<Long> sorter;

	/**
	 * Create a new sorting die mod.
	 * 
	 * @param sorter Sorter to use.
	 * @param die Die to sort.
	 */
	public SortDieMod(Comparator<Long> sorter, Die die) {
		super();

		this.sorter = sorter;

		this.die = die;
	}

	@Override
	public long[] roll() {
		/*
		 * @NOTE
		 *
		 * This is likely quite a bit slower than using Arrays.sort, but
		 * that only sorts in ascending numeric order. If this ends up
		 * being a performance issue, add another sort that does that.
		 */
		List<Long> lst = new ArrayList<>();

		for(long val : die.roll()) {
			lst.add(val);
		}

		lst.sort(sorter);
		
		return ListUtils.toPrimitive(lst);
	}

	@Override
	public long rollSingle() {
		return die.rollSingle();
	}

	@Override
	public boolean canOptimize() {
		return die.canOptimize();
	}

	@Override
	public long optimize() {
		return die.optimize();
	}
}
