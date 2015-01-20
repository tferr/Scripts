/*
 * IJ BAR: https://github.com/tferr/Scripts
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation (http://www.gnu.org/licenses/gpl.txt).
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */
package bar.plugin;

import ij.IJ;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.gui.MultiLineLabel;
import ij.gui.NonBlockingGenericDialog;
import ij.plugin.PlugIn;

import java.awt.AWTEvent;
import java.awt.Button;
import java.awt.Choice;
import java.awt.Color;
import java.awt.TextArea;
import java.awt.TextField;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bar.Utils;

/** Generates templates for new IJ scripts */
public class SnippetCreator implements PlugIn, DialogListener {

	/* Definitions */
	private static final String[] S_TYPES = { "[Select from list]",
			"BeanShell", "Clojure", "Groovy", "IJ1 Macro", "JavaScript",
			"Python", "Ruby" };
	private static final String[] S_EXTS = { "", ".bsh", ".clj", ".groovy",
			".ijm", ".js", ".py", ".rb" };
	private static final String[] C_CHARS = { "", "//", ";", "//", "//", "//",
			"#", "#" };
	private static final int NN = 0;
	private static final int BSH = 1;
	private static final int CLJ = 2;
	private static final int GRV = 3;
	private static final int IJM = 4;
	private static final int JS = 5;
	private static final int PY = 6;
	private static final int RB = 7;

	/* Default parameters for new snippet */
	private static String sContents = "";
	private static String sFilename = "My_Snippet";
	private static int sType = 0;


	/** Prompts user for a new snippet that will be saved in BAR/Snippets/ */
	public void run(final String arg) {
		if (showDialog())
			saveAndOpenSnippet();
	}

	/* Returns a header common to all file types */
	private static StringBuilder commonHeader(final int type) {
		final Date date = new Date();
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
		final StringBuilder sb = new StringBuilder();
		sb.append(C_CHARS[type]).append(" File created on ")
				.append(sdf.format(date)).append(", IJ1 ")
				.append(IJ.getFullVersion()).append("\n\n");
		return sb;
	}

	/** Returns header for a BSH snippet (BeanShell) */
	public static String bshHeader() {
		final StringBuilder sb = commonHeader(BSH);
		sb.append("/* Add BAR/lib to classpath. Load BARlib.bsh */\n");
		sb.append("addClassPath(bar.Utils.getBARDir());\n");
		sb.append("importCommands(\"lib/\");\n");
		sb.append("BARlib();\n");
		sb.append("\n");
		sb.append("lib = new BARlib();\n");
		sb.append("// Confirm availability of BARlib\n");
		sb.append("lib.confirmLoading();\n");
		return sb.toString();
	}

	/** Returns header for a CLJ snippet (Clojure) */
	public static String cljHeader() {
		final StringBuilder sb = commonHeader(CLJ);
		return sb.toString();
	}

	/** Returns header for a GRV snippet (Groovy) */
	public static String grvHeader() {
		final StringBuilder sb = commonHeader(GRV);
		return sb.toString();
	}

	/** Returns header for a IJM snippet (IJ1 macro) */
	public static String ijmHeader() {
		final StringBuilder sb = commonHeader(IJM);
		sb.append("libPath = call('bar.Utils.getLibDir') + 'BARlib.ijm';\n");
		sb.append("libFunctions = File.openAsString(libPath);\n");
		sb.append("call('ij.macro.Interpreter.setAdditionalFunctions', libFunctions);\n");
		sb.append("// Confirm availability of new additions\n");
		sb.append("confirmLoading();");
		return sb.toString();
	}

	/** Returns header for a JS snippet (JavaScript) */
	public static String jsHeader() {
		final StringBuilder sb = commonHeader(JS);
		return sb.toString();
	}

	/** Returns header for a PY snippet (Python) */
	public static String pyHeader() {
		final StringBuilder sb = commonHeader(PY);
		sb.append("import sys, bar\n");
		sb.append("sys.path.append(bar.Utils.getLibDir())\n");
		sb.append("import BARlib as lib\n");
		sb.append("# Confirm availability of BARlib\n");
		sb.append("lib.confirmLoading()\n");
		return sb.toString();
	}

