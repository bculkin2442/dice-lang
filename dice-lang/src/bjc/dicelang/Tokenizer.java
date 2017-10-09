package bjc.dicelang;

import static bjc.dicelang.Errors.ErrorKey.EK_TOK_INVBASE;
import static bjc.dicelang.Errors.ErrorKey.EK_TOK_INVFLEX;
import static bjc.dicelang.Errors.ErrorKey.EK_TOK_UNGROUP;
import static bjc.dicelang.Token.Type.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bjc.dicelang.dice.DiceBox;
import bjc.utils.funcdata.FunctionalMap;
import bjc.utils.funcdata.IMap;
import bjc.utils.funcutils.StringUtils;
import bjc.utils.parserutils.TokenUtils;

/**
 * Converts strings into tokens.
 */
public class Tokenizer {
	/* Literal tokens for tokenization */
	private final IMap<String, Token.Type> litTokens;

	private final DiceLangEngine eng;

	private int nextSym = 0;

	public Tokenizer(final DiceLangEngine engine) {
		eng = engine;

		litTokens = new FunctionalMap<>();

		litTokens.put("+",   ADD);
		litTokens.put("-",   SUBTRACT);
		litTokens.put("*",   MULTIPLY);
		litTokens.put("/",   DIVIDE);
		litTokens.put("//",  IDIVIDE);
		litTokens.put("sd",  DICESCALAR);
		litTokens.put("df",  DICEFUDGE);
		litTokens.put("dg",  DICEGROUP);
		litTokens.put("dc",  DICECONCAT);
		litTokens.put("dl",  DICELIST);
		litTokens.put("=>",  LET);
		litTokens.put(":=",  BIND);
		litTokens.put(".+.", STRCAT);
		litTokens.put(".*.", STRREP);
		litTokens.put(",",   GROUPSEP);
		litTokens.put("crc", COERCE);
	}

	public Token lexToken(final String token, final IMap<String, String> stringLts) {
		if (token.equals("")) {
			return null;
		}

		Token tk = Token.NIL_TOKEN;

		if (litTokens.containsKey(token)) {
			tk = new Token(litTokens.get(token));
		} else {
			switch (token.charAt(0)) {
			case '(':
			case ')':
			case '[':
			case ']':
			case '{':
			case '}':
				tk = tokenizeGrouping(token);
				break;
			default:
				tk = tokenizeLiteral(token, stringLts);
			}
		}

		return tk;
	}

	private static Token tokenizeGrouping(final String token) {
		Token tk = Token.NIL_TOKEN;

		if (StringUtils.containsOnly(token, "\\" + token.charAt(0))) {
			/* Handle multiple-grouped delimiters. */
			switch (token.charAt(0)) {
			case '(':
				tk = new Token(OPAREN, token.length());
				break;

			case ')':
				tk = new Token(CPAREN, token.length());
				break;

			case '[':
				tk = new Token(OBRACKET, token.length());
				break;

			case ']':
				tk = new Token(CBRACKET, token.length());
				break;

			case '{':
				tk = new Token(OBRACE, token.length());
				break;

			case '}':
				tk = new Token(CBRACE, token.length());
				break;

			default:
				Errors.inst.printError(EK_TOK_UNGROUP, token);
				break;
			}
		}

		return tk;
	}

	/* Patterns for matching. */
	private final Pattern   hexadecimalMatcher      =
	        Pattern.compile("\\A[\\-\\+]?0x[0-9A-Fa-f]+\\Z");
	private final Pattern   flexadecimalMatcher     =
	        Pattern.compile("\\A[\\-\\+]?[0-9][0-9A-Za-z]+B\\d{1,2}\\Z");
	private final Pattern   stringLitMatcher        =
	        Pattern.compile("\\AstringLiteral(\\d+)\\Z");

	/* Tokenize a literal value. */
	private Token tokenizeLiteral(final String rtoken, final IMap<String, String> stringLts) {
		Token tk = Token.NIL_TOKEN;

		String token = rtoken.trim();

		if (TokenUtils.isInt(token)) {
			tk = new Token(INT_LIT, Long.parseLong(token));
		} else if (hexadecimalMatcher.matcher(token).matches()) {
			final String newToken = token.substring(0, 1) + token.substring(token.indexOf('x'));
			tk = new Token(INT_LIT, Long.parseLong(newToken.substring(2).toUpperCase(), 16));
		} else if (flexadecimalMatcher.matcher(token).matches()) {
			final int parseBase = Integer.parseInt(token.substring(token.lastIndexOf('B') + 1));

			if (parseBase < Character.MIN_RADIX || parseBase > Character.MAX_RADIX) {
				Errors.inst.printError(EK_TOK_INVBASE, Integer.toString(parseBase));
				return Token.NIL_TOKEN;
			}

			final String flexNum = token.substring(0, token.lastIndexOf('B'));

			try {
				tk = new Token(INT_LIT, Long.parseLong(flexNum, parseBase));
			} catch (final NumberFormatException nfex) {
				Errors.inst.printError(EK_TOK_INVFLEX, flexNum, Integer.toString(parseBase));
				return Token.NIL_TOKEN;
			}
		} else if (TokenUtils.isDouble(token)) {
			tk = new Token(FLOAT_LIT, Double.parseDouble(token));
		} else if (DiceBox.isValidExpression(token)) {
			tk = new Token(DICE_LIT, DiceBox.parseExpression(token));
		} else {
			final Matcher stringLit = stringLitMatcher.matcher(token);

			if (stringLit.matches()) {
				final int litNum = Integer.parseInt(stringLit.group(1));

				eng.addStringLiteral(litNum, stringLts.get(token));
				tk = new Token(STRING_LIT, litNum);
			} else {
				/* Everything else is a symbol */
				eng.symTable.put(nextSym++, token);

				tk = new Token(VREF, nextSym - 1);
			}
		}

		return tk;
	}
}
