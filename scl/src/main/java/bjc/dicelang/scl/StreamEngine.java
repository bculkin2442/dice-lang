package bjc.dicelang.scl;

import bjc.utils.esodata.SingleTape;
import bjc.utils.esodata.Tape;
import bjc.utils.esodata.TapeLibrary;
import bjc.utils.funcdata.FunctionalList;
import bjc.utils.funcdata.FunctionalMap;
import bjc.utils.funcdata.IList;
import bjc.utils.funcdata.IMap;
import bjc.utils.funcutils.ListUtils;

import java.util.Arrays;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

import static bjc.dicelang.scl.Errors.ErrorKey.*;

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
	/**
	 * Whether or not we're doing debugging.
	 */
	public final boolean debug = true;

	/* Our streams. */
	Tape<IList<String>> streams;
	IList<String> currStream;

	/* Saved streams */
	TapeLibrary<IList<String>> savedStreams;

	/* Handler for SCL programs */
	private final StreamControlEngine scleng;

	private static IMap<Character, Predicate<StreamEngine>> commands;

	static {
		commands = new FunctionalMap<>();

		commands.put('+', (eng) -> {
			eng.newStream();
			return true;
		});

		commands.put('>', (eng) -> eng.rightStream());
		commands.put('<', (eng) -> eng.leftStream());
		commands.put('-', (eng) -> eng.deleteStream());
		commands.put('M', (eng) -> eng.mergeStream());
		commands.put('L', (eng) -> {
			String[] arr = eng.currStream.toArray(new String[0]);

			boolean succ = eng.scleng.runProgram(arr);

			return succ;
		});
	}

	/**
	 * Create a new stream engine.
	 *
	 */
	public StreamEngine() {
		savedStreams = new TapeLibrary<>();
		scleng = new StreamControlEngine(this);
	}

	/* Do pre-run (re)initialization. */
	private void init() {
		/* Reinitialize our list of streams. */
		streams = new SingleTape<>();

		/* Create an initial stream. */
		currStream = new FunctionalList<>();
		streams.insertBefore(currStream);
	}

	/**
	 * Process a possibly interleaved set of streams.
	 *
	 * @param toks
	 *        The raw token to read streams from.
	 *
	 * @param dest
	 *        The list to write the final stream to.
	 *
	 * @return Whether or not the streams were successfully processed.
	 */
	public boolean doStreams(final String[] toks, final IList<String> dest) {
		return doStreams(Arrays.asList(toks), dest);
	}

	/**
	 * Process a possibly interleaved set of streams.
	 *
	 * @param toks
	 *        The raw token to read streams from.
	 *
	 * @param dest
	 *        The list to write the final stream to.
	 *
	 * @return Whether or not the streams were successfully processed.
	 */
	public boolean doStreams(final Iterable<String> toks, final IList<String> dest) {
		/* Initialize per-run state. */
		init();

		/* Are we currently quoting things? */
		boolean quoteMode = false;

		/* Process each token. */
		for(final String tk : toks) {
			/* Process stream commands. */
			if(tk.startsWith("{@S") && !quoteMode) {
				if(tk.equals("{@SQ}")) {
					/* Start quoting. */
					quoteMode = true;
				} else if(!processCommand(tk)) {
					return false;
				}
			} else {
				if(tk.equals("{@SU}")) {
					/* Stop quoting. */
					quoteMode = false;
				} else if(tk.startsWith("\\") && tk.endsWith("{@SU}")) {
					/* Unquote quoted end. */
					currStream.add(tk.substring(1));
				} else {
					currStream.add(tk);
				}
			}
		}

		for(final String tk : currStream) {
			/* Collect tokens from the current stream. */
			dest.add(tk);
		}

		return true;
	}

	/** Create a new stream. */
	public void newStream() {
		streams.insertAfter(new FunctionalList<>());
	}

	/**
	 * Move to a stream to the right.
	 *
	 * @return Whether or not the move was successful.
	 */
	public boolean rightStream() {
		if(!streams.right()) {
			Errors.inst.printError(EK_STRM_NONEX);
			return false;
		}

		currStream = streams.item();
		return true;
	}

	/**
	 * Move to a stream to the left.
	 *
	 * @return Whether or not the move was successful.
	 */
	public boolean leftStream() {
		if(!streams.left()) {
			Errors.inst.printError(EK_STRM_NONEX);
			return false;
		}

		currStream = streams.item();
		return true;
	}

	/**
	 * Delete the current stream.
	 *
	 * @return Whether or not the delete succeeded.
	 */
	public boolean deleteStream() {
		if(streams.size() == 1) {
			Errors.inst.printError(EK_STRM_LAST);
			return false;
		}

		streams.remove();
		currStream = streams.item();

		return true;
	}

	/**
	 * Merge the current stream into the previous stream.
	 *
	 * @return Whether or not the merge succeded.
	 */
	public boolean mergeStream() {
		if(streams.size() == 1) {
			Errors.inst.printError(EK_STRM_LAST);
			return false;
		}

		final IList<String> stringLit = streams.remove();
		currStream = streams.item();
		currStream.add(ListUtils.collapseTokens(stringLit, " "));

		return true;
	}

	private boolean processCommand(final String tk) {
		char[] comms = null;

		if(tk.length() > 5) {
			/* Pull off {@S and closing } */
			comms = tk.substring(3, tk.length() - 1).toCharArray();
		} else {
			/* Its a single char. command. */
			comms = new char[1];
			comms[0] = tk.charAt(3);
		}

		/* Process each command. */
		for(final char comm : comms) {
			boolean succ = commands.getOrDefault(comm, (eng) -> {
				Errors.inst.printError(EK_STRM_INVCOM, tk);
				return false;
			}).test(this);

			if(!succ) return false;
		}

		return true;
	}
}