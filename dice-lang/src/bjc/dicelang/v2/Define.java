package bjc.dicelang.v2;

import bjc.utils.data.CircularIterator;

import java.util.Iterator;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Define implements UnaryOperator<String> {
	public static enum Type {
		LINE, TOKEN
	}

	int priority;

	boolean doRecur;
	boolean subType;

	Pattern predicate;
	Pattern searcher;

	Iterator<String>  replacers;
	String replacer;

	public Define(int priorty, boolean isSub, boolean recur,
			String predicte, String searchr, Iterable<String> replacrs) {
		priority = priorty;
		doRecur  = recur;
		subType  = isSub;

		if(predicte != null) {
			predicate = Pattern.compile(predicte);
		}
		searcher  = Pattern.compile(searchr);

		if(subType) {
			if(replacrs.iterator().hasNext()) {
				replacers = new CircularIterator<>(replacrs);
			} else {
				replacers = null;
			}
		} else {
			Iterator<String> itr = replacrs.iterator();

			if(itr.hasNext()) replacer  = itr.next();
			else              replacer  = "";
		}
	}

	public String apply(String tok) {
		if(predicate != null) {
			if(!predicate.matcher(tok).matches()) {
				return tok;
			}
		}

		String strang = doPass(tok);

		if(doRecur) {
			if(strang.equals(tok)) {
				return strang;
			} else {
				String oldStrang = strang;

				do {
					strang = doPass(tok);
				} while(!strang.equals(oldStrang));
			}
		}

		return strang;
	}

	private String doPass(String tok) {
		Matcher searcherMatcher = searcher.matcher(tok);

		if(subType) {
			StringBuffer sb = new StringBuffer();

			while(searcherMatcher.find()) {
				if(replacers == null) searcherMatcher.appendReplacement(sb,"");
				else                  searcherMatcher.appendReplacement(sb, replacers.next());
			}
			
			searcherMatcher.appendTail(sb);

			return sb.toString();
		} else {
			return searcherMatcher.replaceAll(replacer);
		}
	}
}
