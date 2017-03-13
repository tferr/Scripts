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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import net.imagej.ImageJ;

import org.scijava.Context;
import org.scijava.app.StatusService;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.display.DisplayService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.text.TextService;
import org.scijava.ui.DialogPrompt;
import org.scijava.ui.UIService;

/** Loads jarified markdown files (offline documentation of BAR) */
@Plugin(type = Command.class, headless = true)
public class OfflineHelp implements Command {

	@Parameter
	private Context context;

	@Parameter
	private CommandService commandService;

	@Parameter
	private static DisplayService displayService;

	@Parameter
	private static StatusService statusService;

	@Parameter
	private static TextService textService;

	@Parameter
	private static UIService uiService;

	@Parameter(label = "Which markdown file?", choices = { "Analysis", "Annotation", "Data_Analysis", "Segmentation" })
	private String resourceDir;

	@Override
	public void run() {
		openREADME(resourceDir);
	}

	public static void openREADME(final String resourceDirectory) {
		final String resourcePath = "/scripts/BAR/" + resourceDirectory + "/README.md";
		statusService.showStatus("Opening " + resourcePath + "...");
		File file = null;
		try {
			final URI uri = OfflineHelp.class.getResource(resourcePath).toURI();
			FileSystem fileSystem = null;
			Path path;
			if ("jar".equals(uri.getScheme())) {
				fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap());
				path = fileSystem.getPath(resourcePath);
			} else {
				path = Paths.get(uri);
			}
			file = path.toFile();
		} catch (IOException | URISyntaxException exc) {
			file = null;
		}

		if (file == null || !file.exists()) {
			uiService.showDialog("Could not open file.", DialogPrompt.MessageType.ERROR_MESSAGE);
			return;
		} else {
			try {
				final String contents = textService.asHTML(file);
				displayService.createDisplay(resourceDirectory, contents);
			} catch (final IOException e) {
				throw new IllegalStateException(e.getMessage());
			}
		}
		statusService.clearStatus();

	}

	public static void main(final String... args) {
		final ImageJ ij = net.imagej.Main.launch(args);
		ij.command().run(OfflineHelp.class, true);
	}

}
