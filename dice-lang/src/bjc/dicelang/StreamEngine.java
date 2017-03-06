package bjc.dicelang;

import bjc.utils.funcdata.FunctionalList;
import bjc.utils.funcdata.IList;
import bjc.utils.funcutils.ListUtils;

import static bjc.dicelang.Errors.ErrorKey.*;

import bjc.utils.esodata.SingleTape;
import bjc.utils.esodata.Tape;
import bjc.utils.esodata.TapeLibrary;

/**
 * Implements multiple interleaved parse streams, as well as a command language
 * for the streams.
 *
 * The idea for the interleaved streams came from the language Oozylbub &amp;
 * Murphy, but the command language was my own idea.
 *
 * @author Ben Culkin
 */
public class StreamEngine {
	/*
	 * The engine we're attached to.
	 */
	private DiceLangEngine eng;

	/*
	 * Our streams.
	 */
	private Tape<IList<String>> streams;
	private IList<String>       currStream;

	/*
	 * Saved streams
	 */
	private TapeLibrary<IList<String>> savedStreams;

	/**
	 * Create a new stream engine.
	 *
	 * @param engine The dice engine we're attached to.
	 */
	public StreamEngine(DiceLangEngine engine) {
		eng = engine;

		savedStreams = new TapeLibrary<>();
	}

	private void init() {
		/*
		 * Reinitialize our list of streams.
		 */
		streams = new SingleTape<>();

		/*
		 * Create an initial stream.
		 */
		currStream = new FunctionalList<>();
		streams.insertBefore(currStream);
	}

	/**
	 * Process a possibly interleaved set of streams from toks into dest.
	 *
	 * @param toks The raw token to read streams from.
	 * @param dest The list to write the final stream to.
	 *
	 * @return Whether or not the streams were successfully processed.
	 */
	public boolean doStreams(String[] toks, IList<String> dest) {
		/*
		 * Initialize per-run state.
		 */
		init();

		/*
		 * Are we currently quoting things?
		 */
		boolean quoteMode = false;

		/*
		 * Process each token.
		 */
		for(String tk : toks) {
			/*
			 * Process stream commands.
			 */
			if(tk.startsWith("{@S") && !quoteMode) {
				if(tk.equals("{@SQ}")) {
					quoteMode = true;
				} else if(!processCommand(tk)) {
					return false;
				}
				/*
				 * Command ran correctly, continue
				 */
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
