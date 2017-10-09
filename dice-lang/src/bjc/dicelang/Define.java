package bjc.dicelang;

import static bjc.dicelang.Errors.ErrorKey.EK_DFN_PREDSYN;
import static bjc.dicelang.Errors.ErrorKey.EK_DFN_RECUR;
import static bjc.dicelang.Errors.ErrorKey.EK_DFN_SRCSYN;

import java.util.Iterator;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import bjc.utils.data.CircularIterator;

/*
 * @TODO 10/09/17 Ben Culkin :DefineRefactor
 * 	Consider replacing this with the defines package from BJC-Utils.
 */
/**
 * A regular expression based pre-processor define.
 *
 * @author EVE
 *
 */
public class Define implements UnaryOperator<String>, Comparable<Define> {
	/**
	 * The define type.
	 *
	 * @author EVE
	 *
	 */
	public static enum Type {
		/** Match on lines. */
		LINE,
		/** Match on tokens. */
		TOKEN
	}

	/** The max amount of times to recur on expansions. */
	public static int MAX_RECURS = 10;

	/** The priority of this definition. */
	public final int        priority;

	/** Whether or not this definition is in error. */
	public final boolean    inError;

	/* Whether this define is recurring. */
	private boolean doRecur;
	/* Whether this define is applied multiple times per unit. */
	private boolean subType;

	/* The pattern that needs to match to apply this. */
	private Pattern predicate;
	/* The pattern to use to find everything to replace. */
	private Pattern searcher;

	/* The array of replacement strings to use. */
	private Iterator<String>        replacers;
	/* The current replacement string to use. */
	private String                  replacer;

	/**
	 * Create a new define.
	 *
	 * @param priorty
	 *                The priority of the define.
	 *
	 * @param isSub
	 *                Whether or not this is a 'sub-define'
	 *
	 * @param recur
	 *                Whether this define is recursive or not.
	 *
	 * @param isCircular
	 *                Whether this define is circular or not.
	 *
	 * @param predicte
	 *                The string to use as a predicate.
	 *
	 * @param searchr
	 *                The string to use as a search.
	 *
	 * @param replacrs
	 *                The source for replacement strings.
	 */
	public Define(final int priorty, final boolean isSub, final boolean recur,
	              final boolean isCircular,
	              final String predicte, final String searchr, final Iterable<String> replacrs) {
		priority = priorty;
		doRecur = recur;
		subType = isSub;

		/* Only try to compile non-null predicates */
		if (predicte != null) {
			try {
				predicate = Pattern.compile(predicte);
			} catch (final PatternSyntaxException psex) {
				Errors.inst.printError(EK_DFN_PREDSYN, psex.getMessage());
				inError = true;
				return;
			}
		}

		/* Compile the search pattern */
		try {
			searcher = Pattern.compile(searchr);
		} catch (final PatternSyntaxException psex) {
			Errors.inst.printError(EK_DFN_SRCSYN, psex.getMessage());
			inError = true;
			return;
		}

		inError = false;

		/* Check whether or not we do sub-replacements */
		if (subType) {
			if (replacrs.iterator().hasNext()) {
				replacers = new CircularIterator<>(replacrs, isCircular);
			} else {
				replacers = null;
			}
		} else {
			final Iterator<String> itr = replacrs.iterator();

			if (itr.hasNext()) {
				replacer = itr.next();
			} else {
				replacer = "";
			}
		}
	}

	@Override
	public String apply(final String tok) {
		if (inError) {
			return tok;
		}

		if (predicate != null) {
			if (!predicate.matcher(tok).matches()) {
				return tok;
			}
		}

		String strang = doPass(tok);

		if (doRecur) {
			int recurCount = 0;

			if (strang.equals(tok)) {
				return strang;
			}

			final String oldStrang = strang;

			do {
				strang = doPass(tok);
				recurCount += 1;
			} while (!strang.equals(oldStrang) && recurCount < MAX_RECURS);

			if (recurCount >= MAX_RECURS) {
				Errors.inst.printError(EK_DFN_RECUR, Integer.toString(MAX_RECURS), tok, strang);
				return strang;
			}
		}

		return strang;
	}

	/* Apply a definition pass. */
	private String doPass(final String tok) {
		final Matcher searcherMatcher = searcher.matcher(tok);

		if (subType) {
			final StringBuffer sb = new StringBuffer();

			while (searcherMatcher.find()) {
				if (replacers == null) {
					searcherMatcher.appendReplacement(sb, "");
				} else {
					final String replac = replacers.next();
					searcherMatcher.appendReplacement(sb, replac);
				}
			}

			searcherMatcher.appendTail(sb);
			return sb.toString();
		}

		return searcherMatcher.replaceAll(replacer);
	}

	@Override
	public int compareTo(final Define o) {
		return priority - o.priority;
	}
}
