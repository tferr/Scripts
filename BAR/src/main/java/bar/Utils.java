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
package bar;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Frame;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.scijava.ui.swing.script.TextEditor;
import org.scijava.Context;

import ij.IJ;
import ij.ImagePlus;
import ij.Menus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.ImageWindow;
import ij.gui.PlotWindow;
import ij.io.OpenDialog;
import ij.measure.ResultsTable;
import ij.plugin.MacroInstaller;
import ij.plugin.PlugIn;
import ij.plugin.frame.Editor;
import ij.text.TextPanel;
import ij.text.TextWindow;

/**
 * A collection of utilities to interact with BAR, including scripting aids for
 * ImageJ. Most methods are designed so that they can be called by the IJ macro
 * language (BTW, Note that the IJ1 macro interpreter converts all returned
 * objects into their String values).
 */
public class Utils implements PlugIn {

	/** The BAR version **/
	public static String VERSION = version();

	/** A reference to the build date */
	public static String BUILD_DATE = buildDate();

	/** A reference to the build year */
	public static String BUILD_YEAR = buildYear();

	/** The URL to BAR's wiki page */
	static final String DOC_URL = "http://imagej.net/BAR";

	/** The URL to BAR's GitHub repository */
	static final String SRC_URL = "https://github.com/tferr/Scripts";

	/** The URL to BAR's GitHub repository */
	static final String API_URL = "http://tferr.github.io/Scripts/apidocs/";

	/** The absolute path to the /BAR directory */
	static final String BAR_DIR = IJ.getDirectory("plugins")
			+ "Scripts" + File.separator + "BAR" + File.separator;

	/** The absolute path to the /BAR/Snippets/ directory */
	static final String SNIPPETS_DIR = BAR_DIR +"Snippets" + File.separator;

	/** The absolute path to the /BAR/lib/ directory */
	static final String LIB_DIR = BAR_DIR + "lib" + File.separator;


	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public void run(final String arg) {

		shiftClickWarning();

		final String[] args = arg.split(":");
		if (args.length==1) return;

		// Instructions related to snippets
		if (args[0].equalsIgnoreCase("snippet")) {

			if (args[1].equalsIgnoreCase("list")) {
				listDirectory(SNIPPETS_DIR);
			} else if (args[1].equalsIgnoreCase("reveal")) {
				revealFile(SNIPPETS_DIR);
			} else //TODO implement a "reload snippets" command
				openSnippet(arg);

		// Instructions related to Menu transfers
		} else if (args[0].equalsIgnoreCase("moveMenu")) {

			moveSubmenu(args[1]);

		// Instructions related to tools and toolsets
		} else if (args[0].startsWith("tools")) {

			final String dir = IJ.getDirectory("macros") + args[0]
					+ File.separator;
			if (args[1].equalsIgnoreCase("reveal")) {
				revealFile(dir);
			} else if (IJ.shiftKeyDown()) {
				IJ.showStatus("Opening file...");
				openScript(dir, args[1]);
			} else {
				installMacroFile(dir, args[1]);
			}

		// Instructions related to lib files
		} else if (arg.startsWith("lib:")) {

			if (args[1].equalsIgnoreCase("reveal")) {
				revealFile(LIB_DIR);
			}

		}
	}


	/**
	 * Retrieves BAR's version
	 *
	 * @return the version or a non-empty place holder string if version could
	 *         not be retrieved.
	 *
	 */
	private static String version() {
		// http://blog.soebes.de/blog/2014/01/02/version-information-into-your-appas-with-maven/
		if (VERSION == null) {
			final Package pkg = Utils.class.getPackage();
			if (pkg != null)
				VERSION = pkg.getImplementationVersion();
			if (VERSION == null)
				VERSION = "X Dev";
		}
		return VERSION;
	}

