package bjc.dicelang.util;

import bjc.dicelang.Errors;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import static bjc.dicelang.Errors.ErrorKey.EK_MISC_IOEX;

/**
 * Load resources bundled with DiceLang.
 *
 * @author EVE
 *
 */
public class ResourceLoader {
	/**
	 * Loads a .help file from the data/help directory.
	 *
	 * @param name
	 *                The name of the help file to load.
	 *
	 * @return The contents of the help file, or null if it could not be
	 *         opened.
	 */
	@SuppressWarnings("unused")
	public static String[] loadHelpFile(String name) {
		URL fle = ResourceLoader.class.getResource("/data/help/" + name + ".help");

		try {
			return Files.lines(Paths.get(fle.toURI())).toArray(sze -> new String[sze]);
		} catch(IOException | URISyntaxException ioex) {
			Errors.inst.printError(EK_MISC_IOEX, fle.toString());
		}

		return null;
	}
}
