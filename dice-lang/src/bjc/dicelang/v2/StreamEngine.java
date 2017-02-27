package bjc.dicelang.v2;

import bjc.utils.funcdata.FunctionalList;
import bjc.utils.funcdata.IList;
import bjc.utils.funcutils.ListUtils;

import static bjc.dicelang.v2.Errors.ErrorKey.*;

import bjc.utils.esodata.SingleTape;
import bjc.utils.esodata.Tape;

public class StreamEngine {
	private DiceLangEngine eng;

	private Tape<IList<String>> streams;
	private IList<String>       currStream;

	public StreamEngine(DiceLangEngine engine) {
		eng = engine;
	}

	private void init() {
		streams = new SingleTape<>();

		currStream = new FunctionalList<>();
		streams.insertBefore(currStream);
	}

	public boolean doStreams(String[] toks, IList<String> dest) {
		init();

		boolean quoteMode = false;

		for(String tk : toks) {
			if(tk.startsWith("{@S") && !quoteMode) {
				if(tk.equals("{@SQ}")) {
					quoteMode = true;
				} else if(!processCommand(tk)) {
					return false;
				}
				// Command ran correctly, continue
			} else {
				if(tk.equals("{@SU}")) {
					quoteMode = false;
				} else if(tk.startsWith("\\") && tk.endsWith("{@SU}")) {
					currStream.add(tk.substring(1));
				} else {
					currStream.add(tk);
				}
			}
		}

		for(String tk : currStream) {
			dest.add(tk);
		}

		return true;
	}

	private boolean processCommand(String tk) {
		char[] comms = null;

		if(tk.length() > 5) {
			comms = tk.substring(3, tk.length() - 1).toCharArray();
		} else {
			comms = new char[1];
			comms[0] = tk.charAt(3);
		}

		for(char comm : comms) {
			switch(comm) {
				case '+':
					streams.insertAfter(new FunctionalList<>());
					break;
				case '>':
					if(!streams.right()) {
						Errors.inst.printError(EK_STRM_NONEX);
						return false;
					}

					currStream = streams.item();
					break;
				case '<':
					if(!streams.left()) {
						Errors.inst.printError(EK_STRM_NONEX);
						return false;
					}

					currStream = streams.item();
					break;
				case '-':
					if(streams.size() == 1) {
						Errors.inst.printError(EK_STRM_LAST);
						return false;
					} else {
						streams.remove();
						currStream = streams.item();
					}
					break;
				case 'S':
					if(streams.size() == 1) {
						Errors.inst.printError(EK_STRM_LAST);
						return false;
					} else {
						IList<String> stringLit = streams.remove();
						currStream = streams.item();
						currStream.add(ListUtils.collapseTokens(stringLit, " "));
					}
					break;
				default:
					Errors.inst.printError(EK_STRM_INVCOM, tk);
					return false;
			}
		}

		return true;
	}
}