	/** Returns header for a RB snippet (Ruby) */
	public static String rbHeader() {
		final StringBuilder sb = commonHeader(RB);
		return sb.toString();
	}

	/** Returns header for a NN snippet (No language) */
	public static String nnHeader() {
		final StringBuilder sb = commonHeader(NN);
		return sb.toString();
	}

	/* Saves and opens the snippet created by the prompt */
	private void saveAndOpenSnippet() {
		IJ.saveString(sContents, Utils.getSnippetsDir() + sFilename);
		Utils.openScript(Utils.getSnippetsDir(), sFilename);
	}

	/* Matches leading dot, ":", slashes, etc. in filename */
	private boolean invalidFilename(final String filename) {
		final String FILE_PATTERN = "^\\.|[/:\\\\]";
		final Pattern pattern = Pattern.compile(FILE_PATTERN);
		final Matcher matcher = pattern.matcher(filename);
		return matcher.find();
	}

	/* Displays prompt */
	private boolean showDialog() {
		final GenericDialog gd = new NonBlockingGenericDialog("New Snippet");
		gd.setInsets(5, 10, 0);
		gd.addTextAreas(sContents, null, 12, 50);
		gd.addStringField("Filename:", sFilename, 18);
		gd.addChoice("Language:", S_TYPES, S_TYPES[sType]);
		gd.setInsets(-85, 255, 0);
		gd.addMessage(" \n "); // placeholder for info messages
		gd.addHelp(Utils.getDocURL());
		gd.setHelpLabel("Online Help");
		gd.setOKLabel("  Create and Open  ");
		gd.addDialogListener(this);
		gd.showDialog();

		sContents = gd.getNextText();
		return !gd.wasCanceled();
	}

	/* Retrieves parameters from prompt in an interactive way */
	public boolean dialogItemChanged(final GenericDialog gd, final AWTEvent e) {
		final Object source = (e == null) ? null : e.getSource();
		final Vector<?> choices = gd.getChoices();
		final Vector<?> fields = gd.getStringFields();
		final Button[] buttons = gd.getButtons();
		final Choice fChoice = (Choice) choices.elementAt(0);
		final TextField fField = (TextField) fields.elementAt(0);
		final Button okButton = buttons[0];

		sFilename = gd.getNextString();
		sType = gd.getNextChoiceIndex();

		// Populate text area
		if (source == fChoice) {
			String header = "";
			switch (sType) {
			case BSH:
				header = bshHeader();
				break;
			case CLJ:
				header = cljHeader();
				break;
			case GRV:
				header = grvHeader();
				break;
			case IJM:
				header = ijmHeader();
				break;
			case JS:
				header = jsHeader();
				break;
			case PY:
				header = pyHeader();
				break;
			case RB:
				header = rbHeader();
				break;
			default:
				header = nnHeader();
				break;
			}
			if (header != "") {
				final TextArea ta = gd.getTextArea1();
				ta.insert(header, ta.getCaretPosition());
				if (IJ.isMacOSX())
					ta.requestFocus();
			}

			// Ensure adequate filename extension
			if (!sFilename.endsWith(S_EXTS[sType])) {
				final int index = sFilename.lastIndexOf(".");
				if (index > -1)
					sFilename = sFilename.substring(0, index);
				sFilename += S_EXTS[sType];
				fField.setText(sFilename);
			}

		}

		// Adjust labels and fields
		final File f = new File(Utils.getSnippetsDir() + sFilename);
		final boolean invalidName = invalidFilename(sFilename);
		okButton.setLabel(f.exists() ? "Overwrite and Open" : "  Create and Open  ");
		fField.setForeground((f.exists() || invalidName) ? Color.RED
				: Color.BLACK);

		// Adjust messages
		final MultiLineLabel label = (MultiLineLabel) gd.getMessage();
		final StringBuilder sb = new StringBuilder();
		if (invalidName) {
			sb.append("\nInvalid Filename");
		} else if (f.exists()) {
			sb.append("File already exists in BAR/Snippets!");
		} else if (sFilename.indexOf("_") == -1) {
			sb.append("\nFile does not contain an underscore");
			sb.append("\nand will not be listed in the IJ Menu.");
		} else {
			sb.append("\nFile will be listed in the BAR Menu.");
		}
		label.setText(sb.toString());

		return !invalidName;
	}

}
