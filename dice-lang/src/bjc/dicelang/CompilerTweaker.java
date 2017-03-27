package bjc.dicelang;

import bjc.utils.parserutils.splitter.SimpleTokenSplitter;

/**
 * Contains methods for customizing the DiceLang and SCL compilers.
 *
 * @author Ben Culkin
 */
public class CompilerTweaker {
	/*
	 * Bits of the compiler necessary
	 */
	private DiceLangEngine		eng;
	private SimpleTokenSplitter	opExpander;

	public CompilerTweaker(DiceLangEngine eng) {
		this.eng = eng;

		this.opExpander = eng.opExpander;
	}

	/**
	 * Add a string literal to the compiler's internal banks.
	 *
	 * @param val
	 *                The string literal to add.
	 *
	 * @return The key into the string literal table for this string.
	 */
	public int addStringLiteral(String val) {
		eng.addStringLiteral(eng.nextLiteral, val);

		eng.nextLiteral += 1;
		return eng.nextLiteral;
	}

	/**
	 * Add a line defn to the compiler.
	 *
	 * @param dfn
	 *                The defn to add.
	 */
	public void addLineDefine(Define dfn) {
		eng.addLineDefine(dfn);
	}

	/**
	 * Add a token defn to the compiler.
	 *
	 * @param dfn
	 *                The defn to add.
	 */
	public void addTokenDefine(Define dfn) {
		eng.addTokenDefine(dfn);
	}

	/**
	 * Adds a delimiter that is expanded from tokens.
	 *
	 * @param delim
	 *                The delimiter to expand on.
	 */
	public void addDelimiter(String delim) {
		opExpander.addDelimiter(delim);
	}

	/**
	 * Adds a multi-character delimiter that is expanded from tokens.
	 *
	 * @param delim
	 *                The multi-character delimiter to expand on.
	 */
	public void addMultiDelimiter(String delim) {
		opExpander.addMultiDelimiter(delim);
	}

	/**
	 * Make delimiter changes visible to the compiler.
	 */
	public void compile() {
		opExpander.compile();
	}

	/**
	 * Change the max no. of times defines are allowed to recur.
	 *
	 * @param times
	 *                The number of times to allow defines to recur.
	 */
	public static void setDefineRecurLimit(int times) {
		Define.MAX_RECURS = times;
	}
}
