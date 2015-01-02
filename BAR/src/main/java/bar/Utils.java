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
import ij.plugin.BrowserLauncher;
import ij.plugin.MacroInstaller;
import ij.plugin.PlugIn;
//import ij.plugin.frame.Editor;
import ij.text.TextPanel;
import ij.text.TextWindow;

import java.awt.Desktop;
import java.awt.Font;
import java.awt.Menu;
import java.awt.PopupMenu;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;


/** Simple commands related to the BAR update site */
public class Utils implements PlugIn {

	private static final String VERSION = "1.0.6-DEV";
	private static final String DOC_URL = "http://fiji.sc/BAR";
	private static final String SRC_URL = "https://github.com/tferr/Scripts#ij-bar";
	private static final String BAR_DIR = IJ.getDirectory("plugins")
			+ "Scripts" + File.separator + "BAR";
	private static final String SNIPPETS_DIR = BAR_DIR + File.separator
			+ "Snippets" + File.separator;

	@Override
	public void run(final String arg) {

		if (arg.startsWith("snippet:")) {

			final String[] args = arg.split(":");
			if (args.length==1) return;
			if (args[1].equalsIgnoreCase("list")) {
				shiftClickWarning();
				listDirectory(SNIPPETS_DIR);
			} else if (args[1].equalsIgnoreCase("reveal")) {
				shiftClickWarning();
				revealFile(SNIPPETS_DIR);
			} else //TODO implement a "reload snippets" command
				openFile(SNIPPETS_DIR, arg);

		} else if (arg.startsWith("moveMenu:")) {

			final String[] args = arg.split(":");
			if (args.length==1) return;
			moveSubmenu(args[1]);

		} else if (arg.endsWith(".ijm")) {

			final String[] args = arg.split("/");
			if (args.length==1) return;
			final String dir = IJ.getDirectory("macros") + args[0] + File.separator;
			if (IJ.shiftKeyDown()) {
				openFile(dir, args[1]);
			} else {
				installMacroFile(dir, args[1]);
			}

		} else if (arg.equalsIgnoreCase("about"))
			aboutBox();

	}

	/** Displays a warning on Shift-Click being disabled */
	static void shiftClickWarning() {
		if (IJ.shiftKeyDown()) {
			IJ.beep();
			IJ.showStatus("Compiled plugin. Use 'About BAR...' to access source code repository.");
		}
	}

	/**
	 * Transfers the specified BAR submenu between the main IJ's menu bar and the
	 * image's context menu (vice-versa if the submenu is already in the context
	 * menu). An acknowledgement message is displayed if !IJ.macroRunning().
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

	/** Prints the contents of a directory to a dedicated table. */
	public static void listDirectory(final String dir) { // static so it can be called from ijm
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
				Utils.revealFile(dir);
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


	/** Installs a macro file */
	void installMacroFile(final String directory, final String filename) {
		if (directory==null || filename==null) return;
		final String path = directory + File.separator + filename;
		final File f = new File(path);
		if (!fileExists(f)) return;
		final MacroInstaller mi = new MacroInstaller();
		mi.installFile(path);
	}

	/** Opens a file in the Script Editor. */
	void openFile(final String dir, final String filename) {
		final File f = new File(dir+filename);
		if (!fileExists(f)) return;
		//final Editor ed = (Editor)IJ.runPlugIn("ij.plugin.frame.Editor", "");
		//if (ed!=null) ed.open(filedir, filename);

		// The following works with both the built-in editor and Fiji's Script Editor
		IJ.run("Edit...", "open=["+ dir + filename +"]");
	}

	/** Checks for a valid file path, warning users if it cannot be found */
	private static boolean fileExists(final File file) {
		final boolean valid = file.exists();
		if (!valid) {
			IJ.showMessage("Invalid path or filename", "<html>Path not found:"
				+ "<p style='width:300px;'><i>"+ file +"</i></p> <p><p>"
				+ "If the file has been deleted and is part of your Fiji installation<p>"
				+ "you can use the updater to re-install it.");
		}
		return valid;
	}

	/** Reveals a file in the operating system default file explorer */
	public static void revealFile(final String filePath) { // static so it can be called from ijm
		final File file = new File(filePath);
		if (!fileExists(file)) return;
		Desktop desktop = null;
		if (Desktop.isDesktopSupported())
			desktop = Desktop.getDesktop();
		try {
			desktop.open(file);
		} catch (final IOException e) {
			IJ.log(">>>> An error occured when opening\n"+ file +"\n"+ e);
		}
	}

	/** Opens an URL in the default browser */
	void openURL(final String URL) {
		try {
			BrowserLauncher.openURL(URL);
		} catch (final IOException e) {
			IJ.log(">>>> An error occurred when opening\n"+ URL +":\n"+ e);
		}
	}

	/** Implements the About BAR... command */
	void aboutBox() {
		shiftClickWarning();
		final Font plainf = new Font("SansSerif", Font.PLAIN, 12);
		final Font boldf = new Font("SansSerif", Font.BOLD, 12);

		final GenericDialog gd = new GenericDialog("About BAR...");
		gd.addMessage("BAR v" + VERSION, boldf);
		gd.setInsets(0, 20, 0);
		gd.addMessage("A curated collection of Broadly Applicable Routines for ImageJ1",
				plainf);
		gd.setInsets(10, 20, 0);
		gd.addMessage("Author/Maintainer", boldf);
		gd.setInsets(0, 20, 0);
		gd.addMessage("Tiago Ferreira", plainf);
		gd.addMessage("Contributors", boldf);
		gd.setInsets(0, 20, 0);
		gd.addMessage("Johannes Schindelin, Kota Miura, Wayne Rasband, Maxime Pinchon,\n"
				+"Jérôme Mutterer", plainf);
		gd.enableYesNoCancel("Browse Repository", "Open BAR");
		gd.hideCancelButton();
		gd.addHelp(DOC_URL);
		gd.setHelpLabel("Browse Documentation");
		gd.showDialog();
		if (gd.wasCanceled())
			return;
		else if (gd.wasOKed())
			this.openURL(SRC_URL);
		else
			Utils.revealFile(BAR_DIR);
	}

}
