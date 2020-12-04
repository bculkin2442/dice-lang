package bjc.dicelang.neodice.diepool;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import bjc.dicelang.neodice.*;

public class ExpandDiePool<SideType> implements DiePool<SideType> {
	private final Die<SideType> contained;
	
	private final BiFunction<Die<SideType>, Random, Stream<SideType>> expander;

	public ExpandDiePool(Die<SideType> contained,
			BiFunction<Die<SideType>, Random, Stream<SideType>> expander) {
		this.contained = contained;
		this.expander = expander;
	}


	@Override
	public Stream<SideType> roll(Random rng) {
		return expander.apply(contained, rng);
	}
}
