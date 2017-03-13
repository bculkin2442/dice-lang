package bjc.dicelang.v1;

/**
 * Utility class that produces common polyhedral dice
 * 
 * @author ben
 *
 */
public class PolyhedralDice {
	/**
	 * Produce a single d10
	 * 
	 * @return A single d10
	 */
	public static IDiceExpression d10() {
		return d10(1);
	}

	/**
	 * Produce the specified number of 10-sided dice
	 * 
	 * @param nDice
	 *                The number of ten-sided dice to produce
	 * @return A group of ten-sided dice of the specified size
	 */
	public static IDiceExpression d10(int nDice) {
		return new ComplexDice(nDice, 10);
	}

	/**
	 * Produce a single d100
	 * 
	 * @return A single d100
	 */
	public static IDiceExpression d100() {
		return d100(1);
	}

	/**
	 * Produce the specified number of 100-sided dice
	 * 
	 * @param nDice
	 *                The number of hundred-sided dice to produce
	 * @return A group of hundred-sided dice of the specified size
	 */
	public static IDiceExpression d100(int nDice) {
		return new ComplexDice(nDice, 100);
	}

	/**
	 * Produce a single d12
	 * 
	 * @return A single d12
	 */
	public static IDiceExpression d12() {
		return d12(1);
	}

	/**
	 * Produce the specified number of 12-sided dice
	 * 
	 * @param nDice
	 *                The number of twelve-sided dice to produce
	 * @return A group of twelve-sided dice of the specified size
	 */
	public static IDiceExpression d12(int nDice) {
		return new ComplexDice(nDice, 12);
	}

	/**
	 * Produce a single d20
	 * 
	 * @return A single d20
	 */
	public static IDiceExpression d20() {
		return d20(1);
	}

	/**
	 * Produce the specified number of 20-sided dice
	 * 
	 * @param nDice
	 *                The number of twenty-sided dice to produce
	 * @return A group of twenty-sided dice of the specified size
	 */
	public static IDiceExpression d20(int nDice) {
		return new ComplexDice(nDice, 20);
	}

	/**
	 * Produce a single d4
	 * 
	 * @return A single d4
	 */
	public static IDiceExpression d4() {
		return d4(1);
	}

	/**
	 * Produce the specified number of 4-sided dice
	 * 
	 * @param nDice
	 *                The number of four-sided dice to produce
	 * @return A group of four-sided dice of the specified size
	 */
	public static IDiceExpression d4(int nDice) {
		return new ComplexDice(nDice, 4);
	}

	/**
	 * Produce a single d6
	 * 
	 * @return A single d6
	 */
	public static IDiceExpression d6() {
		return d6(1);
	}

	/**
	 * Produce the specified number of 6-sided dice
	 * 
	 * @param nDice
	 *                The number of six-sided dice to produce
	 * @return A group of six-sided dice of the specified size
	 */
	public static IDiceExpression d6(int nDice) {
		return new ComplexDice(nDice, 6);
	}

	/**
	 * Produce a single d8
	 * 
	 * @return A single d8
	 */
	public static IDiceExpression d8() {
		return d8(1);
	}

	/**
	 * Produce the specified number of 8-sided dice
	 * 
	 * @param nDice
	 *                The number of eight-sided dice to produce
	 * @return A group of eight-sided dice of the specified size
	 */
	public static IDiceExpression d8(int nDice) {
		return new ComplexDice(nDice, 8);
	}
}
