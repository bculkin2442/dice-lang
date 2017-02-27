package bjc.dicelang.v2.dice;

public class MathDie implements Die {
	public static enum MathOp {
		ADD, SUBTRACT, MULTIPLY;

		public String toString() {
			switch(this) {
			case ADD:
				return "+";
			case SUBTRACT:
				return "-";
			case MULTIPLY:
				return "*";
			default:
				return this.name();
			}
		}
	}

	private MathDie.MathOp type;

	private Die left;
	private Die right;

	public MathDie(MathDie.MathOp op, Die lft, Die rght) {
		type = op;

		left  = lft;
		right = rght;
	}

	public boolean canOptimize() {
		return left.canOptimize() && right.canOptimize();
	}

	private long performOp(long lft, long rght) {
		switch(type) {
		case ADD:
			return lft + rght;
		case SUBTRACT:
			return lft - rght;
		case MULTIPLY:
			return lft * rght;
		default:
			return 0;
		}
	}

	public long optimize() {
		long lft  = left.optimize();
		long rght = right.optimize();

		return performOp(lft, rght);
	}

	public long roll() {
		long lft  = left.roll();
		long rght = right.roll();

		return performOp(lft, rght);
	}

	public long rollSingle() {
		long lft  = left.rollSingle();
		long rght = right.rollSingle();

		return performOp(lft, rght);
	}

	public String toString() {
		return left.toString() + " " + type.toString() + " " + right.toString();
	}
}