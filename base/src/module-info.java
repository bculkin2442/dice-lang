/**
 * A language for rolling dice.
 * 
 * @author bjcul
 *
 */
module dicelang.base {
	exports bjc.dicelang.cli;
	exports bjc.dicelang.eval;
	exports bjc.dicelang;
	exports bjc.dicelang.tokens;
	exports bjc.dicelang.expr;
	exports bjc.dicelang.util;

	requires transitive bjc.utils;
	requires transitive dicelang.dice;
	requires transitive dicelang.scl;
	requires transitive esodata;
	requires guava;
	requires java.logging;
	requires jline;
}