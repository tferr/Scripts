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
package bar.gui;

import java.io.File;
import java.io.FileFilter;

import net.imagej.ImageJ;

import org.scijava.Context;
import org.scijava.ui.DialogPrompt.MessageType;
import org.scijava.ui.DialogPrompt.OptionType;
import org.scijava.ui.UIService;

import bar.Utils;
import bar.plugin.Commander;
import ij.IJ;

public class GuiUtils {

	private final UIService uiService;

	public GuiUtils(final UIService uiService) {
		this.uiService = uiService;
	}

	/** IJ1 constructor */
	public GuiUtils() {
		final Context context = (Context) IJ.runPlugIn("org.scijava.Context", "");
		uiService = context.getService(UIService.class);
	}

	public void aboutMyRoutines() {
		aboutMyRoutines(null, getMyRoutinesCount());
	}

	public boolean isEmptyMyRoutines() {
		final int count = getMyRoutinesCount();
		if (count == 0)
			aboutMyRoutines("Empty Directory: No Routines Available", count);
		return (count == 0);
	}

	public void browseMyRoutines() {
		if (!isEmptyMyRoutines())
			new Commander("!myr").run();
	}

	public void revealMyRoutines() {
		isEmptyMyRoutines();
		Utils.revealFile(Utils.getMyRoutinesDir());
	}

	public void listMyRoutines() {
		if (!isEmptyMyRoutines())
			Utils.listDirectory(Utils.getMyRoutinesDir());
	}

	private void aboutMyRoutines(String msgTitle, final int fileCount) {
		if (msgTitle == null)
			msgTitle = "BAR: My Routines Directory...";
		final String ordinal = (fileCount == 1) ? "file" : "files";
		final String msg = "<html>The directory <i>My Routines</i> is the place on your local installation where<br>"
				+ "you can save your own scripts to be easily accessed through the BAR<br>"
				+ "menu hierarchy. The directory is not managed by the BAR Update site.<br> <br>"
				+ "Currently the directory contains " + fileCount + " " + ordinal + ".";
		uiService.showDialog(msg, msgTitle, MessageType.INFORMATION_MESSAGE, OptionType.DEFAULT_OPTION);
	}

	public int getMyRoutinesCount() {
		int count = 0;
		try {
			final File dir = new File(Utils.getMyRoutinesDir());
			dir.mkdirs();
			if (dir.exists()) {
				final File[] files = dir.listFiles(new FileFilter() {
					@Override
					public boolean accept(final File f) {
						if (f.isHidden() || f.getName().equals("Thumbs.db"))
							return false;
						return true;
					}
				});
				count = files.length;
			}
		} catch (NullPointerException | SecurityException exc) {
			count = 0;
		}
		return count;
	}

	public static void main(final String... args) {
		final ImageJ ij = net.imagej.Main.launch(args);
		new GuiUtils(ij.ui()).aboutMyRoutines();
	}

}
