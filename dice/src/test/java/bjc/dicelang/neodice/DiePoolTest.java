package bjc.dicelang.neodice;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

@SuppressWarnings("javadoc")
public class DiePoolTest {
	private static final Random rng = new Random();
	
	@Test
	public void containedOneDieYieldsOneDie() {
		Die     oneSidedDie = DieFactory.polyhedral(1);
		DiePool pool        = DiePoolFactory.containing(oneSidedDie);
		
		assertArrayEquals(
				"A contained pool created with one die, yields that die",
				new Die[] {oneSidedDie}, pool.contained());
	}
	
	@Test
	public void containedOneDieRollsOneDie() {
		Die     oneSidedDie = DieFactory.polyhedral(1);
		DiePool pool        = DiePoolFactory.containing(oneSidedDie);
		
		for (int i = 0; i < 10; i++) {
			assertArrayEquals("One-die pools roll one die",
					new int[] {1}, pool.roll(rng));
		}
	}
}
