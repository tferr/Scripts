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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import net.imagej.ImageJ;

import org.scijava.ItemVisibility;
import org.scijava.app.StatusService;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.display.DisplayService;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.text.TextService;
import org.scijava.ui.DialogPrompt;
import org.scijava.ui.UIService;

import bar.Utils;

/**
 * Renders GitHub README.md files as HTML files that once JARified are used as
 * offline documentation of BAR)
 */
@Plugin(type = Command.class, menu = { @Menu(label = "BAR"), @Menu(label = "Help", weight = 0.01d),
		@Menu(label = "Offline Help...") })
public class MDReader implements Command {

	@Parameter
	private CommandService commandService;

	@Parameter
	private DisplayService displayService;

	@Parameter
	private StatusService statusService;

	@Parameter
	private TextService textService;

	@Parameter
	private UIService uiService;

	@Parameter(visibility = ItemVisibility.MESSAGE, persist = false)
	private final String help = helpMsg();

	@Parameter(label = "Help on which topic?", choices = { "Analysis", "Analysis/Time Series", "Annotation",
			"Data Analysis", "My Routines", "Segmentation", "Utilities", "boilerplate", "lib", "tools", "tutorials",
			"Other..." })
	private String resourceDir;

	private String helpMsg() {
		final StringBuffer sb = new StringBuffer();
		sb.append("<html>").append("<body><div WIDTH=400>")
				.append("This command displays the markdown documentation of BAR. ")
				.append("These are the same files displayed by GitHub when browsing the ").append("<a href='")
				.append(Utils.getSourceURL()).append("'> BAR repository</a>. ")
				.append("Note that while files are accessed locally, proper display of embeded ")
				.append("images requires an internet connection.");
		return sb.toString();
	}

	@Override
	public void run() {
		if (("Other...").equals(resourceDir))
			commandService.run(Help.class, true);
		else {
			final String parent = (Character.isUpperCase(resourceDir.charAt(0))) ? "scripts/BAR/" : "";
			openREADME(parent + resourceDir.replace(" ", "_"));
		}
	}

	public void openREADME(final String resourceDirectory) {

		final String resourcePath = resourceDirectory + "/README.md";
		statusService.showStatus("Opening " + resourcePath + "...");

		String contents = null;
		try {
			final URL url = Utils.getBARresource(resourcePath);
			final InputStream is = url.openStream();
			final File file = File.createTempFile(resourceDirectory + "README", ".md");
			final OutputStream os = new FileOutputStream(file);
			final byte[] buffer = new byte[1024];

			// copy file content in bytes
			int length;
			while ((length = is.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}
			os.close();
			contents = textService.asHTML(file);

		} catch (NullPointerException | SecurityException | IOException | UnsupportedOperationException exc) {
			exc.printStackTrace();
			contents = null;
		}

		if (contents == null) {
			uiService.showDialog("Could not open file. See Console for details.", "An Error Ocurred",
					DialogPrompt.MessageType.ERROR_MESSAGE);
		} else {
			displayService.createDisplay("BAR/" + resourceDirectory + " Routines", cleanseRelativeImagePaths(contents));
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
		ij.command().run(MDReader.class, true);
	}

}
