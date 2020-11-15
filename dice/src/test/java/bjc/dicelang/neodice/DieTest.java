package bjc.dicelang.neodice;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

@SuppressWarnings("javadoc")
public class DieTest {
	private final static Random rng = new Random();

	@Test
	public void onesidedDiceReturnOne() {
		Die die = DieFactory.polyhedral(1);
		
		for (int i = 0; i < 10; i++) {
			assertEquals("One-sided dice always return 1", 1, die.roll(rng));
		}
	}
	
	@Test
	public void polyhedralDiceStayInRange() {
		Die die = DieFactory.polyhedral(6);
		
		for (int i = 0; i < 50; i++) {
			int result = die.roll(rng);
			
			boolean inRange = result <= 6 && result >= 1;
			
			assertTrue("Six-sided dice always return a value from 1 to 6", inRange);
		}
	}
	
	public void polyhedralDiceEqualityFunctionsProperly() {
		Die dieA1 = DieFactory.polyhedral(1);
		Die dieA2 = DieFactory.polyhedral(1);
		Die dieB1 = DieFactory.polyhedral(2);
		
		assertEquals("Polyhedral dice with the same number of sides are equal",
				dieA1, dieA2);
		assertNotEquals("Polyhedral dice with a diffeent number of sides aren't equal",
				dieA1, dieB1);
	}
}
