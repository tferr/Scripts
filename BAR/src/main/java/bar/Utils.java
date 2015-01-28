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

import ij.IJ;
import ij.Menus;
import ij.gui.GenericDialog;
import ij.plugin.MacroInstaller;
import ij.plugin.PlugIn;
import ij.plugin.frame.Editor;
import ij.text.TextPanel;
import ij.text.TextWindow;

import java.awt.Desktop;
import java.awt.Font;
import java.awt.Menu;
import java.awt.PopupMenu;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import net.imagej.ui.swing.script.TextEditor;
import net.imagej.ui.swing.script.TextEditor.Tab;

import org.scijava.Context;


/** A collection of miscellaneous utilities for BAR */
public class Utils implements PlugIn {

	static final String VERSION = "1.0.6a-DEV";
	static final String DOC_URL = "http://fiji.sc/BAR";
	static final String SRC_URL = "https://github.com/tferr/Scripts";
	static final String BAR_DIR = IJ.getDirectory("plugins")
			+ "Scripts" + File.separator + "BAR" + File.separator;
	static final String SNIPPETS_DIR = BAR_DIR +"Snippets" + File.separator;
	static final String LIB_DIR = BAR_DIR + "lib" + File.separator;


	@Override
	public void run(final String arg) {

		shiftClickWarning();
		if (arg.equalsIgnoreCase("about")) {
			aboutBox();
		} else {

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
	}

	/**
	 * Displays a status-bar warning on "open by Shift-click" being disabled for
	 * pre-compiled plugins
	 */
	public static void shiftClickWarning() {
		if (IJ.shiftKeyDown()) {
			IJ.beep();
			IJ.showStatus("Compiled plugin. Use 'About BAR...' to access source code repository.");
		}
	}

	/**
	 * Transfers the specified BAR submenu between the main IJ's menu bar and
	 * the image's context menu (vice-versa if the submenu is already in the
	 * context menu). An acknowledgement message is displayed if
	 * !IJ.macroRunning().
	 */
	private void moveSubmenu(final String subMenu) {
		shiftClickWarning();
		final Menu barMenu = Menus.getImageJMenu("BAR");
		final PopupMenu popMenu = Menus.getPopupMenu();

		final Integer popmenuPos = getMenuItem(popMenu, subMenu);
		final Integer barmenuPos = getMenuItem(barMenu, subMenu);
		if (popmenuPos==null || barmenuPos==null) {
			IJ.error("BAR v"+ VERSION +" Error", "Some menu items are not accessible.\n"
					+"Perhaps the image's context menu is disabled?");
			return;
		}

		if (popmenuPos==-1) { // parent is MenuBar

			popMenu.addSeparator();
			popMenu.add(barMenu.getItem(barmenuPos));
			removeLastSeparator(barMenu);
			if (!IJ.macroRunning())
				IJ.showMessage("BAR v"+ VERSION, ""+ subMenu +"> transferred to context menu.\n"
						+"Right-click on the image canvas to access it.");

		} else { // parent is PopupMenu

			barMenu.addSeparator();
			barMenu.add(popMenu.getItem(popmenuPos));
			removeLastSeparator(popMenu);
			if (!IJ.macroRunning())
				IJ.showStatus("BAR>"+ subMenu +"> transferred to main menu.");

		}
	}

	/** Removes any trailing separator(s) from the specified menu. */
	private void removeLastSeparator(final Menu menu) {
		if (menu==null) return;
		final int lastItem = menu.getItemCount()-1;
		if (menu.getItem(lastItem).getLabel().equals("-")) {
			menu.remove(lastItem);
			removeLastSeparator(menu);
		}
	}

	/**
	 * Returns the index of the MenuItem labeled by the specified string.
	 * Returns -1 if no match is found, null if menu is not available.
	 **/
	private Integer getMenuItem(final Menu menu, final String label) {
		int position = -1;
		if (menu==null) {
			return null;
		} else {
			for (int i=0; i<menu.getItemCount(); i++) {
				if (menu.getItem(i).getLabel().equals(label)) {
					position = i;
					break;
				}
			}
		}
		return position;
	}

	/**
	 * Returns text from the system clipboard or an empty string if no text
	 * could be retrieved.
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
	 * Prints the contents of a directory to a dedicated IJ1 table. An error is
	 * message is displayed in a dialog box if directory could not be found.
	 * Some system files (dot files, Thumbs.db, ...) are excluded from the list.
	 */
	public static void listDirectory(final String dir) {
		// Method must be static so it can be called from ijm

		final File f = new File(dir);
		if (!fileExists(f) || !f.isDirectory()) return;

		final String[] files = f.list();
		final ArrayList<String> list = new ArrayList<String>();
		for (int i=0; i<files.length; i++) {
			final String file = files[i];
			if (file.startsWith(".") || file.equals("Thumbs.db")) //TODO exclude other files?
				continue;
			if ( (new File(dir+file)).isDirectory() )
				list.add(file + File.separator);
			else
				list.add(file);
		}

		if (list.size()==0) {
			if (IJ.showMessageWithCancel("Empty Directory", dir +"\nis empty. Open it?"))
				revealFile(dir);
			return;
		}

		final TextWindow tw = new TextWindow(dir,"", 550, 200);
		final TextPanel tp = tw.getTextPanel();
		tp.setColumnHeadings("Double-click on a filename to open it");

		Collections.sort(list);
		final Iterator<String> it = list.listIterator();
		int counter = 1;
		while (it.hasNext())
			tw.append( ""+ IJ.pad(counter++,2) +": "+ dir + it.next() );

		IJ.showStatus("" + (counter-1) + " items in " + SNIPPETS_DIR);
		tp.updateDisplay();

	}


	/**
	 * Installs a macro file. An error message is displayed in a dialog box if
	 * filepath is invalid.
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
	 * the existence of file or directory are performed.
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
	 * assessing the existence of file or directory are performed.
	 */
	public static void openIJ1Script(final String dir, final String filename) {
		final Editor ed = (Editor)IJ.runPlugIn("ij.plugin.frame.Editor", "");
		if (ed!=null) ed.open(dir, filename);
	}

	/**
	 * Opens the specified file in the ImageJ2 Script Editor. No tests assessing
	 * whether file exists are performed.
	 */
	public static void openIJ2Script(final File file){
		// retrieve the ImageJ application context
		// https://github.com/imagej/imagej-tutorials/tree/master/call-modern-from-legacy
		final Context context = (Context)IJ.runPlugIn("org.scijava.Context", "");
		final TextEditor editor = new TextEditor(context);
		final Tab tab = editor.open(file);
		editor.setVisible(true);
		tab.setVisible(true);
	}

	/**
	 * Opens the specified file in the "Snippets" directory of BAR. No tests
	 * validating filename path are performed.
	 */
	public static void openSnippet(final String filename) {
		openScript(SNIPPETS_DIR , filename);
	}

	/**
	 * Opens the specified file in the "lib" directory of BAR. No tests
	 * validating filename path are performed.
	 */
	public static void openLib(final String filename) {
		openScript(LIB_DIR, filename);
	}

	/**
	 * Tests whether a file or directory exists. An error message is displayed
	 * in a dialog box if file path is invalid
	 * @see {@link #revealFile(File) revealFile(file)}
	 */
	public static boolean fileExists(final File file) {
		final boolean valid = file.exists();
		final int WIDTH = 400;
		if (!valid) {
			IJ.showMessage("Invalid path or filename", "<html><div WIDTH="+ WIDTH +">"
					+"Path not found:<br><i>"+ file.toString() +"</i><br><br>"
					+"If the file has been deleted and is part of your Fiji "
					+"installation, you can use the updater to re-install it."
					+"</div><html>");
		}
		return valid;
	}

	/**
	 * Tests whether a file or directory exists at the specified path. An error
	 * message is displayed in a dialog box if file path is invalid
	 * @see {@link #revealFile(File) revealFile(file)}
	 */
	public static boolean fileExists(final String filepath) {
		return fileExists(new File(filepath));
	}

	/**
	 * "Reveals" the specified file path in the operating system. Described in
	 * {@link #revealFile(File) revealFile(file)}
	 */
	public static void revealFile(final String filePath) {
		final File file = new File(filePath);
		revealFile(file);
	}

	/**
	 * "Reveals" the specified file path in the operating system. If file is a
	 * directory, opens it in the file manager of the operating system,
	 * otherwise the application associated with the file type will be launched.
	 * An error message is displayed in a dialog box if file path is invalid, or
	 * if the operating system could not be detected.
	 *
	 * @see {@link #revealFile(String) revealFile(filePath)}
	 * @see {@link #fileExists(String)}
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

	/** Returns the path to BAR/Analysis/ */
	public static String getAnalysisDir() {
		return BAR_DIR + "Analysis" + File.separator;
	}

	/** Returns the path to BAR/Annotation/ */
	public static String getAnnotationDir() {
		return BAR_DIR + "Annotation" + File.separator;
	}

	/** Returns the path to BAR/Data_Analysis/ */
	public static String getDataAnalysisDir() {
		return BAR_DIR + "Data_Analysis" + File.separator;
	}

	/** Returns the path to BAR/lib/ */
	public static String getLibDir() {
		return LIB_DIR;
	}

	/** Returns the path to BAR/Morphometry */
	public static String getMorphometryDir() {
		return BAR_DIR + "Morphometry" + File.separator;
	}

	/** Returns the path to the root directory of BAR */
	public static String getBARDir() {
		return BAR_DIR;
	}

	/** Returns the path to BAR/Segmentation/ */
	public static String getSegmentationDir() {
		return BAR_DIR + "Segmentation" + File.separator;
	}

	/** Returns the path to Bar/Snippets/ */
	public static String getSnippetsDir() {
		return SNIPPETS_DIR;
	}

	/** Returns BAR version */
	public static String getVersion() {
		return VERSION;
	}

	/** Returns the URL of BAR's documentation page */
	public static String getDocURL() {
		return DOC_URL;
	}

	/** Returns the URL of BAR's Git repository */
	public static String getSourceURL() {
		return SRC_URL;
	}

	/** Implements the About BAR... command */
	void aboutBox() {
		shiftClickWarning();
		final Font plainf = new Font("SansSerif", Font.PLAIN, 12);
		final Font boldf = new Font("SansSerif", Font.BOLD, 12);

		final GenericDialog gd = new GenericDialog("About BAR...");
		gd.addMessage("BAR v" + VERSION, boldf);
		gd.setInsets(0, 20, 0);
		gd.addMessage("A curated collection of Broadly Applicable Routines for ImageJ",
				plainf);
		gd.setInsets(10, 20, 0);
		gd.addMessage("Author/Maintainer", boldf);
		gd.setInsets(0, 20, 0);
		gd.addMessage("Tiago Ferreira", plainf);
		gd.addMessage("Contributors", boldf);
		gd.setInsets(0, 20, 0);
		gd.addMessage("Johannes Schindelin, Kota Miura, Wayne Rasband, Maxime Pinchon,\n"
				+"Jérôme Mutterer", plainf);
		gd.enableYesNoCancel("Open Repository", "Open BAR");
		gd.addHelp(DOC_URL);
		gd.setHelpLabel("Online Help");
		gd.setCancelLabel("Dismiss");
		gd.showDialog();
		if (gd.wasCanceled())
			return;
		else if (gd.wasOKed())
			IJ.runPlugIn("ij.plugin.BrowserLauncher", SRC_URL);
		else
			revealFile(BAR_DIR);
	}

}
