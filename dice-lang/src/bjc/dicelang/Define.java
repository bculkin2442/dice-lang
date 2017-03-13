package bjc.dicelang;

import bjc.utils.data.CircularIterator;

import static bjc.dicelang.Errors.ErrorKey.*;

import java.util.Iterator;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Define implements UnaryOperator<String>, Comparable<Define> {
	public static enum Type {
		LINE, TOKEN
	}

	public static int MAX_RECURS = 10;

	public final int priority;
	public final boolean inError;

	private boolean doRecur;
	private boolean subType;

	private Pattern predicate;
	private Pattern searcher;

	private Iterator<String> replacers;
	private String replacer;

	public Define(int priorty, boolean isSub, boolean recur, boolean isCircular, String predicte, String searchr,
			Iterable<String> replacrs) {
		priority = priorty;
		doRecur = recur;
		subType = isSub;

		/*
		 * Only try to compile non-null predicates
		 */
		if (predicte != null) {
			try {
				predicate = Pattern.compile(predicte);
			} catch (PatternSyntaxException psex) {
				Errors.inst.printError(EK_DFN_PREDSYN, psex.getMessage());
				inError = true;
				return;
			}
		}

		/*
		 * Compile the search pattern
		 */
		try {
			searcher = Pattern.compile(searchr);
		} catch (PatternSyntaxException psex) {
			Errors.inst.printError(EK_DFN_SRCSYN, psex.getMessage());
			inError = true;
			return;
		}

		inError = false;
		/*
		 * Check whether or not we do sub-replacements
		 */
		if (subType) {
			if (replacrs.iterator().hasNext()) {
				replacers = new CircularIterator<>(replacrs, isCircular);
			} else {
				replacers = null;
			}
		} else {
			Iterator<String> itr = replacrs.iterator();

			if (itr.hasNext())
				replacer = itr.next();
			else
				replacer = "";
		}
	}

	public String apply(String tok) {
		if (inError)
			return tok;

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
			} else {
				String oldStrang = strang;

				do {
					strang = doPass(tok);
					recurCount += 1;
				} while (!strang.equals(oldStrang) && recurCount < MAX_RECURS);

				if (recurCount >= MAX_RECURS) {
					Errors.inst.printError(EK_DFN_RECUR, Integer.toString(MAX_RECURS), tok, strang);
					return strang;
				}
			}
		}

		return strang;
	}

	private String doPass(String tok) {
		Matcher searcherMatcher = searcher.matcher(tok);

		if (subType) {
			StringBuffer sb = new StringBuffer();
			while (searcherMatcher.find()) {
				if (replacers == null) {
					searcherMatcher.appendReplacement(sb, "");
				} else {
					String replac = replacers.next();
					searcherMatcher.appendReplacement(sb, replac);
				}
			}

			searcherMatcher.appendTail(sb);
			return sb.toString();
		} else {
			return searcherMatcher.replaceAll(replacer);
		}
	}

	@Override
	public int compareTo(Define o) {
		return priority - o.priority;
	}
}
