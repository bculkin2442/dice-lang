package bjc.dicelang.scl;

public class WordSCLToken extends SCLToken {
	public Word wordVal;
	
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
}
