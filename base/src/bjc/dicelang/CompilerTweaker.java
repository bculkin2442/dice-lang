package bjc.dicelang;

import bjc.utils.parserutils.splitter.ConfigurableTokenSplitter;

/*
 * @TODO 10/09/17 Ben Culkin :CompilerTweaking
 * 
 * Expand this to allow tweaking more things about the compiler.
 */
/**
 * Contains methods for customizing the DiceLang and SCL compilers.
 *
 * @author Ben Culkin
 */
public class CompilerTweaker {
	/* Bits of the compiler necessary */
	private final DiceLangEngine eng;
	private final ConfigurableTokenSplitter opExpander;

	/**
	 * Create a new compiler tweaker.
	 *
	 * @param engine
	 *            The engine to tweak.
	 */
	public CompilerTweaker(final DiceLangEngine engine) {
		eng = engine;

		this.opExpander = engine.opExpander;
	}

	/**
	 * Add a string literal to the compiler's internal banks.
	 *
	 * @param val
	 *            The string literal to add.
	 *
	 * @return The key into the string literal table for this string.
	 */
	public int addStringLiteral(final String val) {
		eng.addStringLiteral(eng.nextLiteral, val);

		eng.nextLiteral += 1;
		return eng.nextLiteral;
	}

	/**
	 * Add a line defn to the compiler.
	 *
	 * @param dfn
	 *            The defn to add.
	 */
	public void addLineDefine(final Define dfn) {
		eng.addLineDefine(dfn);
	}

	/**
	 * Add a token defn to the compiler.
	 *
	 * @param dfn
	 *            The defn to add.
	 */
	public void addTokenDefine(final Define dfn) {
		eng.addTokenDefine(dfn);
	}

	/**
	 * Adds delimiters that are expanded from tokens.
	 *
	 * @param delims
	 *            The delimiters to expand on.
	 */
	public void addDelimiter(final String... delims) {
		opExpander.addSimpleDelimiters(delims);
	}

	/**
	 * Adds multi-character delimiters that are expanded from tokens.
	 *
	 * @param delims
	 *            The multi-character delimiters to expand on.
	 */
	public void addMultiDelimiter(final String... delims) {
		opExpander.addMultiDelimiters(delims);
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
	 *            The number of times to allow defines to recur.
	 */
	public static void setDefineRecurLimit(final int times) {
		Define.MAX_RECURS = times;
	}
}
