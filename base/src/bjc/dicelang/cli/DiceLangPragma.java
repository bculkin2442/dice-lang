package bjc.dicelang.cli;

import bjc.dicelang.DiceLangEngine;

/**
 * Represents a pragma for the command interface.
 * 
 * @author EVE
 *
 */
public interface DiceLangPragma {
	/**
	 * Execute the pragma.
	 * 
	 * @param lne
	 *        The command line the pragma came from.
	 * @param eng
	 *        The engine we are attached to.
	 * @return Whether or not the pragma succeeded.
	 */
	public boolean execute(String lne, DiceLangEngine eng);

	/**
	 * Get a description on how to use this pragma
	 * 
	 * @return The description on how to use the pragma
	 */
	public String getDescription();

	/**
	 * Get a brief idea on what this pragma does
	 * 
	 * @return A brief description of what this pragma does.
	 */
	public String getBrief();
}
