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
import ij.gui.GenericDialog;
import ij.plugin.MacroInstaller;
import ij.plugin.PlugIn;

import java.awt.Font;
import java.io.File;

/** Simple commands related to the BAR update site */
public class Utils implements PlugIn {

	private static final String VERSION = "1.0.2";
	private static final String DOC_URL = "http://fiji.sc/BAR";
	private static final String SRC_URL = "https://github.com/tferr/Scripts#ij-bar";

	@Override
	public void run(final String arg) {
		if (arg.equalsIgnoreCase("about"))
			aboutBox();
		else if (arg.indexOf(".ijm") != -1) {
			if (arg.indexOf("Tools") != -1) // filenames containing "Tools" and "Toolset"
				installToolset(arg);
			else
				installTool(arg);
		}
	}
	
	/** Installs a macro file */
	void installMacroFile(final String path) {
		if (path == null) return;
		final File f = new File(path);
		if (f.exists()) {
			final MacroInstaller mi = new MacroInstaller();
			mi.installFile(path);
		} else
			IJ.error("File not found", path +"\nnot found. Make sure the "
					+"file exists or use the updater to re-install it.");
	}
	
	/** Installs a macro Tool from macros/tools/ */
	void installTool(final String filename) {
		final String path = IJ.getDirectory("macros") + "tools" + File.separator + filename;
		installMacroFile(path);
	}

	/** Installs a toolset from macros/toolsets/ */
	void installToolset(final String filename) {
		final String path = IJ.getDirectory("macros") + "toolsets" + File.separator + filename;
		installMacroFile(path);
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
			IJ.runPlugIn("ij.plugin.BrowserLauncher", DOC_URL);
		else
			IJ.runPlugIn("ij.plugin.BrowserLauncher", SRC_URL);
	}

}
