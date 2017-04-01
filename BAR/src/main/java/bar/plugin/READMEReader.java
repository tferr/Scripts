/*
 * IJ BAR: https://github.com/tferr/Scripts
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation
 * (http://www.gnu.org/licenses/gpl.txt).
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 */
package bar.plugin;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import net.imagej.ImageJ;

import org.scijava.app.StatusService;
import org.scijava.command.Command;
import org.scijava.display.DisplayService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.text.TextService;
import org.scijava.ui.DialogPrompt;
import org.scijava.ui.UIService;

/** Loads jarified markdown files (offline documentation of BAR) */
@Plugin(type = Command.class, menuPath = "BAR > Help > Open Offline Help...")
public class READMEReader implements Command {

	@Parameter
	private static DisplayService displayService;

	@Parameter
	private static StatusService statusService;

	@Parameter
	private static TextService textService;

	@Parameter
	private static UIService uiService;

	@Parameter(label = "Which markdown file?", choices = { "Analysis", "Analysis/Time Series", "Annotation",
			"Data Analysis", "lib", "lib/tests", "My Routines", "Segmentation", "tools", "Utilities" })
	private String resourceDir;

	@Override
	public void run() {
		openREADME(resourceDir.replace(" ", "_"));
	}

	public void openREADME(final String resourceDirectory) {

		final String resourcePath = "/scripts/BAR/" + resourceDirectory + "/README.md";
		statusService.showStatus("Opening " + resourcePath + "...");

		String contents = null;
		try {
			final URI uri = READMEReader.class.getResource(resourcePath).toURI();
			FileSystem fileSystem = null;
			Path path;
			if ("jar".equals(uri.getScheme())) {
				fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap());
				path = fileSystem.getPath(resourcePath);
			} else {
				path = Paths.get(uri);
			}
			contents = textService.asHTML(path.toFile());
		} catch (IOException | URISyntaxException exc) {
			contents = null;
		}

		if (contents == null) {
			uiService.showDialog("Could not open file.", DialogPrompt.MessageType.ERROR_MESSAGE);
			return;
		} else {
			displayService.createDisplay(resourceDirectory, cleanseRelativeImagePaths(contents));
		}
		statusService.clearStatus();

	}

	private String cleanseRelativeImagePaths(final String markdownText) {
		final String RELATIVE_IMAGE_PATH = "../../../../../../../";
		final String ABSOLUTE_IMAGE_PATH = "https://raw.githubusercontent.com/tferr/Scripts/master/";
		return markdownText.replaceAll(RELATIVE_IMAGE_PATH, ABSOLUTE_IMAGE_PATH);
	}

	public static void main(final String... args) {
		final ImageJ ij = net.imagej.Main.launch(args);
		ij.command().run(READMEReader.class, true);
	}

}
