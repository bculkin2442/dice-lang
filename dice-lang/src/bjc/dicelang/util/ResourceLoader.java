package bjc.dicelang.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import bjc.dicelang.Errors;

import static bjc.dicelang.Errors.ErrorKey.*;

/**
 * Load resources bundled with DiceLang
 * 
 * @author EVE
 *
 */
public class ResourceLoader {
	/**
	 * Loads a .help file from the data/help directory.
	 * 
	 * @param name The name of the help file to load.
	 * 
	 * @return The contents of the help file, or null if it could not be opened
	 */
	public String[] loadHelpFile(String name) {
		URL fle = this.getClass().getResource("/data/help/" + name + ".help");
		
		try {
			return Files.lines(Paths.get(fle.toURI())).toArray(sze -> new String[sze]);
		} catch (IOException ioex) {
			Errors.inst.printError(EK_MISC_IOEX, fle.toString());
		} catch (URISyntaxException usex) {
			Errors.inst.printError(EK_MISC_IOEX, fle.toString());			
		}
		
		return null;
	}
}
