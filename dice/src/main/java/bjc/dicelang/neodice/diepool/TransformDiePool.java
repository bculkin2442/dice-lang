package bjc.dicelang.neodice.diepool;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import bjc.dicelang.neodice.*;

public class TransformDiePool<SideType> implements DiePool<SideType> {
	private final DiePool<SideType> contained;
	
	private UnaryOperator<Stream<SideType>> transform;

	public TransformDiePool(DiePool<SideType> contained,
			UnaryOperator<Stream<SideType>> transform) {
		super();
		this.contained = contained;
		this.transform = transform;
	}

	@Override
	public Stream<SideType> roll(Random rng) {
		return transform.apply(contained.roll(rng));
	}
	
	@Override
	public List<Die<SideType>> contained() {
		return contained.contained();
	}

	@Override
	public int hashCode() {
		return Objects.hash(contained, transform);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)                  return true;
		if (obj == null)                  return false;
		if (getClass() != obj.getClass()) return false;
	
		TransformDiePool<?> other = (TransformDiePool<?>) obj;
		
		return Objects.equals(contained, other.contained)
				&& Objects.equals(transform, other.transform);
	}
	
	@Override
	public String toString() {
		return contained.toString() + "(transformed)";
	}
}
