package bjc.dicelang.v2;

import bjc.utils.funcdata.FunctionalList;
import bjc.utils.funcdata.IList;
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

		for(String tk : toks) {
			if(tk.startsWith("{@S")) {
				if(!processCommand(tk)) return false;
			} else {
				currStream.add(tk);
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
		}

		return true;
	}
}
