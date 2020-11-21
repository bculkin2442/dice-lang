package bjc.dicelang.neodice;

public class DieBoxException extends RuntimeException {
	private static final long serialVersionUID = 1851356458656622896L;

	public DieBoxException() {
		super();
	}
	
	public DieBoxException(String message) {
		super(message);
	}
	
	public DieBoxException(String format, Object... args) {
		super(String.format(format, args));
	}
	
	public DieBoxException(Throwable cause) {
		super(cause);
	}
	
	public DieBoxException(Throwable cause, String message) {
		super(message, cause);
	}
	
	public DieBoxException(Throwable cause, String format, Object... args) {
		super(String.format(format, args), cause);
	}
}