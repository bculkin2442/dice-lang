package bjc.dicelang.scl;

public class IntSCLToken extends SCLToken {
	/* Used for ILIT */
	public long intVal;

	/* Create a new token. */
	public IntSCLToken(final Type typ) {
		super(typ);
	}

	/* Create a new token. */
	public IntSCLToken(final long iVal) {
		super(Type.ILIT);

		intVal = iVal;
	}
}