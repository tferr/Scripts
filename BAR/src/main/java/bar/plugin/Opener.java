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

import ij.IJ;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

import java.awt.Button;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import bar.Utils;

/**
 * This class implements a file browser modeled after CommandFinder[1] by Mark
 * Longair and FunctionFinder[2] by Jerome Mutterer. It is also influenced by
 * Johannes Schindelin's Recent_Commands plugin[3].
 *
 * [1] https://github.com/imagej/ImageJA/blob/master/src/main/java/ij/plugin/CommandFinder.java
 * [2] https://github.com/imagej/ImageJA/blob/master/src/main/java/ij/macro/FunctionFinder.java
 * [3] https://github.com/fiji/Fiji_Plugins/blob/master/src/main/java/fiji/util/Recent_Commands.java
 *
 */
public class Opener implements PlugIn, FileFilter, ActionListener,
		DocumentListener, KeyListener, ListSelectionListener, MouseListener,
		WindowListener {

	/* Defaults for "Reset" option */
	private static final String DEF_PATH = Utils.getBARDir();
	private static final int DEF_MAX_SIZE = 200;
	private static final boolean DEF_CLOSE_ON_OPEN = false;
	private static final boolean DEF_IJM_LEGACY = false;

	/* Parameters */
	private String path = DEF_PATH;
	private int maxSize = DEF_MAX_SIZE;
	private boolean closeOnOpen = DEF_CLOSE_ON_OPEN;
	private boolean ijmLegacy = DEF_IJM_LEGACY;
	private String matchingString = "";
	private boolean truncatedList = false;

	private Dialog dialog;
	private JTextField prompt;
	private JScrollPane listPane;
	private JList list;
	private JLabel status;
	private Button optionsButton, openButton, closeButton;
	private PopupMenu optionsMenu;
	private Vector<String> filenames;
	private String selectedItem;
	ArrayList<String> bookmarks;


	public void run(final String arg) {
		new Thread() {
			public void run() {
				runInteractively();
			}
		}.start();
	}

	public void runInteractively() {

		// Populate file list and initialize bookmarks
		filenames = new Vector<String>();
		setFileList();
		bookmarks = new ArrayList<String>();

		// Build dialog
		dialog = new Dialog(IJ.getInstance(), "BAR Opener");
		dialog.addWindowListener(this);
		dialog.setLayout(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridy = c.gridx = 0;

		// Add prompt
		prompt = new JTextField("", 20);
		prompt.setToolTipText("<html>Search Field Shortcuts:<br>"
				+ "&nbsp;&emsp;&crarr;&ensp; Open filtered item <br>"
				+ "&nbsp;&emsp;&darr;&emsp; Move to list<br>"
				+ "&emsp;Esc&nbsp; Clear search field</html>");
		prompt.addActionListener(this);
		prompt.addKeyListener(this);
		prompt.getDocument().addDocumentListener(this);
		c.gridy++; c.gridx = 0;
		dialog.add(prompt, c);

		// By default, lowLevel keyEvents are captured by FocusTraversalKeys
		// To capture  KeyEvents such as VK_TAB, we'd need to remove the
		// default FocusTraversalKeys from the JTextField. Alternatively, we
		// could use java.awt.TextField instead
		//		prompt.setFocusTraversalKeys(
		//				KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
		//				Collections.<AWTKeyStroke> emptySet());

		// Add list
		listPane = new JScrollPane();
		list = new JList(filenames);
		list.setVisibleRowCount(16);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setToolTipText("<html>File List Shortcuts:<br>"
				+ "&emsp;&uarr; &darr;&ensp; Select items<br>"
				+ "&emsp;&crarr;&emsp; Open item<br>"
				+ "&emsp;&larr;&emsp; Parent directory<br>"
				+ "&emsp;&rarr;&emsp; Expand selected folder<br>"
				+ "Alt &uarr;, &lArr;&ensp;Search prompt</html>");
		list.addKeyListener(this);
		list.addMouseListener(this);
		list.addListSelectionListener(this);
		listPane.getViewport().setView(list);
		c.gridy++; c.gridx = 0;
		dialog.add(listPane, c);

		// Add status label
		status = new JLabel();
		status.addMouseListener(this);
		status.setForeground(Color.DARK_GRAY);
		updateStatus(path);
		c.gridy++; c.gridx = 0;
		dialog.add(status, c);

		// Add buttons
		final Panel buttonPanel = new Panel();
		optionsButton = new Button(". . .");
		optionsMenu = createOptionsMenu();
		optionsButton.add(optionsMenu);
		optionsButton.addActionListener(this);
		optionsButton.addMouseListener(this);
		buttonPanel.add(optionsButton);
		closeButton = new Button("Dismiss");
		closeButton.addActionListener(this);
		buttonPanel.add(closeButton);
		openButton = new Button("Open");
		openButton.addActionListener(this);
		buttonPanel.add(openButton);
		c.gridy++; c.gridx = 0;
		dialog.add(buttonPanel, c);

		// Display dialog
		dialog.pack();
		dialog.setResizable(false);
		dialog.setVisible(true);
		prompt.requestFocus();

	}

	void addBookmark() {
		if (!bookmarks.contains(path)) {
			if (bookmarks.size() > 0)
				optionsMenu.remove(optionsMenu.getItemCount() - 1);
			bookmarks.add(path);
			optionsMenu.add(createBookmarkMenu());
		}
	}

	void cdToDirectory(final String defaultpath) {
		try {
			Class.forName("fiji.util.gui.GenericDialogPlus");
			final GenericDialogPlus gd = new GenericDialogPlus("Change directory");
			gd.addDirectoryField("cd to..", defaultpath, 50);
			gd.setOKLabel("    Set Path    ");
			gd.addDialogListener(new DialogListener() {
				@Override
				public boolean dialogItemChanged(final GenericDialog gd, final AWTEvent e) {
					final TextField tf = (TextField) gd.getStringFields().elementAt(0);
					final Button[] buttons = gd.getButtons();
					if (new File(gd.getNextString()).exists()) {
						tf.setForeground(Color.BLACK);
						buttons[0].setLabel("    Set Path    ");
						return true;
					} else {
						tf.setForeground(Color.RED);
						buttons[0].setLabel("Invalid Path");
						return false;
					}
				}
			});
			gd.showDialog();
			if (!gd.wasCanceled())
				setPath(gd.getNextString());
		} catch (final ClassNotFoundException e) {
			IJ.error("Dependencies Missing", "Error: This command requires fiji-lib.");
		}
	}

	void changeDirectory(String newDir) {
		if (newDir.isEmpty())
			newDir = IJ.getDirectory("Choose new Directory");
		if (newDir == null)
			return;
		if (Utils.fileExists(newDir))
			setPath(newDir);
		else
			error("Path unavailable. Refreshing...");
		resetFileList();
	}

	void clearBookmarks() {
		optionsMenu.remove(optionsMenu.getItemCount() - 1);
		bookmarks.clear();
	}

	boolean consoleMode() {
		return matchingString.startsWith("!");
	}

	PopupMenu createOptionsMenu() {
		final PopupMenu popup = new PopupMenu();

		MenuItem mi = new MenuItem("Bookmark");
		mi.addActionListener(this);
		popup.add(mi);
		mi = new MenuItem("Reveal Path");
		mi.addActionListener(this);
		popup.add(mi);
		mi = new MenuItem("Go To...");
		mi.addActionListener(this);
		popup.add(mi);
		popup.addSeparator();
		mi = new MenuItem("Refresh File List");
		mi.addActionListener(this);
		popup.add(mi);
		mi = new MenuItem("Print Current List");
		mi.addActionListener(this);
		popup.add(mi);
		popup.addSeparator();

		mi = new MenuItem("Enter Console Mode");
		mi.addActionListener(this);
		popup.add(mi);
		mi = new MenuItem("Options...");
		mi.addActionListener(this);
		popup.add(mi);
		popup.addSeparator();

		return popup;
	}

	Menu createBookmarkMenu() {
		final Menu menu = new Menu("Bookmarks");
		MenuItem mi;
		for (final String bookmark : bookmarks) {
			mi = new MenuItem(bookmark);
			mi.addActionListener(this);
			menu.add(mi);
		}
		return menu;
	}

	void error(final String msg) {
		status.setForeground(Color.RED);
		status.setText(" "+ msg);
	}

	void info(final String msg) {
		status.setForeground(Color.BLACK);
		status.setText(" "+ msg);
	}

	void interpretCommand(final String cmd) {
		if (cmd.isEmpty()) // just a spacer in command list
			return;

		final String result = execCommand(cmd);

		if (result == null) { // The path encoded by cmd was not found
			if (cmd.startsWith("image")) {
				error("Image directory unknown");
				IJ.error("Unknown path", "Could not determine path of active image.");
			} else if (cmd.startsWith("current")) {
				error("Working directory unknown");
				IJ.error("Unknown path", "Working directory is set upon a valid I/O operation.");
			} else if (!cmd.startsWith("new")) {
				error("Directory not found");
				IJ.error("Error", "The requested directory could not be found.");
			}
			resetCommandList();
			return;
		}

		if (result.equals(String.valueOf(0))) { // Successful execution of a void command
			resetFileList();
			return;
		}

		if (result.isEmpty()) {  // Directory was already changed by a cmd-encoded path
			return;
		}

		changeDirectory(result);

	}

	String execCommand(final String cmd) {

		// Commands that do not change directories
		String exitStatus = String.valueOf(0);
		if (cmd.equals("options")) {
			showOptionsDialog();
			return exitStatus;
		} else if (cmd.startsWith("ls")) {
			printList();
			return exitStatus;
		} else if (cmd.startsWith("refresh")) {
			resetFileList();
			return exitStatus;
		} else if (cmd.startsWith("reveal")) {
			Utils.revealFile(path);
			return exitStatus;
		} else if (cmd.startsWith("close")) {
			dialog.dispose();
			return exitStatus;
		} else if (cmd.equals("help") || cmd.equals("?")) {
			showHelp();
			return exitStatus;
		}

		// Commands that change directories directly
		exitStatus = "";
		if (cmd.equals("..")) {
			selectParentDirectory(path);
			return exitStatus;
		} else if (cmd.equals("cd")) {
			cdToDirectory(path);
			return exitStatus;
		}

		// Commands that retrieve paths
		exitStatus = null;
		if (cmd.equals("new")) {
			exitStatus = IJ.getDirectory("Choose new directory");
		} else if (cmd.startsWith("home")) {
			exitStatus = IJ.getDirectory("home");
		} else if (cmd.startsWith("image")) {
			exitStatus = IJ.getDirectory("image");
		} else if (cmd.equals("luts")) {
			exitStatus = IJ.getDirectory(cmd);
		} else if (cmd.equals("macros")) {
			exitStatus = IJ.getDirectory(cmd);
		} else if (cmd.equals("plugins")) {
			exitStatus = IJ.getDirectory(cmd);
		} else if (cmd.startsWith("current")) {
			exitStatus = OpenDialog.getDefaultDirectory();
		} else if (cmd.equals("ij")) {
			exitStatus = IJ.getDirectory("imagej");
		} else if (cmd.startsWith("temp")) {
			exitStatus = IJ.getDirectory("temp");
		} else if (cmd.equals("snip")) {
			exitStatus = Utils.getSnippetsDir();
		} else if (cmd.equals("lib")) {
			exitStatus = Utils.getLibDir();
		} else if (cmd.equals("bar")) {
			exitStatus = Utils.getBARDir();
		} else if (cmd.equals("samples")) {
			exitStatus = IJ.getDirectory("imagej") + cmd;
		} else if (cmd.equals("scripts")) {
			exitStatus = IJ.getDirectory("imagej") + cmd;
		} else if (cmd.equals("tools/")) {
			exitStatus = IJ.getDirectory("macros") + "tools";
		} else if (cmd.equals("toolsets")) {
			exitStatus = IJ.getDirectory("macros") + cmd;
		}
		return exitStatus;

	}

	void resetCommandList() {
		setMatchingString("!");
		prompt.setText("!");
		prompt.selectAll();
		updateList();
	}

	void resetFileList() {
		setMatchingString("");
		prompt.setText("");
		updateList();
		list.setSelectedIndex(0);
	}

	boolean isOpenable(String path) {
		if (path == null)
			return false;
		if (!consoleMode()) {
			final String[] compiledExtensions = { ".jar", ".class", ".pyc" };
			path = path.toLowerCase();
			for (final String ext : compiledExtensions) {
				if (path.endsWith(ext))
					return false;
			}
		}
		return true;
	}

	boolean isFolder(final String path) {
		return path != null && path.endsWith(File.separator);
	}

	void openItem(final String filename) {

		if (!isOpenable(filename))
			return;

		if (consoleMode()) {
			try {
				final int idx1 = filename.indexOf("<b>") + 4;
				final int idx2 = filename.indexOf("</b>");
				selectedItem = filename.substring(idx1, idx2);
				interpretCommand(selectedItem);
			} catch (final Exception e) {
				error("Error: malformed command");
			}
			return;
		}

		if (isFolder(filename)) {
			selectSubDirectory(filename);
			return;
		}

		new Thread() {
			public void run() {
				if (!Utils.fileExists(path + filename)) {
					error(filename + " unavailable...");
					return;
				}
				if (isScript(filename)) {
					if (ijmLegacy && filename.toLowerCase().endsWith("ijm"))
						Utils.openIJ1Script(path, filename);
					else
						Utils.openScript(path, filename);
				} else {
					IJ.open(path + filename);
				}
			}
		}.start();

		if (closeOnOpen)
			dialog.dispose();

	}

	boolean isScript(final String file) {
		final String[] EXTS = { ".txt", ".bsh", ".clj", ".groovy", ".ijm",
				".js", ".py", ".rb", ".java" };
		for (final String ext : EXTS)
			if (file.endsWith(ext))
				return true;
		return false;
	}

	void search() {
		matchingString = prompt.getText().toLowerCase();
		updateList();
	}

	void selectParentDirectory(final String currentDir) {
		changeDirectory(new File(currentDir).getParentFile().getAbsolutePath());
		list.setSelectedIndex(0);
		list.requestFocus();
	}

	void selectSubDirectory(final String subdir) {
		changeDirectory(path + subdir);
		list.setSelectedIndex(0);
		list.requestFocus();
	}

	void setCommandList() {
		final String spacer = "<html><span style='color:white;'><b>!</b></span></html>";
		final String cmds[] = {
				"!..@Parent directory of current path",
				"!cd@Change directory to...",
				"!current, !pwd@ImageJ <i>working</i> directory",
				"!image, !imp@Folder of active image",
				"!home, !~@"+ System.getProperty("user.name") +"'s home directory",
				"!new@Choose new path in file manager",
				"!temp, !tmp@Temporary directory",
				spacer,
				"!ij@<i>ImageJ</i> directory",
				"!luts@<i>Lookup Tables</i> directory",
				"!macros@<i>Macros</i> directory",
				"!plugins@<i>Plugins</i> directory",
				"!samples@Cached File&gt;Open Samples&gt;",
				"!scripts@<i>Scripts</i> directory",
				"!tools/@Macro <i>tools</i> directory", // slash makes command unique
				"!toolsets@Macro <i>toolsets</i> directory",
				spacer,
				"!bar@BAR root directory",
				"lib@Path to <i>BAR/lib</i>",
				"!snip@Path to <i>BAR/Snippets</i>",
				spacer,
				"!close !quit@Dismiss this window",
				"!ls !print@List contents of current path",
				"!help@Display built-in help",
				"!options@Prompt for settings",
				"!refresh@Refresh (reload) list"
			};

		truncatedList = false;
		filenames.removeAllElements();
		for (String cmd : cmds) {
			if (cmd.indexOf(matchingString) >= 0) {
				if (!cmd.equals(spacer)) {
					final String[] items = cmd.split("@");
					cmd = "<html><span style='color:blue;'>"
							+ "<b>" + items[0] + "</b>"
							+ "</span>&ensp;" + items[1] + "</html>";
				}
				filenames.add(cmd);
			}
		}
	}

	void setFileList() {
		truncatedList = false;
		filenames.removeAllElements();
		final File dir = new File(path);
		final int rootIdx = path.length();
		for (final File f : dir.listFiles(this)) {
			if (filenames.size() >= maxSize) {
				truncatedList = true;
				break;
			}
			String name = f.getAbsolutePath().substring(rootIdx);
			if (f.isDirectory())
				name += File.separator;
			if (name.length() == 0)
				continue;
			else if (name.toLowerCase().indexOf(matchingString) >= 0)
				filenames.add(name);
		}
		// Collections.sort(filenames);
		filenames.add("..");
	}

	void setPath(String newPath) {
		if (!newPath.endsWith(File.separator))
			newPath += File.separator;
		path = newPath;
	}

	void showOptionsDialog() {
		final GenericDialog gd = new GenericDialog("Opener Settings");
		final Font boldf = new Font("SansSerif", Font.BOLD, 12);
		gd.setInsets(0, 0, 0);
		gd.addMessage("Options:", boldf);
		gd.setInsets(0,20,0);
		gd.addCheckbox("Open IJM files in built-in (legacy) editor", ijmLegacy);
		gd.addCheckbox("Close window after opening selected file", closeOnOpen);
		gd.addNumericField("Maximum number of items in list", maxSize, 0);
		gd.setInsets(20, 0, 0);
		gd.addMessage("Resets:", boldf);
		gd.addCheckbox("Reset path to BAR directory", false);
		gd.addCheckbox("Clear bookmarks", false);
		gd.enableYesNoCancel("OK", "Restore Default Options");
		gd.showDialog();
		if (gd.wasCanceled())
			return;
		else if (gd.wasOKed()) {
			maxSize = (int) Math.max(1, gd.getNextNumber());
			ijmLegacy = gd.getNextBoolean();
			closeOnOpen = gd.getNextBoolean();
			if (gd.getNextBoolean()) path = DEF_PATH;
			if (gd.getNextBoolean()) clearBookmarks();

		} else {
			maxSize = DEF_MAX_SIZE;
			ijmLegacy = DEF_IJM_LEGACY;
			closeOnOpen = DEF_CLOSE_ON_OPEN;
			showOptionsDialog();
		}
		updateList();
	}

	void updateList() {
		if (consoleMode())
			setCommandList();
		else
			setFileList();
		updatingList = true;
		list.setListData(filenames);
		updatingList = false;
		updateStatus(path);
	}

	void updateStatus(final String dir) {
		String tip, msg;
		if (!dialog.isShowing()) { // First time prompt is displayed
			msg = " Waiting for input...";
			tip = "<html>Double-click to change directory or type <tt>!new</tt>."
					+ "<br>Current path:<br><pre>" + dir + "</pre></html>";
		} else { // Interactive mode

			final int hits = filenames.size();

			if (hits == 0) {
				prompt.setForeground(Color.RED);
				status.setForeground(Color.RED);
				msg = "No match found...";
				tip = "<html>Reset search or type <tt>!refresh</tt>."
						+ "<br>Current path:<br><pre>" + dir + "</pre></html>";
			} else { // No search (unfiltered list) or search with hits
				if (consoleMode()) { // Default console status
					prompt.setForeground(Color.BLUE);
					status.setForeground(Color.BLUE);
					msg = " Console mode enabled...";
					tip = "<html>Double-click to exit console mode.<br>Current path:<br><pre>"
							+ dir + "</pre></html>";
				} else { // Default file list status
					prompt.setForeground(Color.BLACK);
					status.setForeground(Color.DARK_GRAY);
					if (truncatedList) { // Some files may not be displayed
						msg = String.valueOf(maxSize) + " items limit reached...";
						tip = "<html>Double-click to change list size or type <tt>!options</tt>."
								+ "<br>Current path:<br><pre>" + dir + "</pre></html>";
					} else {  // All files are being filtered
						final String label = (hits == 1) ? String.valueOf(hits)
								+ " item, " : String.valueOf(hits) + " items, ";
						msg = label + dir;
						tip = "<html>Double-click to change directory or type <tt>!new</tt>."
								+ "<br>Current path:<br><pre>" + dir + "</pre></html>";
					}
				}
			}
		}
		status.setText(msg);
		status.setToolTipText(tip);
	}

	void validateOptionsMenu() {
		// Toggle "Bookmark", "Reveal Path", "Print Current List", etc.
		for (int i = 0; i < optionsMenu.getItemCount(); i++) {
			final MenuItem item = optionsMenu.getItem(i);
			final String label = item.getLabel();
			if (label.equals("Bookmark") || label.equals("Reveal Path")
					|| label.equals("Print Current List")
					|| label.equals("Refresh File List")) {
				item.setEnabled(!consoleMode());
			} else if (label.contains("Console")) {
				String cLabel = (consoleMode()) ? "Exit Console Mode"
						: "Enter Console Mode";
				item.setLabel(cLabel);
			}
		}
	}


	/* FileFilter Methods */
	@Override
	public boolean accept(final File file) {
		return !file.isHidden()
				&& (file.getAbsolutePath().toLowerCase()
						.indexOf(matchingString) >= 0);
	}


	/* ActionEvent Methods */
	@Override
	public void actionPerformed(final ActionEvent e) {
		final Object b = e.getSource();

		if (b == prompt) {
			matchingString = prompt.getText().toLowerCase();
		} else if (b == optionsButton) {
			final Rectangle r = optionsButton.getBounds();
			optionsMenu.show(optionsButton, r.x, r.y);
		} else if (b == openButton) {
			openItemInNewThread(selectedItem);
		} else if (b == closeButton) {
			dialog.dispose();
		} else { // An item from optionsMenu has been selected
			final String command = e.getActionCommand();
			if (command.equals("Settings...")) {
				showOptionsDialog();
			} else if (command.equals("Go To...")) {
				changeDirectory("");
			} else if (command.equals("Bookmark")) {
				addBookmark();
			} else if (command.equals("Print List")) {
				Utils.listDirectory(path);
			} else if (command.equals("Reveal Path")) {
				Utils.revealFile(path);
			} else { // A bookmark was selected
				changeDirectory(command);
			}
		}
	}


	/* DocumentListener Methods */
	@Override
	public void insertUpdate(final DocumentEvent ev) {
		search();
	}

	@Override
	public void removeUpdate(final DocumentEvent ev) {
		search();
	}

	public void changedUpdate(final DocumentEvent ev) {
	}

	/* ListSelectionListener Methods */
	@Override
	public void valueChanged(final ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			final int selectedIdx = list.getSelectedIndex();
			selectedItem = filenames.elementAt(selectedIdx);
			openButton.setEnabled(isOpenable(selectedItem));
		}
	}


	/* KeyListener Methods */
	public void keyPressed(final KeyEvent ke) {
		final int key = ke.getKeyCode();
		final Object source = ke.getSource();

		if ((ke.isMetaDown() && key == KeyEvent.VK_W)
				|| key == KeyEvent.VK_ESCAPE) {
			dialog.dispose();

		} else if (source == prompt) {

			if (key == KeyEvent.VK_DOWN
					|| (ke.isAltDown() && key == KeyEvent.VK_TAB)) {
				list.setSelectedIndex(0);
				list.requestFocus();
			} else if (key == KeyEvent.VK_ENTER && filenames.size() <= 2) {
				list.setSelectedIndex(0);
				openItemInNewThread(selectedItem);
			}

		} else if (source == list) {

			if (key == KeyEvent.VK_LEFT) {

				selectParentDirectory(path);

			} else if (key == KeyEvent.VK_RIGHT) {

				if (isFolder(selectedItem))
					selectSubDirectory(selectedItem);

			} else if (key == KeyEvent.VK_ENTER) {

				openItem(selectedItem);

			} else if (key == KeyEvent.VK_BACK_SPACE
					|| (key == KeyEvent.VK_TAB) && ke.isAltDown()) {

				prompt.requestFocus();

			} else if (key == KeyEvent.VK_UP) {

				if (ke.isAltDown())
					prompt.requestFocus();
				else if (list.getSelectedIndex() == 0) {
					list.setSelectedIndex(filenames.size() - 1);
					list.requestFocus();
				}

			} else if (key == KeyEvent.VK_DOWN) {

				if (list.getSelectedIndex() == filenames.size() - 1) {
					list.setSelectedIndex(0);
					list.requestFocus();
				}

			}

		}

	}

	public void keyReleased(final KeyEvent ke) {
	}

	public void keyTyped(final KeyEvent ke) {
	}


	/* MouseListener Methods */
	@Override
	public void mouseClicked(final MouseEvent e) {
		if (e.getClickCount() == 2) {
			if (e.getSource() == list)
				openItemInNewThread(selectedItem);
			else if (e.getSource() == status) {
				if (truncatedList)
					showOptionsDialog();
				else
					changeDirectory("");
			}
		}
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		if (e.getSource() != list && e.isPopupTrigger()) {
			validateOptionsMenu();
			optionsMenu.show(optionsButton, e.getX(), e.getY());
		}
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		if (e.getSource() != list && e.isPopupTrigger()){
			validateOptionsMenu();
			optionsMenu.show(optionsButton, e.getX(), e.getY());
		}
	}

	public void mouseEntered(final MouseEvent e) {
	}

	public void mouseExited(final MouseEvent e) {
	}

	/* WindowListener Methods */
	public void windowClosing(final WindowEvent e) {
		dialog.dispose();
	}

	public void windowActivated(final WindowEvent e) {
	}

	public void windowClosed(final WindowEvent e) {
	}

	public void windowDeactivated(final WindowEvent e) {
	}

	public void windowDeiconified(final WindowEvent e) {
	}

	public void windowIconified(final WindowEvent e) {
	}

	public void windowOpened(final WindowEvent e) {
	}

}
