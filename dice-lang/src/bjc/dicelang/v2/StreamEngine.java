package bjc.dicelang.v2;

import bjc.utils.funcdata.FunctionalList;
import bjc.utils.funcdata.IList;
import bjc.utils.esodata.Tape;

public class StreamEngine {
	private DiceLangEngine eng;

	private Tape<IList<String>> streams;
	private IList<String>       currStream;

	public StreamEngine(DiceLangEngine eng) {
		streams = new Tape<>();

		currStream = new FunctionalList<>();
		streams.append(currStream);
	}

	public boolean doStreams(String[] toks, IList<String> dest) {
		for(String tk : toks) {
			if(tk.startsWith("{@")) {
				if(!processCommand(tk)) return false;
			} else {
				currStream.add(tk);
			}
		}

		for(String tk : currStream.toIterable()) {
			dest.add(tk);
		}

		return true;
	}

	private boolean processCommand(String tk) {
		switch(tk.charAt(2)) {
			case '+':
				streams.append(new FunctionalList<>());
				break;
			case '>':
				if(!streams.right()) {
					System.out.println("\tERROR: Attempted to switch to non-existent stream");
					return false;
				}

				currStream = streams.item();
				break;
			case '<':
				if(!streams.left()) {
					System.out.println("\tERROR: Attempted to switch to non-existent stream");
					return false;
				}

				currStream = streams.item();
				break;
			case '-':
				if(streams.size() == 1) {
					System.out.println("\tERROR: Cannot delete last stream");
					return false;
				} else {
					streams.remove();
					currStream = streams.item();
				}
				break;
			default:
				System.out.println("\tERROR: Unknown stream control command: " + tk);
				return false;
		}

		return true;
	}
}
