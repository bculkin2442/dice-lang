package bjc.dicelang.scl;

import java.util.HashMap;
import java.util.Map;

import static bjc.dicelang.scl.WordSCLToken.Word.*;

public class WordSCLToken extends SCLToken {
	public static enum Word {
		/* Array manipulation */
		MAKEARRAY, MAKEEXEC, MAKEUNEXEC,
		/* Stream manipulation */
		NEWSTREAM, LEFTSTREAM, RIGHTSTREAM, DELETESTREAM, MERGESTREAM,
		/* Stack manipulation */
		STACKCOUNT, STACKEMPTY, DROP, NDROP, NIP, NNIP,
	}

	public Word wordVal;

	public WordSCLToken(String wrd) {
		this(builtinWords.get(wrd));
	}

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

	public static boolean isBuiltinWord(String wrd) {
		return builtinWords.containsKey(wrd);
	}

	private static final Map<String, WordSCLToken.Word> builtinWords;

	static {
		/* Init builtin words. */
		builtinWords = new HashMap<>();

		builtinWords.put("makearray", MAKEARRAY);
		builtinWords.put("+stream", NEWSTREAM);
		builtinWords.put(">stream", LEFTSTREAM);
		builtinWords.put("<stream", RIGHTSTREAM);
		builtinWords.put("-stream", DELETESTREAM);
		builtinWords.put("<-stream", MERGESTREAM);
		builtinWords.put("cvx", MAKEEXEC);
		builtinWords.put("cvux", MAKEUNEXEC);
		builtinWords.put("#", STACKCOUNT);
		builtinWords.put("empty?", STACKEMPTY);
		builtinWords.put("drop", DROP);
		builtinWords.put("ndrop", NDROP);
		builtinWords.put("nip", NIP);
		builtinWords.put("nnip", NNIP);
	}
}
