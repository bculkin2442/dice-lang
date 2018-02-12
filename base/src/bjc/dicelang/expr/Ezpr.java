/*
 * @TODO 10/08/17 Ben Culkin :EzprFixing Implement these, and make sure they
 * work correctly.
 */
package bjc.dicelang.expr;

import static bjc.dicelang.expr.Ezpr.EzprType.*;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import static bjc.dicelang.expr.Ezpr.EzprNode.EzprNodeType.*;

public class Ezpr {
	public static enum EzprType {
		SUM, MUL
	}

	public static class EzprNode {
		public static enum EzprNodeType {
			EZPR, TOKEN
		}

		public final EzprNodeType typ;

		public final Ezpr ezp;
		public final Token tok;

		public EzprNode(Ezpr exp) {
			typ = EZPR;

			ezp = exp;
			tok = null;
		}

		public EzprNode(Token tk) {
			typ = TOKEN;

			tok = tk;
			ezp = null;
		}

		@Override
		public String toString() {
			if (typ == TOKEN) {
				return tok.toString();
			}
			return ezp.toString();
		}
	}

	private EzprType typ;

	private Multiset<EzprNode> positive;
	private Multiset<EzprNode> negative;

	public Ezpr(EzprType type, Multiset<EzprNode> pos, Multiset<EzprNode> neg) {
		typ = type;

		positive = pos;
		negative = neg;
	}

	public Ezpr flatten() {
		HashMultiset<EzprNode> newPositive = HashMultiset.create();
		HashMultiset<EzprNode> newNegative = HashMultiset.create();

		for (EzprNode nd : positive) {
			/* Flatten enclosed ezprs of the same type. */
			if (nd.typ == EZPR && (nd.ezp.typ == typ)) {
				/* Recursively flatten kids. */
				Ezpr kid = nd.ezp.flatten();

				if (typ == SUM) {
					/* Add sum parts to corresponding bags. */
					for (EzprNode knd : kid.positive) {
						newPositive.add(knd);
					}
					for (EzprNode knd : kid.negative) {
						newNegative.add(knd);
					}
				} else {
					/* @TODO ensure that this is correct. */
					for (EzprNode knd : kid.positive) {
						newPositive.add(knd);
					}
					for (EzprNode knd : kid.negative) {
						newNegative.add(knd);
					}
				}
			} else {
				newPositive.add(nd);
			}
		}

		for (EzprNode nd : negative) {
			/* Flatten enclosed ezprs of the same type. */
			if (nd.typ == EZPR && (nd.ezp.typ == typ)) {
				/* Recursively flatten kids. */
				Ezpr kid = nd.ezp.flatten();

				/* @TODO ensure that this is correct. */
				if (typ == SUM) {
					for (EzprNode knd : kid.positive) {
						newNegative.add(knd);
					}
					for (EzprNode knd : kid.negative) {
						newPositive.add(knd);
					}
				} else {
					for (EzprNode knd : kid.positive) {
						newNegative.add(knd);
					}
					for (EzprNode knd : kid.negative) {
						newPositive.add(knd);
					}
				}
			} else {
				newNegative.add(nd);
			}
		}

		return new Ezpr(typ, newPositive, newNegative);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(typ.toString());

		sb.append(" [ ");
		for (EzprNode nd : positive) {
			sb.append(nd.toString());
			sb.append(" ");
		}

		sb.append("# ");
		for (EzprNode nd : negative) {
			sb.append(nd.toString());
			sb.append(" ");
		}

		sb.append("]");

		return sb.toString();
	}
}
