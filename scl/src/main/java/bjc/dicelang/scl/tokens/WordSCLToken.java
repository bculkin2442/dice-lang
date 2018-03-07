package bjc.dicelang.scl.tokens;

import static bjc.dicelang.scl.tokens.WordSCLToken.Word.*;

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
	 * Represents the word type.
	 * 
	 * @author student
	 *
	 */
	public static enum Word {
		/* Array manipulation */
		/**
		 * Create an array
		 */
		MAKEARRAY,
		/**
		 * Make a token executable.
		 */
		MAKEEXEC,
		/**
		 * Make a token unexecutable.
		 */
		MAKEUNEXEC,

		/* Stream manipulation */
		/**
		 * Create a new stream.
		 */
		NEWSTREAM,
		/**
		 * Swap to the left stream.
		 */
		LEFTSTREAM,
		/**
		 * Swap to the right stream.
		 */
		RIGHTSTREAM,
		/**
		 * Delete the current stream.
		 */
		DELETESTREAM,
		/**
		 * Merge the streams.
		 */
		MERGESTREAM,

		/* Stack manipulation */
		/**
		 * Get the count of items on the stack.
		 */
		STACKCOUNT,
		/**
		 * Check if the stack is empty.
		 */
		STACKEMPTY,
		/**
		 * Drop an item from the top of the stack.
		 */
		DROP,
		/**
		 * Drop a number of items from the top of the stack.
		 */
		NDROP,
		/**
		 * Drop an item, leaving the top of the stack alone.
		 */
		NIP,
		/**
		 * Drop a number of items, leaving the top of the stack alone.
		 */
		NNIP,
	}

	/**
	 * The value of the word.
	 */
	public Word wordVal;

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
	public WordSCLToken(Word wrd) {
		super(Type.WORD);

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

	private static final Map<String, WordSCLToken.Word> builtinWords;

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
