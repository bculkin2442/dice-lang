package bjc.dicelang.scl.tokens;

/**
 * Represents the word type.
 * 
 * @author student
 *
 */
public enum WordType {
	/* Array manipulation */
	/**
	 * Create an array
	 */
	MAKEARRAY,
	/**
	 * Make a token executable.
	 */
	MAKEEXEC,
	/**
	 * Make a token unexecutable.
	 */
	MAKEUNEXEC,

	/* Stream manipulation */
	/**
	 * Create a new stream.
	 */
	NEWSTREAM,
	/**
	 * Swap to the left stream.
	 */
	LEFTSTREAM,
	/**
	 * Swap to the right stream.
	 */
	RIGHTSTREAM,
	/**
	 * Delete the current stream.
	 */
	DELETESTREAM,
	/**
	 * Merge the streams.
	 */
	MERGESTREAM,

	/* Stack manipulation */
	/**
	 * Get the count of items on the stack.
	 */
	STACKCOUNT,
	/**
	 * Check if the stack is empty.
	 */
	STACKEMPTY,
	/**
	 * Drop an item from the top of the stack.
	 */
	DROP,
	/**
	 * Drop a number of items from the top of the stack.
	 */
	NDROP,
	/**
	 * Drop an item, leaving the top of the stack alone.
	 */
	NIP,
	/**
	 * Drop a number of items, leaving the top of the stack alone.
	 */
	NNIP,
	
	/* Definition manipulation. */
	/**
	 * Define a word.
	 */
	DEFINE,
}