package bjc.dicelang.v1;

import static bjc.dicelang.v1.DiceExpressionType.ADD;
import static bjc.dicelang.v1.DiceExpressionType.DIVIDE;
import static bjc.dicelang.v1.DiceExpressionType.MULTIPLY;
import static bjc.dicelang.v1.DiceExpressionType.SUBTRACT;

/**
 * Build a dice expression piece by piece
 *
 * @author ben
 *
 */
public class DiceExpressionBuilder {
	/*
	 * The dice expression we are building
	 */
	private IDiceExpression baking;

	/**
	 * Build a dice expression from a seed dice expression
	 *
	 * @param seed
	 *                The dice expression to use as a seed
	 */
	public DiceExpressionBuilder(IDiceExpression seed) {
		baking = seed;
	}

	/**
	 * Build a dice expression from a seed dice
	 *
	 * @param nSides
	 *                The number of sides in the dice
	 * @param nDice
	 *                The number of dice in the group
	 */
	public DiceExpressionBuilder(int nSides, int nDice) {
		baking = new ComplexDice(nSides, nDice);
	}

	/**
	 * Add a term to this dice expression
	 *
	 * @param exp
	 *                The expression to use on the left
	 * @return A new expression adding the two dice
	 */
	public DiceExpressionBuilder add(IDiceExpression exp) {
		baking = new OperatorDiceExpression(baking, exp, ADD);
		return this;
	}

	/**
	 * Add a scalar to this dice
	 *
	 * @param num
	 *                The scalar to add to the dice
	 * @return A dice expression adding a scalar to this
	 */
	public DiceExpressionBuilder add(int num) {
		baking = new OperatorDiceExpression(baking, new ScalarDie(num), ADD);
		return this;
	}

	/**
	 * Bake the expression being built to completion
	 *
	 * @return A usable dice expression
	 */
	public IDiceExpression bake() {
		return baking;
	}

	/**
	 * Divide a term from dice expression
	 *
	 * @param exp
	 *                The expression to use on the left
	 * @return A new expression dividing the two dice
	 */
	public DiceExpressionBuilder divide(IDiceExpression exp) {
		baking = new OperatorDiceExpression(baking, exp, DIVIDE);
		return this;
	}

	/**
	 * Divide a scalar from this dice
	 *
	 * @param num
	 *                The scalar to add to the dice
	 * @return A dice expression dividing a scalar from this
	 */
	public DiceExpressionBuilder divide(int num) {
		baking = new OperatorDiceExpression(baking, new ScalarDie(num), DIVIDE);
		return this;
	}

	/**
	 * Multiply a term by this dice expression
	 *
	 * @param exp
	 *                The expression to use on the left
	 * @return A new expression multiplying the two dice
	 */
	public DiceExpressionBuilder multiply(IDiceExpression exp) {
		baking = new OperatorDiceExpression(baking, exp, MULTIPLY);
		return this;
	}

	/**
	 * Multiply a scalar by this dice
	 *
	 * @param num
	 *                The scalar to multiply to the dice
	 * @return A dice expression multiplying a scalar to this
	 */
	public DiceExpressionBuilder multiply(int num) {
		baking = new OperatorDiceExpression(baking, new ScalarDie(num), MULTIPLY);
		return this;
	}

	/**
	 * Add a term to this dice expression
	 *
	 * @param exp
	 *                The expression to use on the left
	 * @return A new expression adding the two dice
	 */
	public DiceExpressionBuilder subtract(IDiceExpression exp) {
		baking = new OperatorDiceExpression(baking, exp, SUBTRACT);
		return this;
	}

	/**
	 * Add a scalar to this dice
	 *
	 * @param num
	 *                The scalar to add to the dice
	 * @return A dice expression adding a scalar to this
	 */
	public DiceExpressionBuilder subtract(int num) {
		baking = new OperatorDiceExpression(baking, new ScalarDie(num), SUBTRACT);
		return this;
	}
}
