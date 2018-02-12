package bjc.dicelang.util;

import static bjc.dicelang.Errors.ErrorKey.EK_MISC_IOEX;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import bjc.dicelang.Errors;

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
	 *            The name of the help file to load.
	 *
	 * @return The contents of the help file, or null if it could not be opened.
	 */
	public static String[] loadHelpFile(final String name) {
		final URL fle = ResourceLoader.class.getResource("/data/help/" + name + ".help");

		try {
			Path pth = Paths.get(fle.toURI());

			return Files.lines(pth).toArray(sze -> new String[sze]);
		} catch (IOException | URISyntaxException ioex) {
			Errors.inst.printError(EK_MISC_IOEX, fle.toString());
		}

		return null;
	}
}
