package bjc.dicelang.scl.tokens;

import static bjc.dicelang.scl.tokens.WordType.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a single word.
 * 
 * @author student
 *
 */
public class WordSCLToken extends SCLToken {
	/**
	 * The value of the word.
	 */
	public WordType wordVal;

	/**
	 * Create a new word token.
	 * 
	 * @param wrd
	 *            The value of the word.
	 */
	public WordSCLToken(String wrd) {
		this(builtinWords.get(wrd));
	}

	/**
	 * Create a new word token.
	 * 
	 * @param wrd
	 *            The value of the word.
	 */
	public WordSCLToken(WordType wrd) {
		super(TokenType.WORD);

		wordVal = wrd;
	}

	@Override
	public String toString() {
		return "WordSCLToken [wordVal=" + wordVal + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((wordVal == null) ? 0 : wordVal.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		WordSCLToken other = (WordSCLToken) obj;
		if (wordVal != other.wordVal)
			return false;
		return true;
	}

	/**
	 * Check if a word is built-in.
	 * 
	 * @param wrd
	 *            The word to check.
	 * 
	 * @return Whether or not the word is builtin.
	 */
	public static boolean isBuiltinWord(String wrd) {
		return builtinWords.containsKey(wrd);
	}

	private static final Map<String, WordType> builtinWords;

	static {
		/* Init builtin words. */
		builtinWords = new HashMap<>();

		builtinWords.put("makearray", MAKEARRAY);
		builtinWords.put("cvx", MAKEEXEC);
		builtinWords.put("cvux", MAKEUNEXEC);

		builtinWords.put("+stream", NEWSTREAM);
		builtinWords.put(">stream", LEFTSTREAM);
		builtinWords.put("<stream", RIGHTSTREAM);
		builtinWords.put("-stream", DELETESTREAM);
		builtinWords.put("<-stream", MERGESTREAM);

		builtinWords.put("#", STACKCOUNT);
		builtinWords.put("empty?", STACKEMPTY);
		builtinWords.put("drop", DROP);
		builtinWords.put("ndrop", NDROP);
		builtinWords.put("nip", NIP);
		builtinWords.put("nnip", NNIP);
	}
}
