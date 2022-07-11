module dicelang.base {
	exports bjc.dicelang.cli;
	exports bjc.dicelang.eval;
	exports bjc.dicelang;
	exports bjc.dicelang.tokens;
	exports bjc.dicelang.expr;
	exports bjc.dicelang.util;

	requires bjc.utils;
	requires dicelang.dice;
	requires dicelang.scl;
	requires esodata;
	requires guava;
	requires java.logging;
	requires jline;
}