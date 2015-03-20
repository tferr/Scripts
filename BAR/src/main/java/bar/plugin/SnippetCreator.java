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
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bar.Utils;

/** Generates templates for new IJ scripts */
public class SnippetCreator implements PlugIn, DialogListener, ActionListener {

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

	private static GenericDialog gd;
	private static Button paste, load, list;
	private static MultiLineLabel infoMsg;
	private static TextArea ta;


	/** Prompts user for a new snippet that will be saved in BAR/Snippets/ */
	public void run(final String arg) {
		if (showDialog()) {
			if (sContents.length()>0)
				saveAndOpenSnippet();
			else
				IJ.showStatus(sFilename +" was empty. No file was saved...");
		}

	}

	/* Returns a header common to all file types */
	private static StringBuilder commonHeader(final StringBuilder sb, final int type) {
		final Date date = new Date();
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
		sb.append(C_CHARS[type]).append(" File created on ")
				.append(sdf.format(date)).append(", IJ ")
				.append(IJ.getFullVersion()).append("\n");
		switch (type) {
		case NN:
		case IJM:
			break;
		default:
			sb.append(C_CHARS[type])
				.append(" See http://javadoc.imagej.net for IJ documentation \n\n");
			break;
		}
		return sb;
	}

	/* Returns a header common to all file types */
	private static StringBuilder commonHeader(final int type) {
		final StringBuilder sb = new StringBuilder();
		return commonHeader(sb, type);
	}

//	/* Returns a file header for a language not featured in BAR */
//	private static StringBuilder unsupportedHeader(final StringBuilder sb, final int type) {
//		sb.append(C_CHARS[type]).append(" NB: BAR does not yet feature a ")
//				.append(S_TYPES[type]).append(" lib\n\n");
//		return sb;
//	}

	/** Returns header for a BSH snippet (BeanShell) */
	public static String bshHeader() {
		final StringBuilder sb = commonHeader(BSH);
		sb.append("// Add BAR/lib to classpath and load BARlib.bsh\n");
		sb.append("addClassPath(bar.Utils.getBARDir());\n");
		sb.append("importCommands(\"lib/\");\n");
		sb.append("BARlib();\n");
		sb.append("\n");
		sb.append("// Initiate BARlib and confirm its availability\n");
		sb.append("lib = new BARlib();\n");
		sb.append("lib.confirmLoading();\n");
		return sb.toString();
	}

	/** Returns header for a CLJ snippet (Clojure) */
	public static String cljHeader() {
		final StringBuilder sb = commonHeader(CLJ);
		sb.append(";; Load BARlib.clj\n");
		sb.append("(load-file (str (bar.Utils/getLibDir) \"BARlib.clj\"))\n");
		sb.append("\n");
		sb.append(";; Confirm BARlib availability\n");
		sb.append("(confirmLoading)\n");
		return sb.toString();
	}

	/** Returns header for a GRV snippet (Groovy) */
	public static String grvHeader() {
		final StringBuilder sb = commonHeader(GRV);
		sb.append("// Parse and load BARlib.groovy\n");
		sb.append("import bar.Utils\n");
		sb.append("file = new File(Utils.getLibDir() + \"BARlib.groovy\")\n");
		sb.append("BARlib = new GroovyClassLoader(getClass().getClassLoader()).parseClass(file)\n");
		sb.append("\n");
		sb.append("// Initiate BARlib and confirm its availability\n");
		sb.append("lib = BARlib.newInstance()\n");
		sb.append("lib.confirmLoading()\n");
		return sb.toString();
	}

	/** Returns header for a IJM snippet (IJ1 macro) */
	public static String ijmHeader() {
		final StringBuilder sb = commonHeader(IJM);
		sb.append("// Load BARlib.ijm. NB: functions may only be available once\n");
		sb.append("// a new instance of the macro interpreter is initiated. See\n");
		sb.append("// http://fiji.sc/BAR#FAQ for details\n");
		sb.append("libPath = call('bar.Utils.getLibDir') + 'BARlib.ijm';\n");
		sb.append("libContents = File.openAsString(libPath);\n");
		sb.append("call('ij.macro.Interpreter.setAdditionalFunctions', libContents);\n");
		sb.append("\n");
		sb.append("// Confirm availability of new additions\n");
		sb.append("confirmLoading();");
		return sb.toString();
	}

	/** Returns header for a JS snippet (JavaScript) */
	public static String jsHeader() {
		final StringBuilder sb = commonHeader(JS);
		sb.append("// Load BARlib.js\n");
		sb.append("importClass(Packages.bar.Utils);\n");
		sb.append("load(Utils.getLibDir() +\"BARlib.js\");\n");
		sb.append("\n");
		sb.append("// Initiate BARlib and confirm its availability\n");
		sb.append("lib = new BARlib();\n");
		sb.append("lib.confirmLoading();\n");
		return sb.toString();
	}

