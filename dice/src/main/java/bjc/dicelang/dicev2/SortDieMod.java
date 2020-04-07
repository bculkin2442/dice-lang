package bjc.dicelang.dicev2;

import bjc.utils.funcutils.ListUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

public class SortDieMod extends Die {
	public final Die die;
	
	public Comparator<Long> sorter;

	public SortDieMod(Comparator<Long> sorter, Die die) {
		super();

		this.sorter = sorter;

		this.die = die;
	}

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

	public long rollSingle() {
		return die.rollSingle();
	}

	public boolean canOptimize() {
		return die.canOptimize();
	}

	public long optimize() {
		return die.optimize();
	}
}
