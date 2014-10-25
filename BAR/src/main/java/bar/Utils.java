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

	private static final String VERSION = "1.0.2";
	private static final String DOC_URL = "http://fiji.sc/BAR";
	private static final String SRC_URL = "https://github.com/tferr/Scripts#ij-bar";
	private static final String SNIPPETS_DIR = IJ.getDirectory("plugins") + "Scripts"
				+ File.separator + "BAR" + File.separator + "Snippets" + File.separator;

	@Override
	public void run(final String arg) {

		if (arg.startsWith("snippet:")) {

			final String[] args = arg.split(":");
			if (args.length==1) return;
			if (args[1].equalsIgnoreCase("list"))
				listDirectory(SNIPPETS_DIR);
			else if (args[1].equalsIgnoreCase("reveal"))
				revealFile(SNIPPETS_DIR);
			else //TODO implement a "reload snippets" command
				openFile(SNIPPETS_DIR, arg);

		} else if (arg.startsWith("moveMenu:")) {

			final String[] args = arg.split(":");
			if (args.length==1) return;
			moveSubmenu(args[1]);

		} else if (arg.indexOf(".ijm") != -1) {

			if (arg.indexOf("Tools") != -1) // filenames containing "Tools" and "Toolset"
				installToolset(arg);
			else
				installTool(arg);

		} else if (arg.equalsIgnoreCase("about"))
			aboutBox();

	}

	/**
	 * Transfers the specified BAR submenu between the main IJ's menu bar and the
	 * image's context menu (vice-versa if the submenu is already in the context
	 * menu). An acknowledgement message is displayed if !IJ.macroRunning().
	 */
	private void moveSubmenu(final String subMenu) {
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
				IJ.showMessage("BAR v"+ VERSION, "BAR>"+ subMenu +"> transferred to main menu.");

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
	private void listDirectory(final String dir) {

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

		IJ.showStatus("" + counter + " items in " + SNIPPETS_DIR);
		tp.updateDisplay();

	}


	/** Installs a macro file */
	void installMacroFile(final String path) {
		if (path == null) return;
		final File f = new File(path);
		if (!fileExists(f)) return;
		final MacroInstaller mi = new MacroInstaller();
		mi.installFile(path);
	}
	
	/** Installs a macro Tool from macros/tools/ */
	void installTool(final String filename) {
		final String path = IJ.getDirectory("macros") + "tools" + File.separator + filename;
		installMacroFile(path);
	}

	/** Installs a macro Toolset from macros/toolsets/ */
	void installToolset(final String filename) {
		final String path = IJ.getDirectory("macros") + "toolsets" + File.separator + filename;
		installMacroFile(path);
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

	/** Warns users if file cannot be found */
	private boolean fileExists(final File file) {
		if (file.exists())  {
			return true;
		} else {
			IJ.error("File path not found:\n"+ file +"\n \nMake sure it "
					+"exists or use the updater to re-install it.");
			return false;
		}
	}

	/** Reveals a file in the operating system default file explorer */
	private void revealFile(final File file) {
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

	/** Reveals a file in the operating system default file explorer */
	private void revealFile(final String file) {
		revealFile( new File(file) );
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
		final Font plainf = new Font("SansSerif", Font.PLAIN, 12);
		final Font boldf = new Font("SansSerif", Font.BOLD, 12);

		final GenericDialog gd = new GenericDialog("About BAR...");
		gd.addMessage("BAR v" + VERSION, boldf);
		gd.setInsets(0, 20, 0);
		gd.addMessage("A curated collection of Broadly Applicable Routines",
				plainf);
		gd.setInsets(10, 20, 0);
		gd.addMessage("Authors", boldf);
		gd.setInsets(0, 20, 0);
		gd.addMessage("Tiago Ferreira", plainf);
		gd.enableYesNoCancel("Browse Documentation", "Browse Repository");
		gd.hideCancelButton();
		gd.showDialog();
		if (gd.wasCanceled())
			return;
		else if (gd.wasOKed())
			this.openURL(DOC_URL);
		else
			this.openURL(SRC_URL);
	}

}
