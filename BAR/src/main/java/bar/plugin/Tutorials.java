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
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.stream.Stream;

import net.imagej.ImageJ;

import org.scijava.Context;
import org.scijava.app.StatusService;
import org.scijava.command.Command;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.DialogPrompt;
import org.scijava.ui.UIService;
import org.scijava.ui.swing.script.TextEditor;

/** Loads a new instance of the Script editor with jarified tutorial files */
@Plugin(type = Command.class, menu = { @Menu(label = "BAR"), @Menu(label = "Help", weight = 0.01d),
		@Menu(label = "Open Interactive Tutorials...") })
public class Tutorials implements Command {

	@Parameter
	private Context context;

	@Parameter
	private UIService uiService;

	@Parameter
	private StatusService statusService;

	@Parameter(label = "Which introductory tutorial?", choices = { "Python", "BeanShell" })
	private String lang;

	@Override
	public void run() {
		final ArrayList<URL> urls = getTutorialFiles(lang);
		if (urls == null || urls.isEmpty()) {
			uiService.showDialog("Could not load tutorial files.", DialogPrompt.MessageType.ERROR_MESSAGE);
			return;
		}

		// Ensure files are opened in the right order
		urls.sort(new Comparator<URL>() {
			@Override
			public int compare(final URL u1, final URL u2) {
				return u1.toString().compareTo(u2.toString());
			}

		});
		final TextEditor editor = new TextEditor(context);
		for (final URL url : urls) {
			statusService.showStatus(urls.indexOf(url), urls.size(), "Opening tutorial files...");
			editor.loadTemplate(url);
		}
		editor.switchTo(0);
		editor.setVisible(true);
		editor.setTitle("BAR Tutorial Files [" + lang + "]");
		statusService.clearStatus();
	}

	private String getMatcherPattern(final String language) {
		// see http://docs.oracle.com/javase/tutorial/essential/io/find.html
		switch (language.toLowerCase()) {
		case "python":
			return "glob:**.py";
		case "beanshell":
			return "glob:**.bsh";
		case "groovy":
			return "glob:**.{groovy,gvy}";
		default:
			return "glob:**.*"; // any extension
		}
	}

	/* See http://stackoverflow.com/a/28057735 for details */
	private ArrayList<URL> getTutorialFiles(final String language) {
		final String dir = "/tutorials";
		final ArrayList<URL> urlList = new ArrayList<>();
		final PathMatcher matcher = FileSystems.getDefault().getPathMatcher(getMatcherPattern(language));
		try {
			final URI uri = Tutorials.class.getResource(dir).toURI();
			FileSystem fileSystem = null;
			Path path;
			if ("jar".equals(uri.getScheme())) {
				fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap());
				path = fileSystem.getPath(dir);
			} else {
				path = Paths.get(uri);
			}
			final Stream<Path> walk = Files.walk(path, 1);
			final Iterator<Path> it = walk.iterator();
			while (it.hasNext()) {
				final Path p = it.next();
				if (matcher.matches(p)) {
					urlList.add(p.toUri().toURL());
				}
			}
			walk.close();
			if (fileSystem != null)
				fileSystem.close();
		} catch (IOException | URISyntaxException exc) {
			return null;
		}
		return urlList;
	}

	public static void main(final String... args) {
		final ImageJ ij = net.imagej.Main.launch(args);
		ij.command().run(Tutorials.class, true);
	}

}
