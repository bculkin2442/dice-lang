package bjc.dicelang.neodice.diepool;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import bjc.dicelang.neodice.*;

public class ExpandDiePool<SideType> implements IDiePool<SideType> {
	private final IDie<SideType> contained;
	
	private final BiFunction<IDie<SideType>, Random, Stream<SideType>> expander;

	public ExpandDiePool(IDie<SideType> contained,
			BiFunction<IDie<SideType>, Random, Stream<SideType>> expander) {
		this.contained = contained;
		this.expander = expander;
	}


	@Override
	public Stream<SideType> roll(Random rng) {
		return expander.apply(contained, rng);
	}
}
