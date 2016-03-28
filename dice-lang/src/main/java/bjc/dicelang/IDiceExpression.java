package bjc.dicelang;

/**
 * An expression for something that can be rolled like a polyhedral die
 * 
 * @author ben
 *
 */
@FunctionalInterface
public interface IDiceExpression {
	/**
	 * Roll the dice once
	 * 
	 * @return The result of rowing the dice
	 */
	public int roll();
}