	/**
	 * Retrieves BAR's implementation date
	 *
	 * @return the implementation date or an empty strong if date could not be
	 *         retrieved.
	 */
	private static String buildDate() {
		// http://stackoverflow.com/questions/1272648/
		if (BUILD_DATE == null) {
			final Class<Utils> clazz = Utils.class;
			final String className = clazz.getSimpleName() + ".class";
			final String classPath = clazz.getResource(className).toString();
			final String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1)
					+ "/META-INF/MANIFEST.MF";
			try {
				final Manifest manifest = new Manifest(new URL(manifestPath).openStream());
				final Attributes attr = manifest.getMainAttributes();
				BUILD_DATE = attr.getValue("Implementation-Date");
				BUILD_DATE = BUILD_DATE.substring(0, BUILD_DATE.lastIndexOf("T"));
			} catch (final Exception ignored) {
				BUILD_DATE = "";
			}
		}
		return BUILD_DATE;
	}

	/**
	 * Retrieves BAR's implementation year.
	 *
	 * @return the implementation year or an empty string if date could not be
	 *         retrieved.
	 */
	private static String buildYear() {
		if (BUILD_DATE == null || BUILD_DATE.length() < 4)
			return "";
		return BUILD_DATE.substring(0, 4);
	}

	/**
	 * Displays a status-bar warning on "open by Shift-click" being disabled for
	 * pre-compiled plugins. Used by BAR plugins.
	 */
	public static void shiftClickWarning() {
		if (IJ.shiftKeyDown()) {
			IJ.beep();
			IJ.showStatus("Compiled plugin. Use 'About BAR...' to access source code repository.");
		}
	}

	/**
	 * Transfers the specified BAR submenu between the main IJ's menu bar and
	 * the image's context menu, or vice-versa if the submenu is already in the
	 * context menu. An acknowledgement message is displayed if
	 * {@link ij.IJ#macroRunning()} returns false.
	 *
	 * @param subMenu
	 *            the {@link java.awt.Menu} to be transfered
	 */
	private void moveSubmenu(final String subMenu) {

		final Menu barMenu = Menus.getImageJMenu("BAR");
		final PopupMenu popMenu = Menus.getPopupMenu();
		final String placeHolderString = subMenu + " ";
		final Menu placeHolder = new Menu(placeHolderString);
		placeHolder.setEnabled(false);

		final Integer popmenuPos = getMenuItem(popMenu, subMenu);
		final Integer barmenuPos = getMenuItem(barMenu, subMenu);
		if (popmenuPos==null || barmenuPos==null) {
			IJ.error("BAR v"+ VERSION +" Error", "Some menu items are not accessible.\n"
					+"Perhaps the image's context menu is disabled?");
			return;
		}

		if (popmenuPos==-1) { // parent is MenuBar

			final MenuItem lastItem = popMenu.getItem(popMenu.getItemCount()-1);
			if (!(lastItem instanceof Menu))
				popMenu.addSeparator();
			popMenu.add(barMenu.getItem(barmenuPos));
			barMenu.insert(placeHolder, barmenuPos);
			if (!IJ.macroRunning())
				IJ.showMessage("BAR v"+ VERSION, ""+ subMenu +"> transferred to context menu.\n"
						+"Right-click on the image canvas to access it.");

		} else { // parent is PopupMenu

			final Integer placeholderPos = getMenuItem(barMenu, placeHolderString);
			barMenu.remove(placeholderPos);
			barMenu.insert(popMenu.getItem(popmenuPos), placeholderPos);
			final MenuItem lastItem = popMenu.getItem(popMenu.getItemCount()-1);
			if (lastItem.getLabel().equals("-"))
				popMenu.remove(lastItem);
			if (!IJ.macroRunning())
				IJ.showStatus("BAR>" + subMenu + "> transferred to main menu.");

		}
	}

	/**
	 * Returns the index of the MenuItem labeled by the specified string.
	 *
	 * @param menu
	 *            the java.awt.Menu to be parsed
	 * @param label
	 *            the java.awt.MenuItem label being searched
	 * @return the index of the java.awt.MenuItem. Returns {@code -1} if no
	 *         match is found, {@code null} if menu is not available.
	 */
	private Integer getMenuItem(final Menu menu, final String label) {
		int position = -1;
		if (menu==null) {
			return null;
		}
		for (int i=0; i<menu.getItemCount(); i++) {
			if (menu.getItem(i).getLabel().equals(label)) {
				position = i;
				break;
			}
		}
		return position;
	}

	/**
	 * Retrieves text from the system clipboard.
	 *
	 * @return the text contents of the clipboard or an empty string if no text
	 *         could be retrieved
	 */
	public static String getClipboardText() {
		String text = "";
		try {
			final Toolkit toolkit = Toolkit.getDefaultToolkit();
			final Clipboard clipboard = toolkit.getSystemClipboard();
			text = (String)clipboard.getData(DataFlavor.stringFlavor);
		} catch (final Exception e) {
			//if (IJ.debugMode) IJ.handleException(e);
		}
		return text;
	}

	/**
	 * Prints the contents of a directory to a dedicated TextWindow displayed at
	 * the specified location. The location reverts to the center of the screen
	 * if one of the specified coordinates is invalid).
	 * <p>
	 * An error message is displayed in a dialog box if directory could not be
	 * found or if directory is empty (see {@link #fileExists(File)}).
	 * <p>
	 * A {@link bar.FileDrop FileDrop} listener is attached to the
	 * {@link ij.text.TextWindow TextWindow}, which means that, once displayed,
	 * the method can be triggered by dragging and dropping files from the
	 * native file manager.
	 * <p>
	 * Some system files (hidden files, {@code Thumbs.db}) are excluded
	 * from the list. If the directory is empty, users are prompted with the
	 * option to reveal the file in the native file browser (see
	 * {@link #revealFile(String)}).
	 * <p>
	 * Users can double click on a listed file path to have it open by ImageJ.
	 * 
	 * @param dir
	 *            Path of the directory to be listed
	 * @param xPos
	 *            The screen x location (from top-left corner) where the
	 *            TextWindow should be displayed
	 * @param yPos
	 *            The screen y location (from top-left corner) where the
	 *            TextWindow should be displayed
	 *
	 * @see #listDirectory(String)
	 * @see #listDirectory(String, boolean)
	 */
	public static void listDirectory(String dir, int xPos, int yPos) {

		if (!dir.endsWith(File.separator))
			dir += File.separator;

		final File f = new File(dir);
		if (!fileExists(f) || !f.isDirectory())
			return;

		// Define the FileFilter to exclude system files
		final FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(final File f) {
				if (f.isHidden() || f.getName().equals("Thumbs.db"))
					return false;
				return true;
			}
		};

		// Retrieve file list
		final File[] files = f.listFiles(filter);
		if (files.length == 0) {
			if (IJ.showMessageWithCancel("Empty Directory", dir
					+ "\nis empty. Open it?"))
				revealFile(dir);
			return;
		}

		// Create Window and validate positioning
		final int T_WIDTH = 550;
		final int T_HEIGHT = 200;
		final TextWindow tw = new TextWindow(dir + " [All files]", "", T_WIDTH, T_HEIGHT);
		if (xPos > IJ.getScreenSize().getWidth() - (T_WIDTH / 2))
			xPos = -1;
		if (yPos > IJ.getScreenSize().getHeight() - (T_HEIGHT / 2))
			yPos = -1;
		if (xPos > 0 && yPos > 0)
			tw.setLocation(xPos, yPos);

		// Implement drag and drop support. Consider only first file dropped
		new FileDrop(tw, new FileDrop.Listener() {
			@Override
			public void filesDropped(final java.io.File[] files) {
				try {
					final String dir = (files[0].isDirectory()) ? files[0]
							.getCanonicalPath() : files[0].getParent();
					if (dir == null) {
						IJ.error("BAR " + VERSION,
								"Error: Drag and Drop failed...");
						return;
					}
					int xPos, yPos;
					try {
						final java.awt.Point pos = tw.getLocationOnScreen();
						xPos = (int) pos.getX() + 20;
						yPos = (int) pos.getY() + 40;
					} catch (final java.awt.IllegalComponentStateException e) {
						xPos = yPos = -1;
					}
					listDirectory(dir, xPos, yPos);

				} catch (final Exception e) {
					IJ.error("BAR " + VERSION,
							"Error: Drag and Drop failed...");
					return;
				}
			}
		});

		// Populate TextPanel
		final TextPanel tp = tw.getTextPanel();
		final String HEADING = "Double-click on a filename to open it. Drag & "
				+ "drop a folder to generate new lists";
		tp.setColumnHeadings(HEADING);

		final int padDigits = (int) (Math.log10(files.length) + 1);
		for (int i = 0; i < files.length; i++) {
			final String fname = (files[i].isDirectory()) ? files[i].getName()
					+ File.separator : files[i].getName();
			tp.appendWithoutUpdate("" + IJ.pad((i + 1), padDigits) + ": " + dir + fname);
		}

		// Hack: create an empty row as wide as heading to ensure heading is fully visible
		final String spacer = "                                                 "
				+ "                                                             "
				+ "                          ";
		tp.appendWithoutUpdate(spacer);

		tp.updateDisplay();
		IJ.showStatus(String.valueOf(files.length) + " items in " + dir);

	}

	/**
	 * Prints the contents of a directory to a dedicated TextWindow displayed on
	 * the center of the screen.
	 *
	 * @param dir
	 *            the path of the directory to be listed
	 *
	 * @see #listDirectory(String, int, int)
	 * @see #listDirectory(String, boolean)
	 */
	public static void listDirectory(final String dir) {
		listDirectory(dir, -1, -1);
	}

	/**
	 * Prints the contents of a directory to a dedicated TextWindow allowed to
	 * be displayed with a slight offset from frontmost window. This "cascade"
	 * positioning defaults to the center of the screen if frontmost window
	 * could not be retrieved or is too close to screen boundaries.
	 *
	 * @param dir
	 *            the path of the directory to be listed
	 * @param cascade
	 *            if {@code true} {@link ij.text.TextWindow TextWindow}
	 *            "cascades" from ImageJ's
	 *            {@link ij.WindowManager#getFrontWindow() frontmost} window. If
	 *            {@code false}, TextWidow is displayed in the center of
	 *            the screen
	 *
	 * @see #listDirectory(String, int, int)
	 * @see #listDirectory(String)
	 */
	public static void listDirectory(final String dir, final boolean cascade) {
		final java.awt.Frame frame = WindowManager.getFrontWindow();
		int xPos = -1; int yPos = -1;
		if (cascade && frame != null) {
			final java.awt.Point pos = frame.getLocationOnScreen();
			xPos = (int) pos.getX() + 20;
			yPos = (int) pos.getY() + 40;
		}
		listDirectory(dir, xPos, yPos);
	}

	/**
	 * Installs the specified macro file. An error message is displayed in a
	 * dialog box if file path is invalid.
	 *
	 * @param directory
	 *            the directory containing the macro file to be installed
	 * @param filename
	 *            the filename of the macro file to be installed
	 *
	 * @see #fileExists(File)
	 */
	void installMacroFile(final String directory, final String filename) {
		if (directory==null || filename==null) return;
		final String path = directory + File.separator + filename;
		final File f = new File(path);
		if (!fileExists(f)) return;
		final MacroInstaller mi = new MacroInstaller();
		mi.installFile(path);
	}

	/**
	 * Opens the specified file in the IJ2 Script Editor or in the IJ1 built-in
	 * Editor, if the former cannot be found (vanilla IJ1). No tests assessing
	 * the existence of specified file/directory are performed.
	 *
	 * @param dir
	 *            the directory containing the file to be opened
	 * @param filename
	 *            the filename of the file to be opened
	 *
	 * @see #openIJ2Script(File)
	 * @see #openIJ1Script(String, String)
	 */
	public static void openScript(final String dir, final String filename) {
		try {
			Class.forName("net.imagej.ui.swing.script.TextEditor");
			openIJ2Script(new File(dir+filename));
		} catch (final ClassNotFoundException e) {
			openIJ1Script(dir, filename);
		}
	}

	/**
	 * Opens the specified file in the ImageJ1 built-in editor. No tests
	 * assessing the existence of specified file/directory are performed.
	 *
	 * @param dir
	 *            the directory containing the file to be opened
	 * @param filename
	 *            the filename of the file to be opened
	 *
	 * @see #openScript(String, String)
	 * @see #openIJ2Script(File)
	 */
	public static void openIJ1Script(final String dir, final String filename) {
		final Editor ed = (Editor)IJ.runPlugIn("ij.plugin.frame.Editor", "");
		if (ed!=null) ed.open(dir, filename);
	}

	/**
	 * Opens the specified file in the ImageJ2 Script Editor. No tests assessing
	 * the existence of the specified file are performed.
	 *
	 * @param file
	 *            the file to be opened
	 *
	 * @see #openScript(String, String)
	 * @see #openIJ1Script(String, String)
	 */
	public static void openIJ2Script(final File file){
		// retrieve the ImageJ application context
		// https://github.com/imagej/imagej-tutorials/tree/master/call-modern-from-legacy
		final Context context = (Context)IJ.runPlugIn("org.scijava.Context", "");
		final TextEditor editor = new TextEditor(context);
		editor.createNewFromTemplate(file, null);
		//final TextEditor.Tab tab = editor.open(file);
		//tab.setVisible(true); // TODO. Allow document to be opened in tabs!
		editor.setVisible(true);
	}

	/**
	 * Opens the specified file in the "Snippets" directory of BAR. No tests
	 * assessing the existence of the specified file are performed.
	 *
	 * @param filename
	 *            the filename of the script to be opened
	 *
	 * @see #getSnippetsDir()
	 * @see #openScript(String, String)
	 */
	public static void openSnippet(final String filename) {
		openScript(SNIPPETS_DIR , filename);
	}

	/**
	 * Removes {@code $py.class}, {@code .class}, {@code .pyc} and {@code .pyo}
	 * files from the "lib" directory of BAR.
	 *
	 * @see #getLibDir()
	 * @see #zapCompiledLibs()
	 */
	public static void zapCompiledLibs() {
		zapCompiledFiles(new File(LIB_DIR));
	}

	/**
	 * Removes {@code $py.class}, {@code .class}, {@code .pyc} and {@code .pyo}
	 * files from the specified directory.
	 *
	 * @param directory
	 *            the directory to be cleansed
	 *
	 * @see #zapCompiledLibs()
	 */
	public static void zapCompiledFiles(final File directory) {
		if (directory.exists()) {
			final File[] files = directory.listFiles();
			for (final File f : files) {
				if (f.getName().endsWith(".class") || f.getName().endsWith(".pyc") || f.getName().endsWith(".pyo"))
					if (!f.delete()) {
						IJ.log("Could not delete " + f.getName());
					}
			}
		}
	}

	/**
	 * Opens the specified file in the "lib" directory of BAR. No tests
	 * assessing the existence of the specified file are performed.
	 *
	 * @param filename
	 *            the filename of the {@code lib/} file to be opened
	 *
	 * @see #openScript(String, String)
	 * @see #getLibDir()
	 */
	public static void openLib(final String filename) {
		openScript(LIB_DIR, filename);
	}

	/**
	 * Tests whether a file or directory exists. An error
	 * {@link ij.IJ#showMessage message} is displayed in a dialog box if file
	 * cannot be found
	 *
	 * @param file
	 *            the File to be tested
	 * @return {@code true}, if successful.
	 *
	 * @see #fileExists(String)
	 */
	public static boolean fileExists(final File file) {
		final boolean valid = file.exists();
		if (!valid) {
			IJ.showMessage("Invalid file path",
					"Path not found:\n" + splitFilenameAcrossLines(file.toString(), 65)
							+ "\n \nThe file may have been moved, renamed or deleted. If it has been\n"
							+ "deleted and is part of your ImageJ installation, you can use the\n"
							+ "updater to re-install it.");
		}
		return valid;
	}

	// http://stackoverflow.com/questions/7528045/
	static String splitFilenameAcrossLines(final String filename, final int maxLineLength) {
		final String[] tokens = filename.split(File.separator);
		final StringBuilder output = new StringBuilder(filename.length());
		int lineLength = 0;
		for (int i = 0; i < tokens.length; i++) {
			String word = tokens[i];
			if (lineLength + (File.separator + word).length() > maxLineLength) {
				if (i > 0)
					output.append(File.separator).append("\n");
				lineLength = 0;
			}
			if (i < tokens.length - 1
					&& (lineLength + (word + File.separator).length() + tokens[i + 1].length() <= maxLineLength))
				word += File.separator;
			output.append(word);
			lineLength += word.length();
		}
		return output.toString();
	}

	/**
	 * Tests whether a file or directory exists at the specified path. An error
	 * message is displayed in a dialog box if file path is invalid
	 *
	 * @param filepath
	 *            the file path to be tested
	 * @return {@code true}, if successful
	 *
	 * @see #fileExists(File)
	 */
	public static boolean fileExists(final String filepath) {
		return fileExists(new File(filepath));
	}

	/**
	 * "Reveals" the file associated with the specified file path in the
	 * operating system. Described in {@link #revealFile(File)}
	 *
	 * @param filePath
	 *            the file path to be opened by the operating system
	 *
	 * @see #fileExists(File)
	 */
	public static void revealFile(final String filePath) {
		final File file = new File(filePath);
		revealFile(file);
	}

	/**
	 * "Reveals" the specified file path in the operating system. If
	 * {@code file} is a directory, opens it in the file manager of the
	 * operating system, otherwise the application associated with the file type
	 * will be launched. An error message is displayed in a dialog box if file
	 * path is invalid, or if the operating system could not be detected.
	 *
	 * @param file
	 *            the file to be opened by the operating system
	 *
	 * @see #fileExists(File)
	 */
	public static void revealFile(final File file) {
		if (!fileExists(file))
			return;

		boolean supportedOS = false;

		if (Desktop.isDesktopSupported()) {
			try {
				final Desktop desktop = Desktop.getDesktop();
				if (desktop.isSupported(Desktop.Action.OPEN)) {
					desktop.open(file);
					supportedOS = true;
				}
			} catch (final Exception e) {
				IJ.handleException(e);
				supportedOS = false;
			}
		}

		if (!supportedOS) {

			String cmd;
			if (IJ.isLinux())
				cmd = "xdg-open "; //gnome-open should also be tried?
			else if (IJ.isMacOSX())
				cmd = "open ";
			else if (IJ.isWindows())
				cmd = "cmd /c start ";
			else {
				IJ.error("This command does not support your OS. Please report this bug at\n"
						+ SRC_URL);
				return;
			}

			// URIs seem to be the most cross-platform effective way of dealing with
			// spaces in file paths. However, triple slashes seem to be required for
			// proper handling of local files (at least in ubuntu 14.10). This does
			// not seem to be the case with MW (wine) or Mac OS that seem to accept
			// 'file:/' just fine. Since we are dealing with URIs we could use
			// ij.plugin.BrowserLauncher.openURL(uri). Unfortunately that does not
			// seem to work with linux, at least in Ubuntu 14.10 (fresh install)
			String path = file.getPath();
			if (path.contains(" ")) {
				path = file.toURI().normalize().toString();
				if (path.indexOf("file:///")==-1)
					path = path.replace("file:/", "file:///");
			}

			try {
				Runtime.getRuntime().exec(cmd + path);
			} catch (final Exception e) {
				IJ.handleException(e);
			}

		}
	}

	/**
	 * Returns the path to {@code BAR/Analysis/}.
	 *
	 * @return the absolute path to the "Analysis" directory
	 */
	public static String getAnalysisDir() {
		return BAR_DIR + "Analysis" + File.separator;
	}

	/**
	 * Returns the path to {@code BAR/Annotation/}.
	 *
	 * @return the absolute path to the "Annotation" directory
	 */
	public static String getAnnotationDir() {
		return BAR_DIR + "Annotation" + File.separator;
	}

	/**
	 * Returns the path to {@code BAR/Data_Analysis/}.
	 *
	 * @return the absolute path to the "Data_Analysis" directory
	 */
	public static String getDataAnalysisDir() {
		return BAR_DIR + "Data_Analysis" + File.separator;
	}

	/**
	 * Returns the path to {@code BAR/lib/}.
	 *
	 * @return the absolute path to the "lib" directory
	 */
	public static String getLibDir() {
		return LIB_DIR;
	}

	/**
	 * Returns the path to the root directory of BAR.
	 *
	 * @return the absolute path to the root directory of BAR
	 */
	public static String getBARDir() {
		return BAR_DIR;
	}

	/**
	 * Returns the path to {@code BAR/Segmentation/}.
	 *
	 * @return the absolute path to the "Segmentation" directory
	 */
	public static String getSegmentationDir() {
		return BAR_DIR + "Segmentation" + File.separator;
	}

	/**
	 * Returns the path to {@code Bar/Snippets/}.
	 *
	 * @return the absolute path to the "Snippets" directory
	 */
	public static String getSnippetsDir() {
		return SNIPPETS_DIR;
	}

	/**
	 * Returns the URL of BAR's API.
	 *
	 * @return the API (javadoc) URL
	 */
	public static String getJavadocURL() {
		return API_URL;
	}

	/**
	 * Returns the URL of BAR's documentation page.
	 *
	 * @return the documentation URL
	 */
	public static String getDocURL() {
		return DOC_URL;
	}

	/**
	 * Returns the URL of BAR's Git repository.
	 *
	 * @return the GitHub URL of BAR
	 */
	public static String getSourceURL() {
		return SRC_URL;
	}

	/** Checks if table in the "Results" window contains valid data */
	private static boolean validResultsTable() {
		final ResultsTable rt = ResultsTable.getResultsTable();
		return (ResultsTable.getResultsWindow() != null && rt != null && rt.getCounter() != 0);
	}

	/**
	 * Returns a reference to the default IJ Results table. It the Results table
	 * is not displayed or is empty, prompts the user for data to populate it
	 * (by using {@link #getTable(boolean, WindowListener)} to display data in
	 * the "Results" window). This method is thought for IJ macros, since the IJ
	 * macro language can only interact with the "Results" window. Note that any
	 * previous data in the "Results" window will be lost.
	 *
	 * @return A reference to the populated {@code ResultsTable} in the
	 *         "Results" window or {@code null} if chosen source did not contain
	 *         valid data. Note that the IJ1 macro interpreter converts all
	 *         returned objects into their String values
	 *
	 * @see #getTable()
	 */
	public static ResultsTable getResultsTable() {
		try {
			ResultsTable table = ResultsTable.getResultsTable();
			if (table == null || table.getCounter() == 0)
				table = getTable(true, null);
			return table;
		} catch (final Exception ignored) { // useful for IJM calls
			return null;
		}
	}

	/**
	 * Variant of {@link #getTable(boolean, WindowListener)} that ignores
	 * WindowListeners.
	 * 
	 * @return A reference to the chosen {@code ResultsTable} or {@code null} if
	 *         chosen source did not contain valid data
	 *
	 * @see #getTable(boolean, WindowListener)
	 * @see #getResultsTable()
	 */
	public static ResultsTable getTable() {
		return getTable(false, null);
	}

	/**
	 * Returns the ResultsTable associated with the specified TextWindow.
	 *
	 * @param textWindowTitle
	 *            The title of the {@code TextWindow} holding the
	 *            {@code TextPanel} and associated {@code ResultsTable}
	 *
	 * @return The {@code ResultsTable} associated with the specified TextWindow
	 *         title or an empty {@code ResultsTable} if no table exists.
	 *
	 * @see #getTable(boolean, WindowListener)
	 * @see #getResultsTable()
	 */
	public static ResultsTable getTable(final String textWindowTitle) {
		ResultsTable rt = null;
		final TextWindow window = (TextWindow)WindowManager.getFrame(textWindowTitle);
		if (window != null)
			rt = window.getTextPanel().getResultsTable();
		if (rt == null)
			rt = new ResultsTable();
		return rt;
	}

	/**
	 * Prompts the user for tabular data, retrieved from several sources
	 * including 1) Importing a new text/csv file; 2) Trying to import data from
	 * the system clipboard; 3) Importing a demo dataset populated by random
	 * (Gaussian) values; 4) the {@link ResultsTable} of any {@link TextWindow}
	 * or a {@link PlotWindow} currently being displayed in ImageJ. For 1) and
	 * 2) data is displayed in a new TextWindow.
	 *
	 * @param displayInResults
	 *            If {@code true} chosen data is displayed in the "Results"
	 *            window. Useful, since macros (and several plugins) can only
	 *            work with the "Results" window. Note that any previous data in
	 *            the "Results" window will be lost. If {@code false} chosen
	 *            data is displayed on a dedicated window.
	 *
	 * @param listener
	 *            The {@link WindowListener} to be added to the window
	 *            containing data (if retrieval was successful. It is ignored
	 *            when null. Note that the window containing the data can either
	 *            be a {@link TextWindow} or a {@link PlotWindow}
	 *
	 * @return A reference to the chosen {@code ResultsTable} or {@code null} if
	 *         chosen source did not contain valid data
	 *
	 * @see #getResultsTable()
	 * @see #getTable()
	 */
	public static ResultsTable getTable(final boolean displayInResults, final WindowListener listener) {
		return getTable(null, displayInResults, true, listener);
	}

	public static ResultsTable getTable(final Component relativeComponent, final boolean displayInResults, final boolean offerSampleChoice, final WindowListener listener) {

		ResultsTable rt = null;
		final ArrayList<ResultsTable> tables = new ArrayList<>();
		final ArrayList<String> tableTitles = new ArrayList<>();

		// Retrieve tables from all available TextWindows
		final Frame[] windows = WindowManager.getNonImageWindows();
		for (final Frame w : windows) {
			if (w == null)
				continue;
			if (w instanceof TextWindow) {
				final TextWindow rtWindow = (TextWindow) w;
				rt = ((TextWindow) w).getTextPanel().getResultsTable();
				if (rt != null) {
					if (displayInResults && rt == ResultsTable.getResultsTable())
						continue;
					tables.add(rt);
					tableTitles.add(rtWindow.getTitle());
				}
			}
		}

		// Retrieve tables from all available PlotWindows
		final int[] ids = WindowManager.getIDList();
		if (ids != null) {
			for (final int id : ids) {
				final ImagePlus pImp = WindowManager.getImage(id);
				if (pImp == null)
					continue;
				final ImageWindow pWin = pImp.getWindow();
				if (pWin == null)
					continue;
				if (pWin instanceof PlotWindow) {
					rt = ((PlotWindow) pWin).getResultsTable();
					if (rt != null) {
						tables.add(rt);
						tableTitles.add(pWin.getTitle());
					}
				}
			}
		}
		final boolean noTablesOpened = tableTitles.isEmpty();

		// Append options for external sources
		tableTitles.add("External file...");
		tableTitles.add("Clipboard");
		if (offerSampleChoice)
			tableTitles.add("Demo sample of Gaussian values");

		// Make prompt as intuitive as possible
		String gdTitle = "Choose Data Source";
		String subtitle = "Use tabular data from:";
		if (displayInResults) {
			if (validResultsTable()) {
				gdTitle = "Transfer Data to Results Table";
			} else {
				gdTitle = "No Data in Results Table";
			}
			subtitle = "Replace values in \"Results\" table with data from:";
		}

		// Build prompt
		final GenericDialog gd = new GenericDialog(gdTitle);
		final int cols = (tableTitles.size() < 18) ? 1 : 2;
		final int rows = (tableTitles.size() % cols > 0) ? tableTitles.size() / cols + 1 : tableTitles.size() / cols;
		gd.addRadioButtonGroup(subtitle, tableTitles.toArray(new String[tableTitles.size()]), rows, cols,
				tableTitles.get(0));
		// gd.hideCancelButton();
		if (relativeComponent!=null)
			gd.setLocationRelativeTo(relativeComponent);
		gd.showDialog();
		if (gd.wasCanceled())
			return null;

		if (gd.wasOKed()) {

			final String choice = gd.getNextRadioButton();
			String rtTitle;
			Window win = null;

			if (choice.equals("External file...")) {

				rtTitle = (displayInResults) ? "Results" : null;
				try {
					return openAndDisplayTable("", rtTitle, listener);
				} catch (final IOException exc) {
					IJ.error(exc.getMessage());
					return null;
				}

				// Clipboard
			} else if (choice.equals("Clipboard")) {
				final String clipboard = getClipboardText();
				final String error = "Clipboard does not seem to contain valid data";
				if (clipboard == null || clipboard.isEmpty()) {
					IJ.error(error);
					return null;
				}
				try {
					final File temp = File.createTempFile("BARclipboard", ".txt");
					temp.deleteOnExit();
					try (PrintStream out = new PrintStream(temp.getAbsolutePath())) {
						out.println(clipboard);
						out.close();
					} catch (final Exception exc) {
						IJ.error("Could not extract data from clipboard.");
						return null;
					}
					rtTitle = (displayInResults) ? "Results" : "Clipboard Data";
					rt = openAndDisplayTable(temp.getAbsolutePath(), rtTitle, listener, true);
					if (rt == null) {
						IJ.error(error);
						return null;
					}
				} catch (final IOException exc) {
					IJ.error("Could not extract data from clipboard.");
					return null;
				}

			} else if (choice.equals("Demo sample of Gaussian values")) {

				rt = generateGaussianData();
				if (rt != null) {
					rtTitle = (displayInResults) ? "Results" : WindowManager.makeUniqueName("Gaussian Data");
					rt.show(rtTitle);
					win = WindowManager.getFrame(rtTitle);
				}

				// Any other ResultsTable in available TextWindows/PlotWindows
			} else if (!noTablesOpened) {

				rt = tables.get(tableTitles.indexOf(choice));
				if (displayInResults) {
					rt.show("Results");
					win = ResultsTable.getResultsWindow();
				} else {
					win = WindowManager.getFrame(choice);
				}

				// ??
			} else
				return null;

			if (win != null && listener != null)
				win.addWindowListener(listener);
		}

		// Ensure nothing went awry when overriding the "Results" window
		if (displayInResults && !validResultsTable())
			rt = null;

		return rt;

	}

	/**
	 * Opens a tab or comma delimited text file.
	 *
	 * @param path
	 *            The absolute pathname string of the file. A file open dialog
	 *            is displayed if path is {@code null} or an empty string.
	 * @param title
	 *            The title of the window in which data is displayed. The
	 *            filename is used if title is null or an empty string. To avoid
	 *            windows with duplicated titles, title is made unique by
	 *            {@link WindowManager}.
	 * @param listener
	 *            The {@link WindowListener} to be added to the window
	 *            containing data if retrieval was successful. It is ignored
	 *            when {@code null}.
	 * @param silent
	 *            If {@code true} I/O exceptions are silently ignored.
	 *
	 * @return A reference to the opened {link ResultsTable} or {@code null} if
	 *         file could not be open.
	 *
	 * @see #getTable()
	 * @see ij.io.Opener#openTable(String)
	 */
	public static ResultsTable openAndDisplayTable(final String path, final String title, final WindowListener listener, final boolean silent) {
		try {
			return openAndDisplayTable(path, title, listener);
		} catch (final IOException exc) {
			if (!silent)
				IJ.handleException(exc);
			return null;
		}
		
	}

	/**
	 * Opens a tab or comma delimited text file.
	 *
	 * @param path
	 *            The absolute pathname string of the file. A file open dialog
	 *            is displayed if path is {@code null} or an empty string.
	 * @param title
	 *            The title of the window in which data is displayed. The
	 *            filename is used if title is null or an empty string. To avoid
	 *            windows with duplicated titles, title is made unique by
	 *            {@link WindowManager} .
	 * @param listener
	 *            The {@link WindowListener} to be
	 *            added to the window containing data if retrieval was
	 *            successful. It is ignored when {@code null}.
	 * @throws IOException
	 *             if file could not be opened
	 * @return A reference to the opened {link ResultsTable} or {@code null} if
	 *         table was empty.
	 *
	 * @see #getTable()
	 * @see ij.io.Opener#openTable(String)
	 */
	public static ResultsTable openAndDisplayTable(final String path, final String title, final WindowListener listener)
			throws IOException {
		ResultsTable rt = null;
		rt = ResultsTable.open(path);
		if (rt == null || rt.getCounter() == 0) // nothing to be displayed
			return null;
		rt.showRowNumbers(false);
		String rtTitle = (title != null && !title.isEmpty()) ? title : OpenDialog.getLastName();
		rtTitle = WindowManager.makeUniqueName(rtTitle);
		rt.show(rtTitle);
		final TextWindow rtWindow = (TextWindow) WindowManager.getFrame(rtTitle);
		if (rtWindow != null && listener != null)
			rtWindow.addWindowListener(listener);
		return rt;
	}

	/**
	 * Returns a {@link ResultsTable} containing
	 * Gaussian ("normally") distributed values without displaying it.
	 *
	 * @return the {link ResultsTable} containing the demo data
	 *
	 * @see #getTable(boolean, WindowListener)
	 * @see #getTable()
	 * @see #getResultsTable()
	 */
	public static ResultsTable generateGaussianData() {
		final ResultsTable rt = new ResultsTable();
		final double[] m1MeanSD = { 200, 50 };
		final double[] m2MeanSD = { 250, 50 };
		final double[] a1MeanSD = { 350, 100 };
		final double[] a2MeanSD = { 300, 100 };
		final double[] xyMeanSD = { 500, 100 };
		double value;
		for (int i = 0; i < 50; i++) {
			rt.incrementCounter();
			value = new Random().nextGaussian();
			rt.setLabel("Type I", i);
			rt.setValue("Mean", i, m1MeanSD[0] + value * m1MeanSD[1]);
			rt.setValue("Area", i, a1MeanSD[0] + value * a1MeanSD[1]);
			rt.setValue("X", i, xyMeanSD[0] + new Random().nextGaussian() * xyMeanSD[1]);
			rt.setValue("Y", i, xyMeanSD[0] + new Random().nextGaussian() * xyMeanSD[1]);
		}
		for (int i = 50; i < 100; i++) {
			rt.incrementCounter();
			value = new Random().nextGaussian();
			rt.setLabel("Type II", i);
			rt.setValue("Mean", i, m2MeanSD[0] + value * m2MeanSD[1]);
			rt.setValue("Area", i, a2MeanSD[0] + value * a2MeanSD[1]);
			rt.setValue("X", i, xyMeanSD[0] + new Random().nextGaussian() * xyMeanSD[1]);
			rt.setValue("Y", i, xyMeanSD[0] + new Random().nextGaussian() * xyMeanSD[1]);
		}
		return rt;
	}

}