	/** Returns header for a PY snippet (Python) */
	public static String pyHeader() {
		final StringBuilder sb = commonHeader(PY);
		sb.append("# Load BARlib.py\n");
		sb.append("import sys\n");
		sb.append("from bar import Utils as barUtils\n");
		sb.append("sys.path.append(barUtils.getLibDir())\n");
		sb.append("import BARlib as lib\n");
		sb.append("\n");
		sb.append("# Confirm availability of BARlib\n");
		sb.append("lib.confirmLoading()\n");
		return sb.toString();
	}

	/** Returns header for a RB snippet (Ruby) */
	public static String rbHeader() {
		StringBuilder sb = new StringBuilder();
		//sb.append("# @AppService appService\n");
		//sb.append("require_relative \"#{$appService.getApp.getBaseDirectory}/plugins/JRuby/imagej.rb\"\n");
		//sb.append("\n");
		sb = commonHeader(sb, RB);
		sb.append("# Load BARlib.rb\n");
		sb.append("java_import \"bar.Utils\"\n");
		sb.append("require \"#{Utils.getLibDir}\" + \"BARlib.rb\"\n");
		sb.append("\n");
		sb.append("# Initiate BARlib and confirm its availability\n");
		sb.append("lib = BARlib.new()\n");
		sb.append("lib.confirmLoading\n");
		return sb.toString();
	}

	/** Returns header for a NN snippet (No language) */
	public static String nnHeader() {
		final StringBuilder sb = commonHeader(NN);
		return sb.toString();
	}

	/* Saves and opens the snippet created by the prompt */
	private void saveAndOpenSnippet() {
		final String result = IJ.saveString(sContents, Utils.getSnippetsDir()
				+ sFilename);
		if (result == null) {
			Utils.openScript(Utils.getSnippetsDir(), sFilename);
		} else if (IJ.showMessageWithCancel("Snippet Creator",
				"An error has occurred while saving the file.\n"
						+ "Display snippet in the Log window?")) {
			IJ.log("\n*** " + sFilename + " contents ***\n" + sContents);
		}
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
		gd = new NonBlockingGenericDialog("New Snippet");
		addButtons(gd);
		gd.setInsets(0, 0, 0);
		gd.addTextAreas(sContents, null, 12, 67);
		ta = gd.getTextArea1();
		ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
		gd.addStringField("Filename:", sFilename, 16);
		gd.addChoice("Language:", S_TYPES, S_TYPES[sType]);
		gd.setInsets(-85, 245, 0);
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
		infoMsg = (MultiLineLabel) gd.getMessage();

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
			}
			if (header != "")
				appendToTextArea(header);

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
		okButton.setLabel(f.exists() ? "Replace and Open" : " Create and Open ");
		fField.setForeground((f.exists()||invalidName) ? Color.RED : Color.BLACK);

		// Adjust messages
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
		infoMsg.setText(sb.toString());
		infoMsg.setForeground(Color.DARK_GRAY);

		return !invalidName;
	}

	/* Adds custom buttons to prompt */
	private void addButtons(final GenericDialog gd) {
		final Panel p = new Panel();
		p.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
		paste = new Button("Append clipboard text");
		paste.addActionListener(this);
		p.add(paste);
		load = new Button("Append file contents...");
		load.addActionListener(this);
		p.add(load);
		list = new Button("List snippets");
		list.addActionListener(this);
		p.add(list);
		gd.addPanel(p, GridBagConstraints.CENTER, new Insets(0, 0, 0, 0));
	}

	@Override
	/* Defines actions for custom buttons */
	public void actionPerformed(final ActionEvent e) {
		final Object source = e.getSource();
		if (source==list) {

			final java.awt.Point pos = list.getLocationOnScreen();
			final int xPos = (int) pos.getX() + 10;
			final int yPos = (int) pos.getY() + 10;
			Utils.listDirectory(Utils.getSnippetsDir(), xPos, yPos);
			// gd.toFront();

		} else if (source==paste) {

			final String clipboard = Utils.getClipboardText();
			if (clipboard!=null)
				appendToTextArea(clipboard);
			else
				infoMsg.setText("\nNo valid text in clipboard.");

		} else if (source==load) {

			final String file = IJ.openAsString("");
			if (file==null) return;
			if (file.startsWith("Error: "))
				IJ.error("Snippet Creator", file.substring(7));
			else {
				if (file.length()>30000)
					IJ.error("Snippet Creator", "File is too large");
				else
					appendToTextArea(file);
			}

		}
	}

	/* Inserts text at the active position of the TextArea of the main dialog */
	private void appendToTextArea(String string) {
		final int startPos = ta.getCaretPosition();
		if (!string.endsWith("\n")) string +="\n";
		ta.insert(string, startPos);
		ta.select(startPos, ta.getCaretPosition());
		ta.requestFocusInWindow();
	}


}
