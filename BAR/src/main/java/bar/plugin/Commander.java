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

import fiji.Debug;
import fiji.util.gui.GenericDialogPlus;
import ij.IJ;
//import ij.Menus;
import ij.WindowManager;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;
import ij.text.TextPanel;
import ij.text.TextWindow;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import bar.FileDrop;
import bar.Utils;

/**
 * This class implements a file browser modeled after <a href=
 * "https://github.com/imagej/ImageJA/blob/master/src/main/java/ij/plugin/CommandFinder.java"
 * >(CommandFinder)</a> by Mark Longair and <a href=
 * "https://github.com/imagej/ImageJA/blob/master/src/main/java/ij/macro/FunctionFinder.java"
 * >(FunctionFinder)</a> by Jerome Mutterer. It is also influenced by Johannes
 * Schindelin's <a href=
 * "https://github.com/fiji/Fiji_Plugins/blob/master/src/main/java/fiji/util/Recent_Commands.java"
 * >(Recent_Commands plugin)</a> and a bit of DOS nostalgia.
 *
 * @author tiago
 */
public class Commander implements PlugIn, ActionListener, DocumentListener,
		KeyListener, ListSelectionListener, MouseListener, WindowListener {

	/** Defaults for "Reset" option */
	private static final String DEF_PATH = System.getProperty("user.home");
	private static final int DEF_MAX_SIZE = 200;
	private static final boolean DEF_CLOSE_ON_OPEN = false;
	private static final boolean DEF_IJM_LEGACY = false;
	private static final boolean DEF_REGEX = false;
	private static final String PROMPT_PLACEHOLDER = "Search or press <!> for console";

	/** Flag that monitors if file list reached maximum size */
	private boolean truncatedList = false;
	/** Flag that toggles changes to status bar messages */
	private boolean freezeStatusBar = false;

	/** Parameters **/
	private static final int FRAME_WIDTH = 260;
	private static final int FRAME_HEIGHT = 360;
	private String path = DEF_PATH;
	private int maxSize = DEF_MAX_SIZE;
	private boolean closeOnOpen = DEF_CLOSE_ON_OPEN;
	private boolean ijmLegacy = DEF_IJM_LEGACY;
	private boolean regex = false;
	private String matchingString = "";

	private JFrame frame;
	private JTextField prompt;
	private JScrollPane listPane;
	private JLabel statusBar;
	private Button optionsButton, openButton, closeButton;
	private PopupMenu optionsMenu;
	private ArrayList<String> filenames, bookmarks;
	private String selectedItem;
	private JTable table;
	private static TableModel tableModel;
	private JTableHeader tableHeader;

	public static void main(final String[] args) { Debug.run("BAR Commander...",""); }


	public void run(final String arg) {

		// Check if Commander is already running
		if (WindowManager.getWindow("BAR Commander") != null) {
			IJ.selectWindow("BAR Commander");
			if (arg != null && !arg.isEmpty())
				IJ.showStatus("In Commander, type <" + arg + "> to start browsing...");
			return;
		}

		// Check if a path has been specified
		if ("!lib".equals(arg))
			path = Utils.getLibDir();
		else if ("!snip".equals(arg))
			path = Utils.getSnippetsDir();
		// Try to retrieve a new directory if specified path is not valid
		if (!Utils.fileExists(path)) {
			path = IJ.getDirectory("Choose new directory");
			// Exit if user canceled prompt
			if (path == null) {
				IJ.showStatus("Commander requires a valid directory at startup...");
				return;
			}
		}
		// Start Commander
		new Thread() {
			public void run() {
				runInteractively();
			}
		}.start();

	}


	@SuppressWarnings("serial")
	public void runInteractively() {

		// Initialize file list and favorites list
		filenames = new ArrayList<String>();
		bookmarks = new ArrayList<String>();
		final String metaKey = IJ.isMacOSX() ? "Cmd" : "Ctrl";

		// Build dialog
		frame = new JFrame("BAR Commander");
		frame.addWindowListener(this);
		frame.setLayout(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridy = c.gridx = 0;

		// Add search prompt
		prompt = new JTextField(PROMPT_PLACEHOLDER);
		prompt.selectAll();
		prompt.setToolTipText("<html>Prompt shortcuts:<br>"
				+ "&emsp;&uarr; &darr;&ensp; Move to list<br>"
				+ "&emsp;! &emsp; Console mode<br>"
				+ "&emsp;&crarr;&ensp; Open filtered item <br>"
				+ "&emsp;" + metaKey + "+1&ensp; Open 1<sup>st</sup> hit<br>"
				+ "&emsp;" + metaKey + "+2&ensp; Open 2<sup>nd</sup> hit<br>"
				+ "&emsp;" + metaKey + "+3&ensp; Open 3<sup>rd</sup> hit</html>");
		prompt.setFont(prompt.getFont().deriveFont(
				prompt.getFont().getSize() + 2f));
		prompt.getDocument().addDocumentListener(this);
		prompt.addActionListener(this);
		prompt.addKeyListener(this);
		c.gridy++; c.gridx = 0;
		frame.add(prompt, c);

		// Prepare table holding file list. Format it so it mimics a JList
		tableModel = new TableModel();
		table = new JTable(tableModel) { // Use alternate row colors
			public Component prepareRenderer(final TableCellRenderer renderer,
					final int row, final int column) {
				final Component c = super
						.prepareRenderer(renderer, row, column);
				if (!isRowSelected(row))
					c.setBackground(row % 2 == 0 ? new Color(255, 255, 255)
							: new Color(245, 245, 245));
				return c;
			}
		};
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setRowSelectionAllowed(true);
		table.setColumnSelectionAllowed(false);
		//table.setAutoCreateRowSorter(true);
		table.setFillsViewportHeight(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tableModel.setColumnWidths(table.getColumnModel());
		table.setShowGrid(false);
		table.setShowHorizontalLines(false);
		table.setShowVerticalLines(false);
		table.setRowMargin(0);
		table.setIntercellSpacing(new Dimension(0, 0));
		table.setToolTipText("<html>Navigation shortcuts:<br>"
				+ "&emsp;&uarr; &darr;&ensp; Select items<br>"
				+ "&emsp;&crarr;&emsp; Open item<br>"
				+ "&emsp;&larr;&emsp; Parent directory<br>"
				+ "&emsp;&rarr;&emsp; Expand selected folder<br>"
				+ "&ensp;A-Z&ensp; Alphabetic scroll<br>"
				+ metaKey + "+&uarr;, &lArr;&ensp;Search prompt</html>");
		table.addKeyListener(this);
		table.addMouseListener(this);
		table.getSelectionModel().addListSelectionListener(this);

		// Use Column header as a path bar
		tableHeader = table.getTableHeader();
		tableHeader.setLayout(new BorderLayout());
		tableHeader.addMouseListener(this);

		listPane = new JScrollPane();
		listPane.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
		listPane.getViewport().setView(table);
		c.gridy++; c.gridx = 0;
		frame.add(listPane, c);

		// Auto-scroll table using keystrokes
		table.addKeyListener(new KeyAdapter() {
			public void keyTyped(final KeyEvent evt) {
				final int nRows = tableModel.getRowCount();
				final char ch = Character.toLowerCase(evt.getKeyChar());
				if (!Character.isLetterOrDigit(ch)) {
					return; // Ignore searches for non alpha-numeric characters
				}
				final int sRow = table.getSelectedRow();
				for (int row = (sRow+1) % nRows; row != sRow; row = (row+1) % nRows) {
					final String rowData = tableModel.getValueAt(row, 0).toString();
					final char rowCh = Character.toLowerCase(rowData.charAt(0));
					if (ch == rowCh) {
						table.setRowSelectionInterval(row, row);
						table.scrollRectToVisible(table.getCellRect(row, 0, true));
						break;
					}
				}
			}
		});

		// Allow folders to be dropped in table. Consider only first item dropped
		new FileDrop(listPane, new FileDrop.Listener() {
			public void filesDropped(final java.io.File[] files) {
				try {
					final String dir = (files[0].isDirectory()) ? files[0]
							.getCanonicalPath() : files[0].getParent();
					if (dir == null) {
						error("Drag and Drop failed...");
						return;
					}
					setPath(dir);
					resetFileList();
				} catch (final java.io.IOException e) {
					error("Drag and Drop failed...");
				}
			}
		});

		// Add status bar
		statusBar = new JLabel();
		statusBar.addMouseListener(this);
		c.gridy++; c.gridx = 0;
		frame.add(statusBar, c);
		updateBrowserStatus();

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
		frame.add(buttonPanel, c);

		// Populate file list, path bar and display dialog
		setPath(path);
		updateList();
		frame.pack();
		frame.setResizable(false);
		WindowManager.addWindow(frame);
		frame.setVisible(true);
		prompt.requestFocus();

	}

	void addBookmark() {
		if (!bookmarks.contains(path)) {
			if (bookmarks.size() > 0)
				optionsMenu.remove(optionsMenu.getItemCount() - 1);
			bookmarks.add(path);
			optionsMenu.add(createBookmarkMenu());
			log("New bookmark: "+ path);
		} else
			error("Already bookmarked "+ path);
	}

	/** Prompts for a new path (requires fiji.util.gui.GenericDialogPlus) */
	void cdToDirectory(final String defaultpath) {
		log("Changing directory...");
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
					if (new File(gd.getNextString()).isDirectory()) {
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
			final String newPath = gd.getNextString();
			if (!gd.wasCanceled() && !newPath.isEmpty()) {
				changeDirectory(newPath);
			} else {
				error("cd to... not executed");
				return;
			}
		} catch (final ClassNotFoundException e) {
			error("cd to... not executed");
			error("Dependencies Missing", "Error: This command requires fiji-lib.");
		}
	}

	/**
	 * Changes path to the specified directory path (if valid). Displays and IJ
	 * error if directory does not exist. If specified directory is empty, user
	 * is prompted to choose a new directory.
	 */
	void changeDirectory(String newDir) {
		if (newDir.isEmpty())
			newDir = IJ.getDirectory("Choose new directory");
		if (newDir == null)
			return;
		if (Utils.fileExists(newDir))
			setPath(newDir);
		else
			error("Path unavailable: "+ newDir);
		resetFileList();
	}

	/** Clears all bookmarks in "Favorites" (optionsMenu) */
	void clearBookmarks() {
		optionsMenu.remove(optionsMenu.getItemCount() - 1);
		bookmarks.clear();
	}

	/** Creates optionsMenu */
	PopupMenu createOptionsMenu() {
		final PopupMenu popup = new PopupMenu();

		MenuItem mi = new MenuItem("Add to Favorites");
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

	/** Creates "Favorites" (bookmarks) menu */
	Menu createBookmarkMenu() {
		final Menu menu = new Menu("Favorites");
		MenuItem mi;
		for (final String bookmark : bookmarks) {
			mi = new MenuItem(bookmark);
			mi.addActionListener(this);
			menu.add(mi);
		}
		return menu;
	}

	/**
	 * Displays a message in the status bar. Message (assumed in plain text, not
	 * HTML-formated) is trimmed to the width of the status bar (a JLabel) to
	 * ensure bar is never enlarged.
	 *
	 * @param msecs
	 *            Duration (in milliseconds). Message will remain visible for at
	 *            least the specified duration . This is achieved through a
	 *            TimerTask that keeps the freezeStatusBar flag set to true for
	 *            the specified time.
	 * @param restore
	 *            If true, the previous message displayed in the status bar is
	 *            reinstated after the specified duration.
	 * @see log
	 * @see error
	 */
	void showStatus(String msg, final long msecs, final boolean restore) {

		final FontMetrics fm = statusBar.getFontMetrics(statusBar.getFont());
		final int maxLength = msg.length() * FRAME_WIDTH / fm.stringWidth(msg);
		if (msg.length() > maxLength)
			msg = msg.substring(0, maxLength - 3) + "...";

		final String previousMsg = statusBar.getText();
		statusBar.setText(msg);
		if (msecs == 0)
			return;

		freezeStatusBar = true;
		final Timer timer = new Timer();
		final TimerTask task = new TimerTask() {
			@Override
			public void run() {
				freezeStatusBar = false;
				if (restore)
					statusBar.setText(previousMsg);
				timer.cancel();
			}
		};
		try {
			timer.schedule(task, msecs);
		} catch (final Exception e) {
			timer.cancel();
		}

	}

	/**
	 * Displays an error message. When triggered in console mode the message is
	 * displayed for at least 5s. This is achieved through a timerTask that
	 * keeps the freezeStatusBar flag set to true for five seconds.
	 *
	 * @see log
	 * @see showStatus
	 */
	void error(final String errorMsg) {
		error(errorMsg, isConsoleMode());
	}

	/**
	 * Displays an error message.
	 *
	 * @param persistent
	 *            If true, the message is displayed for at least 5s. This is
	 *            achieved through a timerTask that keeps the freezeStatusBar
	 *            flag set to true for five seconds.
	 *
	 * @see log
	 * @see showStatus
	 */
	void error(final String errorMsg, final boolean persistent) {
		statusBar.setForeground(Color.RED);
		showStatus(errorMsg, (persistent) ? 5000 : 0, false);
	}

	/** Displays an ImageJ error message ensuring focus of main window */
	void error(final String title, final String msg) {
		IJ.error(title, msg);
		frame.toFront();
	}

	/**
	 * Displays an informational message if status bar is not "frozen"
	 * (freezeStatusBar is false). Does nothing if freezeStatusBar is true
	 * (an error is being displayed).
	 * 
	 * @see error
	 * @see showStatus
	 */
	void log(final String msg) {
		if (!freezeStatusBar)
			showStatus(msg, 0, false);
	}

	/**
	 * Displays a temporary informational message (visible only for the
	 * specified amount of time) if status bar is not "frozen" (freezeStatusBar
	 * is false). Does nothing if freezeStatusBar is true (an error is being
	 * displayed).
	 * 
	 * @param msecs
	 *            Duration (in milliseconds).
	 * @see error
	 * @see showStatus
	 */
	void log(final String msg, final long msecs) {
		if (!freezeStatusBar)
			showStatus(msg, msecs, true);
	}

	/**
	 * Interprets console commands upon receiving the exit status from
	 * execCommand
	 *
	 * @see execCommand
	 */
	void interpretCommand(final String cmd) {
		if (cmd.isEmpty()) // just a spacer in command list
			return;

		final String result = execCommand(cmd);

		// Case null: cmd encoded a path and encoded directory was not found
		if (result == null) {
			if (cmd.startsWith("imp")) {
				error("Image directory unknown");
				error("Unknown path", "Could not determine path of active image.");
			} else if (cmd.startsWith("pwd")) {
				error("Working directory unknown");
				error("Unknown path", "Working directory is set upon a valid I/O operation.");
			} else if (!cmd.startsWith("goto")) {
				error("Directory not found");
				error("Error", "The requested directory could not be found.");
			}
			resetCommandList();
			return;
		}

		// Case 0: cmd encoded a non-verbose self-contained instruction
		if (result.equals(String.valueOf(0))) {
			resetFileList();
			prompt.requestFocus();
			return;
		}

		// Remaining cases: cmd encodes a new path
		changeDirectory(result);
		prompt.requestFocus();

	}


	/**
	 * Executes console commands. Outputs one of the following exit status:
	 * <p>
	 * "0": Executed a a self-contained command that need no follow-up.
	 * null: Failed to retrieve a path.
	 * non-null string: A successfully retrieved path
	 *
	 * @see interpretCommand
	 */
	String execCommand(final String cmd) {

		// Case "0": Self-contained commands that need no follow-up
		String exitStatus = String.valueOf(0);
		if (cmd.startsWith("quit")) {
			quit();
			return exitStatus;
		} else if (cmd.startsWith("help")) {
			log("Displaying help...");
			IJ.showMessage("Commander Help", helpMessage());
			return exitStatus;
		} else if (cmd.startsWith("ls")) {
			printList();
			return exitStatus;
		} else if (cmd.equals("options")) {
			showOptionsDialog();
			return exitStatus;
		} else if (cmd.startsWith("refresh")) {
			freezeStatusBar = false;
			return exitStatus;
		} else if (cmd.startsWith("reveal")) {
			Utils.revealFile(path);
			return exitStatus;
		} else if (cmd.startsWith("..")) {
			selectParentDirectory(path);
			return exitStatus;
		} else if (cmd.equals("bookmark")) {
			addBookmark();
			return exitStatus;
		} else if (cmd.equals("cd")) {
			cdToDirectory(path);
			return exitStatus;
		} else if (cmd.startsWith("info")) {
			showInfo();
			return exitStatus;
		}

		// Remaining cases: Commands that only retrieve paths
		exitStatus = null;
		if (cmd.startsWith("goto")) {
			exitStatus = IJ.getDirectory("Choose new directory");
		} else if (cmd.startsWith("~")) {
			exitStatus = IJ.getDirectory("home");
		} else if (cmd.startsWith("image")) {
			exitStatus = IJ.getDirectory("image");
		} else if (cmd.equals("luts")) {
			exitStatus = IJ.getDirectory(cmd);
		} else if (cmd.equals("macros")) {
			exitStatus = IJ.getDirectory(cmd);
		} else if (cmd.equals("plugins")) {
			exitStatus = IJ.getDirectory(cmd);
		} else if (cmd.startsWith("pwd")) {
			exitStatus = OpenDialog.getDefaultDirectory();
		} else if (cmd.equals("ij")) {
			exitStatus = IJ.getDirectory("imagej");
		} else if (cmd.startsWith("tmp")) {
			exitStatus = IJ.getDirectory("temp");
		} else if (cmd.equals("snip")) {
			exitStatus = Utils.getSnippetsDir();
		} else if (cmd.equals("lib")) {
			exitStatus = Utils.getLibDir();
		//} else if (cmd.equals("bar")) {
		//	exitStatus = Utils.getBARDir();
		} else if (cmd.equals("samples")) {
			exitStatus = IJ.getDirectory("imagej") + cmd;
		} else if (cmd.equals("scripts")) {
			exitStatus = IJ.getDirectory("imagej") + cmd;
		}// else if (cmd.equals("tools/")) {
		 //	exitStatus = IJ.getDirectory("macros") + "tools";
		 //} else if (cmd.equals("toolsets")) {
		 //	exitStatus = IJ.getDirectory("macros") + cmd;
		 //}
		return exitStatus;

	}

	/** Display an IJ message with details on the current path */
	private void showInfo() {
		try {
			resetFileList();
			log("Dispaying info...");
			final File f = new File(path);
			final String writable = (f.canWrite()) ? "writable"
					: "non writable";
			final String bookmarked = (bookmarks.contains(path)) ? "bookmarked"
					: "not bookmarked";
			int subfolders = 0;
			for (final String item : filenames)
				if (item.endsWith(File.separator))
					subfolders++;
			final int files = filenames.size() - subfolders;

			final StringBuffer sb = new StringBuffer();
			sb.append("<html>");
			sb.append("<dl>");
			sb.append("<dt>Path (").append(writable).append("):</dt>");
			sb.append("<dd>");
			sb.append(f.getAbsolutePath()).append(File.separator);
			sb.append("</dd>");
			sb.append("<dt>Last modified:</dt>");
			sb.append("<dd>");
			sb.append(new Date(f.lastModified()));
			sb.append("</dd>");
			sb.append("<dt>Listed contents:</dt>");
			sb.append("<dd>");
			sb.append(subfolders).append(" subfolder(s), ").append(files)
					.append(" file(s)");
			sb.append("</dd>");
			sb.append("<dt>Maximum list size:</dt>");
			sb.append("<dd>");
			sb.append("Currently set to ").append(maxSize).append(" items");
			sb.append("</dd>");
			sb.append("<dt>Favorite?</dt>");
			sb.append("<dd>");
			sb.append("Folder is ").append(bookmarked);
			sb.append("</dd>");
			sb.append("</dl>");
			sb.append("</html>");
			IJ.showMessage(f.getName(), sb.toString());
		} catch (final Exception e) {
			error("Error while retrieving details...");
		}
	}

	/** Reloads Console commands */
	void resetCommandList() {
		prompt.setText("!");  // Resets matchingString
		prompt.selectAll();
		updateList();
	}

	/** Reloads an un-filtered listed of current path */
	void resetFileList() {
		prompt.setText(""); // Resets matchingString
		updateList();
		table.setRowSelectionInterval(0, 0);
	}

	/**
	 * Checks if the specified file belongs to a problematic list of extensions
	 * known to trigger undesirable IJ commands. (mainly compiled files that
	 * activate the "Plugins>Install command)
	 */
	boolean isOpenable(String path) {
		if (path == null)
			return false;
		if (!isConsoleMode()) {
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

		if (isConsoleMode()) {
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

		if (filename.startsWith("..")) {
			selectParentDirectory(path);
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
			quit();

	}

	boolean isScript(final String file) {
		final String[] EXTS = { ".txt", ".bsh", ".clj", ".groovy", ".ijm",
				".js", ".py", ".rb", ".java" };
		for (final String ext : EXTS)
			if (file.endsWith(ext))
				return true;
		return false;
	}

	void printList() {
		if (isConsoleMode()) {
			Utils.listDirectory(path);
			return;
		}
		if (filenames.size() < 1) {
			error("No files in current list");
			return;
		}
		final TextWindow tw = new TextWindow(
				path + " [" + matchingString + "]", "", 550, 200);
		final TextPanel tp = tw.getTextPanel();
		tp.setColumnHeadings("Double-click on a filename to open it");
		int counter = 1;
		final int padLngth = (int) (Math.log10(filenames.size()) + 1);
		for (final String f : filenames)
			tw.append("" + IJ.pad(counter++, padLngth) + ": " + path + f);
		tp.updateDisplay();
	}

	void selectParentDirectory(final String currentDir) {
		final File parent = new File(currentDir).getParentFile();
		if (parent == null)
			error("Parent directory unavailable");
		else
			changeDirectory(parent.getAbsolutePath());
	}

	void selectSubDirectory(final String subdir) {
		changeDirectory(path + subdir);
	}

	void setCommandList() {
		final String spacer = "<html><span style='color:white;'><b>!</b></span></html>";
		final String cmds[] = {
				"!..@Parent directory of current path",
				"!cd@Change directory to specified path",
				"!pwd@ImageJ <i>current</i> (working) directory",
				"!imp@Directory of active image",
				"!~@"+ System.getProperty("user.name") +"'s home directory",
				"!goto@Choose new path in file manager",
				spacer,
				"!ij@<i>ImageJ</i> directory",
				"!luts@<i>Lookup Tables</i> directory",
				"!macros@<i>Macros</i> directory",
				"!plugins@<i>Plugins</i> directory",
				"!samples@Cached File&gt;Open Samples&gt;",
				"!scripts@<i>Scripts</i> directory",
				spacer,
				"!lib@Path to <i>BAR/lib/</i>",
				"!snip@Path to <i>BAR/Snippets/</i>",
				"!tmp@<i>Temporary</i> directory",
				spacer,
				"!bookmark@Add current path to Favorites",
				"!info@Display info on current path",
				"!ls@List (print) contents of current path",
				"!help@Display built-in help",
				"!options@Prompt for settings",
				"!refresh@Refresh (reload) list",
				"!quit@Exit Commander"
		};

		truncatedList = false;
		filenames.clear();
		final String matchingCmd = matchingString.substring(1);
		for (String cmd : cmds) {
			if (cmd.equals(spacer) && !matchingCmd.isEmpty())
				continue;
			if (cmd.toLowerCase().indexOf(matchingCmd) >= 0) {
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

	/** Sets filenames matching current search */
	void setFileList() {

		final FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(final File file) {
				if (file.isHidden())
					return false;
				final String name = file.getName();
				if (regex && !matchingString.isEmpty())
					return Pattern.compile(matchingString).matcher(name).matches();
				else
					return name.toLowerCase().indexOf(matchingString) >= 0;
			}
		};

		truncatedList = false;
		filenames.clear();
		final File dir = new File(path);
		final int rootIdx = path.length();
		for (final File f : dir.listFiles(filter)) {
			if (filenames.size() >= maxSize) {
				truncatedList = true;
				setStatusTooltip("Double-click to change list size or type <tt>!options</tt>.");
				break;
			}
			String name = f.getAbsolutePath().substring(rootIdx);
			if (name.length() == 0)
				continue;
			if (f.isDirectory())
				name += File.separator;
			filenames.add(name);
		}
		// Collections.sort(filenames);
	}

	void setMatchingString(final String newMatchingString) {
		this.matchingString = newMatchingString.toLowerCase(Locale.US);
	}

	void repaintColumnHeader(final String newPath) {
		final TableColumn tc = tableHeader.getColumnModel().getColumn(0);
		tc.setHeaderValue(newPath);
		tableHeader.repaint();
		final StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		sb.append("Current path:<br>");
		sb.append("&emsp;").append(newPath).append("<br>");
		sb.append("To change directory:<br>");
		sb.append("&emsp;Double-click in path bar or drag &amp; drop ")
			.append("a new folder from the file manager<br>");
		sb.append("</p></html>");
		tableHeader.setToolTipText(sb.toString());
	}

	void setPath(String newPath) {
		if (!newPath.endsWith(File.separator))
			newPath += File.separator;
		path = newPath;
		repaintColumnHeader(newPath);
	}

	void setSelectedItem(final int index) {
		selectedItem = filenames.get(Math.max(0, index));
	}

	String helpMessage() {
		final StringBuffer sb = new StringBuffer();
		sb.append("<html>");
		sb.append("<head>");
		sb.append("  <style type='text/css'>");
		sb.append("  .cnsl {color: blue; font-family: monospace;}");
		sb.append("  .srch {color: black; font-family: monospace;}");
		sb.append("  .key {background:#eeeeee;}");
		sb.append("  </style>");
		sb.append("<title> BAR Commander</title>");
		sb.append("</head>");
		sb.append("<body><div WIDTH=550>");
		sb.append("  <h4>Description</h4>");
		sb.append("  BAR Commander is a file browser providing instant access to your ImageJ");
		sb.append("  files just by typing abbreviations of the searched item&rsquo;s name.");
		sb.append("  It is modeled after ImageJ's CommandLauncher in");
		sb.append("  <i>Plugins&gt;Utilities&gt;Find Commands... [L]</i>.");
		sb.append("  <br /> <br />");
		sb.append("  It serves two purposes: to expedite the opening of files and to produce");
		sb.append("  filtered lists of directory contents.");
		sb.append("  <br /> <br />");
		sb.append("  <h4>Examples: Opening Files</h4>");
		sb.append("  <ol type='I'>");
		sb.append("    <li>Open <i>").append(IJ.getDirectory("luts")).append("glasbey.lut:</i></li>");
		sb.append("      <ol>");
		sb.append("        <li>Type: <span class='cnsl'>!luts</span> &mdash; Enter &mdash; ")
				.append("<span class='srch'>glas</span> &mdash; Enter.</li>");
		sb.append("      </ol>");
		sb.append("    <li>Open <i>").append(Utils.getSnippetsDir()).append("Median_Filter.py:</i></li>");
		sb.append("      <ol>");
		sb.append("        <li>Type: <span class='cnsl'>!snip</span> &mdash; Enter &mdash; ")
				.append("<span class='srch'>med</span> &mdash; Enter.</li>");
		sb.append("      </ol>");
		sb.append("    </li>");
		sb.append("  </ol>");
		sb.append("  <h4>Examples: Listing Files</h4>");
		sb.append("  <ol type='I'>");
		sb.append("    <li>List the contents of <i>").append(System.getProperty("user.home"))
				.append(File.separator).append("Desktop").append(File.separator).append(":<i></li>");
		sb.append("      <ol>");
		sb.append("        <li>Type: <span class='cnsl'>!home</span> &mdash; Enter &mdash; ")
				.append("<span class='srch'>desk</span> &mdash; Enter.</li>");
		sb.append("      </ol>");
		sb.append("    <li>Extract the paths of all TIFF images in a directory:</li>");
		sb.append("      <ol>");
		sb.append("        <li>Drag and drop the desired folder onto the Commander list. Alternatively ")
				.append("  navigate to the desired folder using <i>Go To...</i> or the commands ");
		sb.append("        <span class='cnsl'>!goto</span> or <span class='cnsl'>!cd</span>.");
		sb.append("        You can also browse the file list using the <span class='key'>&larr;</span>")
				.append("&nbsp;<span class='key'>&larr;</span>&nbsp;<span class='key'>&uarr;</span>")
				.append("<&nbsp;<span class='key'>&darr;</span><&nbsp;<span class='key'>&rarr;</span>");
		sb.append("        navigation keys.</li>");
		sb.append("        <li>Type: <span class='srch'>.tif</span> &mdash; Enter.</li>");
		sb.append("        <li>Choose <i>Print Current List</i> from the Options Menu. Advanced");
		sb.append("        sorting and filtering is also possible by enabling regular expressions");
		sb.append("        in the <i>Options...</i> prompt.</li>");
		sb.append("      </ol>");
		sb.append("    </li>");
		sb.append("  </ol>");
		sb.append("</body>");
		sb.append("</div></html>");
		return sb.toString();
	}

	void showOptionsDialog() {
		log("Prompting for options...");
		boolean hardReset = false;
		final GenericDialog gd = new GenericDialog("Commander Options");
		gd.addNumericField("Maximum number of items in list", maxSize, 0);
		gd.addCheckbox("Close window after opening selected file", closeOnOpen);
		gd.addCheckbox("Open IJM files in built-in (legacy) editor", ijmLegacy);
		gd.addCheckbox("Search files using regex (experimental)", regex);
		gd.addMessage(""); //spacer
		gd.addCheckbox("Clear Favorites list", false);
		gd.enableYesNoCancel("OK", "Reset All Options");
		gd.addHelp(helpMessage());
		gd.showDialog();
		if (gd.wasCanceled()) {
			frame.toFront();
			return;
		} else if (gd.wasOKed()) {
			maxSize = (int) Math.max(1, gd.getNextNumber());
			closeOnOpen = gd.getNextBoolean();
			ijmLegacy = gd.getNextBoolean();
			regex = gd.getNextBoolean();
			if (gd.getNextBoolean())
				clearBookmarks();
			frame.toFront();
		} else {
			hardReset = true;
			maxSize = DEF_MAX_SIZE;
			ijmLegacy = DEF_IJM_LEGACY;
			closeOnOpen = DEF_CLOSE_ON_OPEN;
			regex = DEF_REGEX;
			setPath(DEF_PATH);
			showOptionsDialog();
		}
		if (hardReset)
			resetFileList();
		else
			updateList();
	}

	void updateList() {
		if (isConsoleMode()) {
			setCommandList();
			if (!freezeStatusBar)
				updateConsoleStatus();
			setStatusTooltip("Double-click to reload console commands.");
		} else {
			setFileList();
			if (filenames.size()==0 && matchingString.isEmpty()) {
				filenames.add("..Folder is empty");
			}
			if (!freezeStatusBar)
				updateBrowserStatus();
			setStatusTooltip("Double-click to refresh contents or type <tt>!refresh</tt>.");
		}

		tableModel.setData(filenames);
	}

	/** Defines the actions triggered by double-clicking on the status/path bar */
	void executeStatusBarActions() {
		freezeStatusBar = false;
		if (isConsoleMode()) {
			resetCommandList();
		} else {
			if (truncatedList) {
				showOptionsDialog();
			} else {
				updateList();
			}
		}
		System.gc();
	}

	void setStatusTooltip(final String text) {
		final StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		sb.append(text);
		sb.append("</html>");
		statusBar.setToolTipText(sb.toString());
	}

	void updateConsoleStatus() {
		prompt.setForeground(Color.BLUE);
		statusBar.setForeground(Color.BLUE);

		if (matchingString.equals("!")) {
			log("Console enabled...");
			return;
		}
		final int hits = filenames.size();
		if (hits == 0) {
			error("No matching commands...", false);
		} else if (hits == 1) {
			log("<html>Press &crarr; to execute</html>");
		} else {
			log(String.valueOf(hits) + " mathing commands");
		}
	}

	void updateBrowserStatus() {
		prompt.setForeground(Color.BLACK);
		statusBar.setForeground(Color.DARK_GRAY);

		final int hits = filenames.size();
		if (hits == 0) {
			error("No matches found...");
		} else if (hits == 1) {
			setSelectedItem(0);
			if (selectedItem.startsWith(".."))
				log("<html>Press &crarr; to return to parent</html>");
			else if (isFolder(selectedItem))
				log("<html>Press &crarr; to expand</html>");
			else if (isOpenable(selectedItem))
				log("<html>Press &crarr; to open</html>");
			else
				log("File cannot be opened...");
		} else {
			if (truncatedList)
				error(String.valueOf(maxSize) + " items limit reached...");
			else
				log(String.valueOf(hits) + " items");
		}
	}

	/** Toggles "Add to Favorites", "Reveal Path", "Print Current List", etc. */
	void validateOptionsMenu() {
		for (int i = 0; i < optionsMenu.getItemCount(); i++) {
			final MenuItem item = optionsMenu.getItem(i);
			final String label = item.getLabel();
			if (label.equals("Add to Favorites") || label.equals("Reveal Path")
					|| label.equals("Print Current List")
					|| label.equals("Refresh File List")) {
				item.setEnabled(!isConsoleMode());
			} else if (label.contains("Console")) {
				final String cLabel = (isConsoleMode()) ? "Exit Console Mode"
						: "Enter Console Mode";
				item.setLabel(cLabel);
			}
		}
	}


	boolean isConsoleMode() {
		return matchingString.startsWith("!");
	}

	void quit() {
		WindowManager.removeWindow(frame);
		frame.dispose();
	}

	/* ActionEvent Methods */
	@Override
	public void actionPerformed(final ActionEvent e) {
		final Object b = e.getSource();
		if (b == prompt) {
			//setMatchingString(prompt.getText());
			if (filenames.size() == 1) {
				setSelectedItem(0);
				openItem(selectedItem);
			}
		} else if (b == optionsButton) {
			validateOptionsMenu();
			final Rectangle r = optionsButton.getBounds();
			optionsMenu.show(optionsButton, r.x, r.y);
		} else if (b == openButton) {
			openItem(selectedItem);
		} else if (b == closeButton) {
			quit();
		} else { // An entry in the optionsMenu has been selected
			final String command = e.getActionCommand();
			if (command.equals("Options...")) {
				showOptionsDialog();
			} else if (command.equals("Go To...")) {
				changeDirectory("");
			} else if (command.equals("Add to Favorites")) {
				addBookmark();
			} else if (command.equals("Print Current List")) {
				printList();
			} else if (command.equals("Refresh File List")) {
				resetFileList();
			} else if (command.contains("Console")) {
				if (isConsoleMode())
					resetFileList();
				else
					resetCommandList();
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
		try {
			setMatchingString(prompt.getText());
			updateList();
		} catch (final Exception e) {
		}
	}

	@Override
	public void removeUpdate(final DocumentEvent ev) {
		try {
			setMatchingString(prompt.getText());
			updateList();
		} catch (final Exception ignored) {
		}
	}

	// Plain text should not trigger this event
	@Override
	public void changedUpdate(final DocumentEvent ev) {
		try {
			setMatchingString(prompt.getText());
			updateList();
		} catch (final Exception ignored) {
		}
	}


	/* ListSelectionListener Methods */
	@Override
	public void valueChanged(final ListSelectionEvent e) {
		if (e.getValueIsAdjusting())
			return;
		setSelectedItem(table.getSelectedRow());
		openButton.setEnabled(isOpenable(selectedItem));
	}

	/* KeyListener Methods */
	public void keyPressed(final KeyEvent ke) {

		final int key = ke.getKeyCode();
		final Object source = ke.getSource();
		final boolean meta = (ke.isControlDown() || ke.isMetaDown());

		// Close if Esc, Ctrl+W or Cmd+W
		if (key == KeyEvent.VK_ESCAPE || (key == KeyEvent.VK_W && meta)) {
			quit();
		} else if (source == prompt) {

			// Open top hits if their shortcuts are pressed from prompt
			if (meta && key == KeyEvent.VK_1) {
				setSelectedItem(0);
				openItem(selectedItem);
			} else if (meta && key == KeyEvent.VK_2) {
				setSelectedItem(1);
				openItem(selectedItem);
			} else if (meta && key == KeyEvent.VK_3) {
				setSelectedItem(2);
				openItem(selectedItem);

			// Up or down arrows pressed in prompt: Move the focus to list
			} else if (key==KeyEvent.VK_UP || key==KeyEvent.VK_DOWN) {

				final int index = table.getSelectedRow();// list.getSelectedIndex();
				if (index>0)
					table.setRowSelectionInterval(index, index);
				else
					table.setRowSelectionInterval(0, 0);
				table.requestFocus();
			}
		} else if (source == table) {

			// Focus in list and left arrow key: move up in directory hierarchy
			if (key == KeyEvent.VK_LEFT) {
				selectParentDirectory(path);
				setSelectedItem(table.getSelectedRow());

			// Focus in list and right arrow key: List sub-directory
			} else if (key == KeyEvent.VK_RIGHT) {
				setSelectedItem(table.getSelectedRow());
				if (isFolder(selectedItem))
					selectSubDirectory(selectedItem);

			// Focus in list and enter: Open selected item
			} else if (key == KeyEvent.VK_ENTER) {
				ke.consume();
				setSelectedItem(table.getSelectedRow());
				openItem(selectedItem);

			// Focus in list and backspace or meta + up arrow: Switch focus back to prompt
			} else if (key == KeyEvent.VK_BACK_SPACE || (meta && key == KeyEvent.VK_UP)) {
				prompt.requestFocus();

				// Focus in list and up/down arrow key: Loop through list
			} else if (key == KeyEvent.VK_UP) {
				if (table.getSelectedRow() == 0)
					table.setRowSelectionInterval(tableModel.getRowCount() - 1, tableModel.getRowCount() - 1);
			} else if (key == KeyEvent.VK_DOWN) {
				if (table.getSelectedRow() == tableModel.getRowCount() - 1)
					table.setRowSelectionInterval(0, 0);
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
			if (e.getSource() == table) {
				setSelectedItem(table.getSelectedRow());
				openItem(selectedItem);
			} else if (e.getSource() == statusBar) {
				executeStatusBarActions();
			} else if (e.getSource() == tableHeader) {
				changeDirectory("");
			}
		}
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		if (e.getSource() == optionsButton && e.isPopupTrigger()) {
			validateOptionsMenu();
			optionsMenu.show(optionsButton, e.getX(), e.getY());
		}
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		if (e.getSource() != optionsButton && e.isPopupTrigger()){
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
		quit();
	}

	public void windowActivated(final WindowEvent e) {
		//if (IJ.isMacintosh() && frame!=null) {
		//	IJ.wait(10);
		//	frame.setMenuBar(Menus.getMenuBar());
		//}
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

	@SuppressWarnings("serial")
	private class TableModel extends AbstractTableModel {
		protected ArrayList<String> list;
		public final static int COLUMNS = 1;

		public TableModel() {
			list = new ArrayList<String>();
		}

		public boolean isCellEditable(final int row, final int column) {
			return false;
		}

		public void setData(final ArrayList<String> list) {
			this.list = list;
			fireTableDataChanged();
		}

		public int getColumnCount() {
			return COLUMNS;
		}

		public String getColumnName(final int column) {
			switch (column) {
			case 0:
				return path;
			}
			return null;
		}

		public int getRowCount() {
			return list.size();
		}

		public Object getValueAt(final int row, final int column) {
			if (row >= list.size())
				return null;
			return list.get(row);
		}

		public void setColumnWidths(final TableColumnModel columnModel) {
			columnModel.getColumn(0).setPreferredWidth(FRAME_WIDTH+10);
		}

	}


}
