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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import bar.FileDrop;
import bar.Utils;

/**
 * This class implements a file browser modeled after <a href=
 * "https://github.com/imagej/ImageJA/blob/master/src/main/java/ij/plugin/CommandFinder.java"
 * >CommandFinder</a> by Mark Longair and <a href=
 * "https://github.com/imagej/ImageJA/blob/master/src/main/java/ij/macro/FunctionFinder.java"
 * >FunctionFinder</a> by Jerome Mutterer. It is also influenced by Johannes
 * Schindelin's <a href=
 * "https://github.com/fiji/Fiji_Plugins/blob/master/src/main/java/fiji/util/Recent_Commands.java"
 * >Recent_Commands plugin</a> and a bit of DOS nostalgia. Drag and drop support
 * is implemented by <a
 * href="http://www.iharder.net/current/java/filedrop/">FileDrop</a>.
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
	private static final int FRAME_WIDTH = 250;
	private static final int FRAME_HEIGHT = 450;
	private String path = DEF_PATH;
	private int maxSize = DEF_MAX_SIZE;
	private boolean closeOnOpen = DEF_CLOSE_ON_OPEN;
	private boolean ijmLegacy = DEF_IJM_LEGACY;
	private boolean caseSensitive, regex, wholeWord = false;
	private String matchingString = "";

	private JFrame frame;
	private JTextField prompt;
	private JCheckBox regexCheckBox, caseSensitiveCheckBox, wholeWordCheckBox;
	private JScrollPane listPane;
	private JLabel statusBar;
	private JButton optionsButton, openButton, closeButton;
	private JPopupMenu optionsMenu;

	private ArrayList<String> filenames, bookmarks;
	private String selectedItem;
	private JTable table;
	private static TableModel tableModel;
	private JTableHeader tableHeader;

	public static void main(final String[] args) { Debug.run("BAR Commander...",""); }


	public void run(final String arg) {

		// Check if Commander is already running
		if (WindowManager.getWindow("BAR Commander") != null) {
			if (arg != null && !arg.isEmpty())
				IJ.showStatus("In Commander, type <" + arg + "> to start browsing...");
			IJ.selectWindow("BAR Commander");
			return;
		}

		// Check if a path has been specified in plugins.config
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
				try {
					UIManager.setLookAndFeel(UIManager
							.getSystemLookAndFeelClassName());
				} catch (final Exception ignored) {
				}
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						runInteractively();
					}
				});
			}
		}.start();

	}

	/** Initializes lists, builds and displays prompt */
	void runInteractively() {

		// Initialize file list and favorites list
		filenames = new ArrayList<String>();
		bookmarks = new ArrayList<String>();

		// Create search prompt
		prompt = new JTextField(PROMPT_PLACEHOLDER);
		prompt.setFont(prompt.getFont().deriveFont(15f));
		prompt.selectAll();
		prompt.getDocument().addDocumentListener(this);
		prompt.addActionListener(this);
		prompt.addKeyListener(this);

		// Created search options
		caseSensitiveCheckBox = new JCheckBox("Aa", caseSensitive);
		final Font cboxFont = caseSensitiveCheckBox.getFont();
		final int cboxHeight = cboxFont.getSize();
		final int cboxGap = caseSensitiveCheckBox.getIconTextGap();
		caseSensitiveCheckBox.putClientProperty("JComponent.sizeVariant", "small");
		caseSensitiveCheckBox.setIconTextGap(cboxGap-1);
		caseSensitiveCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(final ItemEvent ie) {
				caseSensitive = caseSensitiveCheckBox.isSelected();
				setMatchingString(prompt.getText());
				updateList();
			}
		});

		wholeWordCheckBox = new JCheckBox("Whole word", wholeWord);
		wholeWordCheckBox.putClientProperty("JComponent.sizeVariant", "small");
		wholeWordCheckBox.setIconTextGap(cboxGap-1);
		wholeWordCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(final ItemEvent ie) {
				wholeWord = wholeWordCheckBox.isSelected();
				setMatchingString(prompt.getText());
				updateList();
			}
		});

		regexCheckBox = new JCheckBox("Regex", regex);
		regexCheckBox.putClientProperty("JComponent.sizeVariant", "small");
		regexCheckBox.setIconTextGap(cboxGap-1);
		regexCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(final ItemEvent ie) {
				regex = regexCheckBox.isSelected();
				wholeWordCheckBox.setEnabled(!regex);
				caseSensitiveCheckBox.setEnabled(!regex);
				setMatchingString(prompt.getText());
				updateList();
			}
		});

		// Create options panel
		final JPanel cboxPanel = new JPanel(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0, 0, cboxHeight / 2, 0);
		c.gridx = 0; c.gridy = 0;
		cboxPanel.add(caseSensitiveCheckBox, c);
		c.gridx++;
		cboxPanel.add(wholeWordCheckBox, c);
		c.gridx++;
		cboxPanel.add(regexCheckBox, c);

		// Create search panel
		final JPanel promptPanel = new JPanel(new BorderLayout());
		promptPanel.add(prompt, BorderLayout.CENTER);
		promptPanel.add(cboxPanel, BorderLayout.PAGE_END);
		promptPanel.setFocusable(true);

		// Create table holding file list. Format it so it mimics a JList
		tableModel = new TableModel();
		table = new ScrollableTable();
		table.setModel(tableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setRowSelectionAllowed(true);
		table.setColumnSelectionAllowed(false);
		//table.setAutoCreateRowSorter(true);
		table.setShowGrid(false);
		table.setShowHorizontalLines(false);
		table.setShowVerticalLines(false);
		table.setRowMargin(0);
		table.setIntercellSpacing(new Dimension(0, 0));
		table.addKeyListener(this);
		table.addMouseListener(this);
		table.getSelectionModel().addListSelectionListener(this);

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

		// Use Column header as a path bar
		tableHeader = table.getTableHeader();
		tableHeader.setDefaultRenderer(new HeaderRenderer(table));
		tableHeader.addMouseListener(this);

		// Allow folders to be dropped in file list. Consider only first item dropped
		listPane = new JScrollPane(table);
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
					updateList();
				} catch (final java.io.IOException e) {
					error("Drag and Drop failed...");
				}
			}
		});

		// Create status bar
		statusBar = new JLabel();
		statusBar.addMouseListener(this);
		updateBrowserStatus();

		// Create popup menu and buttons
		optionsMenu = createOptionsMenu();
		final JPanel buttonPanel = new JPanel();
		closeButton = new JButton("Quit");
		closeButton.addActionListener(this);
		buttonPanel.add(closeButton);
		optionsButton = new JButton(". . .");
		optionsButton.addActionListener(this);
		buttonPanel.add(optionsButton);
		openButton = new JButton("Open");
		openButton.addActionListener(this);
		buttonPanel.add(openButton);

		final JPanel contained = new JPanel(new BorderLayout());
		final JPanel container = new JPanel(new BorderLayout());
		container.setFocusable(true);
		contained.add(statusBar, BorderLayout.CENTER);
		contained.add(buttonPanel, BorderLayout.PAGE_END);
		container.add(promptPanel, BorderLayout.PAGE_START);
		container.add(listPane, BorderLayout.CENTER);
		container.add(contained, BorderLayout.PAGE_END);

		// Set mnemonics
		regexCheckBox.setMnemonic(KeyEvent.VK_R);
		wholeWordCheckBox.setMnemonic(KeyEvent.VK_W);
		caseSensitiveCheckBox.setMnemonic(KeyEvent.VK_A);
		optionsButton.setMnemonic(KeyEvent.VK_PERIOD);
		closeButton.setMnemonic(KeyEvent.VK_Q);
		openButton.setMnemonic(KeyEvent.VK_O);

		// Populate file list. Update status and path bar
		setPath(path);
		updateList();

		// Display commander
		frame = new JFrame("BAR Commander");
		setDefaultTooltips();
		frame.add(container);
		frame.addWindowListener(this);
		frame.pack();
		frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
		frame.setVisible(true);
		//openButton.getRootPane().setDefaultButton(openButton);
		prompt.requestFocusInWindow();
		WindowManager.addWindow(frame);
	}

	/** Adds current path to "Favorites" menu */
	void addBookmark() {
		if (!bookmarks.contains(path)) {
			if (bookmarks.size() > 0)
				optionsMenu.remove(optionsMenu.getComponentCount() - 1);
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
		optionsMenu.remove(optionsMenu.getComponentCount() - 1);
		bookmarks.clear();
	}

	/** Creates optionsMenu */
	JPopupMenu createOptionsMenu() {
		final JPopupMenu popup = new JPopupMenu();
		final OptionsActionListener al = new OptionsActionListener();
		JMenuItem mi = new JMenuItem("Add to Favorites");
		mi.addActionListener(al);
		popup.add(mi);
		mi = new JMenuItem("Reveal Path");
		mi.addActionListener(al);
		popup.add(mi);
		mi = new JMenuItem("Go To...");
		mi.addActionListener(al);
		popup.add(mi);
		popup.addSeparator();
		mi = new JMenuItem("Refresh File List");
		mi.addActionListener(al);
		popup.add(mi);
		mi = new JMenuItem("Print Current List");
		mi.addActionListener(al);
		popup.add(mi);
		popup.addSeparator();

		mi = new JMenuItem("Enter Console Mode");
		mi.addActionListener(al);
		popup.add(mi);
		mi = new JMenuItem("Options...");
		mi.addActionListener(al);
		popup.add(mi);
		popup.addSeparator();

		return popup;
	}

	/** Creates "Favorites" (bookmarks) menu */
	JMenu createBookmarkMenu() {
		final OptionsActionListener al = new OptionsActionListener();
		final JMenu menu = new JMenu("Favorites");
		JMenuItem mi;
		for (final String bookmark : bookmarks) {
			mi = new JMenuItem(bookmark);
			mi.addActionListener(al);
			menu.add(mi);
		}
		return menu;
	}

	/** Displays History Menu */
	void showHistoryMenu() {
		final JPopupMenu popup = new JPopupMenu();
		final HistoryActionListener al = new HistoryActionListener();
		JMenuItem mi;
		mi = new JMenuItem("Save search");
		mi.addActionListener(al);
		popup.add(mi);
		if (prevSearches.size() > 0) {
			mi = new JMenuItem("Clear searches");
			mi.addActionListener(al);
			popup.add(mi);
			popup.addSeparator();
		}
		for (final String item : prevSearches) {
			mi = new JMenuItem(item);
			mi.addActionListener(al);
			popup.add(mi);
		}
		popup.show(historyButton, historyButton.getWidth() / 2, 0);
	}

	/**
	 * Displays a message in the status bar.
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
	void showStatus(final String msg, final long msecs, final boolean restore) {

		//final FontMetrics fm = statusBar.getFontMetrics(statusBar.getFont());
		//final int maxLength = msg.length() * FRAME_WIDTH / fm.stringWidth(msg);
		//if (msg.length() > maxLength)
		//	msg = msg.substring(0, maxLength - 3) + "...";

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
	 * displayed for at least 4s. This is achieved through a timerTask that
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
	 *            If true, the message is displayed for at least 4s. This is
	 *            achieved through a timerTask that keeps the freezeStatusBar
	 *            flag set to true for five seconds.
	 *
	 * @see log
	 * @see showStatus
	 */
	void error(final String errorMsg, final boolean persistent) {
		statusBar.setForeground(Color.RED);
		showStatus(errorMsg, (persistent) ? 4000 : 0, false);
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
			resetFileList("!" + cmd + " executed...");
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
	 * Reloads an un-filtered list of current path displaying a 'blink
	 * message' (visible only for ~half a second) in the status bar
	 */
	void resetFileList(final String blinkMsg) {
		resetFileList();
		log(blinkMsg, 500);
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

	/** Check if the specified path has been tagged as directory */
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

	/** Checks if the specified filename contains a known script extension */
	boolean isScript(final String file) {
		final String[] EXTS = { ".txt", ".bsh", ".clj", ".groovy", ".ijm",
				".js", ".py", ".rb", ".java" };
		for (final String ext : EXTS)
			if (file.endsWith(ext))
				return true;
		return false;
	}

	/**
	 * Prints current (filtered) list. An unfiltered list is printed when
	 * running from console.
	 */
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

	/** Replaces file list with console commands */
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
			if (match(getCaseSensitiveString(cmd), matchingCmd)) {
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

	/** Evaluates matches between string an pattern according to current search criteria */
	boolean match(final String string, final String pattern) {
		if (!validQuery(pattern)) {
			return true;
		} else if (regex) {
			return Pattern.compile(pattern).matcher(string).matches();
		} else if (wholeWord) {
			return Pattern.compile(".*\\b" + pattern + "\\b.*").matcher(string).matches();
		} else {
			return string.indexOf(pattern) >= 0;
		}
	}

	/** Converts string to lower case according to current search criteria */
	String getCaseSensitiveString(final String string) {
		return (this.regex || this.caseSensitive) ? string : string.toLowerCase(Locale.US);
	}

	/** Creates a list of filenames matching current search */
	void setFileList() {
		final FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(final File file) {
				if (file.isHidden())
					return false;
				String name = getCaseSensitiveString(file.getName());
				if (file.isDirectory())
					name += File.separator;
				return match(name, matchingString);
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
		this.matchingString = getCaseSensitiveString(newMatchingString);
	}

	/** Updates path bar (JTable header) */
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

	/** Creates HTML text for built-in help */
	String helpMessage() {
		final StringBuffer sb = new StringBuffer();
		sb.append("<html>");
		sb.append("<head>");
		sb.append("  <style type='text/css'>");
		sb.append("  .cnsl {color: blue; font-family: monospace;}");
		sb.append("  .srch {color: black; font-family: monospace;}");
		sb.append("   ol {margin-top: -10px; margin-left:20px;}");
		sb.append("   dl {margin-top: -10px;");
		sb.append("  </style>");
		sb.append("<title> BAR Commander</title>");
		sb.append("</head>");
		sb.append("<body><div WIDTH=500>");
		sb.append("  <h4>Description</h4>");
		sb.append("  BAR Commander is a file browser providing instant access to files ");
		sb.append("  just by typing abbreviations of filenames. ");
		sb.append("  It serves two purposes: 1) to expedite the opening of files and 2) to ");
		sb.append("  produce filtered lists of directory contents.");
		sb.append("  <br /> <br />");
		sb.append("  <h4>Examples:</h4>");
		sb.append("  <ol type='A'>");
		sb.append("    <li>Open <i>").append(IJ.getDirectory("luts")).append("glasbey.lut:</i></li>");
		sb.append("      <ol>");
		sb.append("        <li>Type: <span class='cnsl'>!luts</span> &mdash; Enter &mdash; ")
				.append("<span class='srch'>glas</span> &mdash; Enter</li>");
		sb.append("      </ol>");
		sb.append("    <li>Open <i>").append(Utils.getSnippetsDir()).append("Median_Filter.py:</i></li>");
		sb.append("      <ol>");
		sb.append("        <li>Type: <span class='cnsl'>!snip</span> &mdash; Enter &mdash; ")
				.append("<span class='srch'>med</span> &mdash; Enter</li>");
		sb.append("      </ol>");
		sb.append("    <li>List the contents of <i>").append(System.getProperty("user.home"))
				.append(File.separator).append("Desktop").append(File.separator).append(":<i></li>");
		sb.append("      <ol>");
		sb.append("        <li>Type: <span class='cnsl'>!home</span> &mdash; Enter &mdash; ")
				.append("<span class='srch'>desk</span> &mdash; Enter</li>");
		sb.append("      </ol>");
		sb.append("    <li>Extract the paths of all TIFF images in a directory:</li>");
		sb.append("      <ol>");
		sb.append("        <li>Drag and drop the desired folder into the Commander list</li>");
		sb.append("        <li>Type: <span class='srch'>.tif</span> &mdash; Enter</li>");
		sb.append("        <li>Choose <i>Print Current List</i> from the Options Menu</li>");
		sb.append("      </ol>");
		sb.append("    </li>");
		sb.append("    <li>Retrieve both TIFF and JPEG files in a directory:</li>");
		sb.append("      <ol>");
		sb.append("        <li>Enable <i>Regex</i>, then type: <span class='srch'>.*(tif|jpg)</span></li>");
		sb.append("      </ol>");
		sb.append("    </li>");
		sb.append("    <li>Retrieve all PNG files that <u>contain</u> the (sub)string \"ImageJ\":</li>");
		sb.append("      <ol>");
		sb.append("        <li>Enable <i>Regex</i>, then type: <span class='srch'>.*(ImageJ).*(png)</span></li>");
		sb.append("      </ol>");
		sb.append("    </li>");
		sb.append("    <li>Retrieve all PNG files that do <u>not contain</u> the (sub)string \"ImageJ\":</li>");
		sb.append("      <ol>");
		sb.append("        <li>Enable <i>Regex</i>, then type: <span class='srch'>((?!ImageJ).)*(png)</span></li>");
		sb.append("      </ol>");
		sb.append("  </ol>");
		sb.append("  <h4>Tips:</h4>");
		sb.append("  <dl>");
		sb.append("    <dt>Drag and drop alternatives:</dt>");
		sb.append("    <dd><i>Go To...</i> or the console commands <span class='cnsl'>!goto</span> or ")
				.append("<span class='cnsl'>!cd</span>.</dd>");
		sb.append("    <dt>Keyboard navigation in file list:</dt>");
		sb.append("    <dd>Browse the file list using the arrow keys. Press the first character of a ")
				.append("filename to jump to the first file starting with that letter. Additional ")
				.append("presses of the same letter will cycle through all the files starting with ")
				.append("that initial.</dd>");
		sb.append("  </dl>");
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
		gd.addCheckbox("Clear Favorites list", false);
		gd.enableYesNoCancel("OK", "Reset Options");
		gd.addHelp(helpMessage());
		gd.showDialog();
		if (gd.wasCanceled()) {
			log("Prompt dismissed...");
			frame.toFront();
			return;
		} else if (gd.wasOKed()) {
			maxSize = (int) Math.max(1, gd.getNextNumber());
			closeOnOpen = gd.getNextBoolean();
			ijmLegacy = gd.getNextBoolean();
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
			if (truncatedList)
				setStatusTooltip("Double-click to change list size or type <tt>!options</tt>.");
			else
				setStatusTooltip("Double-click to refresh contents or type <tt>!refresh</tt>.");
		}

		tableModel.setData(filenames);
	}

	/** Defines the actions triggered by double-clicking on the status/path bar */
	void executeStatusBarActions() {
		freezeStatusBar = false;
		if (isConsoleMode()) {
			resetCommandList();
			log("Commands reloaded...", 500);
		} else {
			if (truncatedList) {
				showOptionsDialog();
			} else {
				updateList();
				log("Contents reloaded...", 500);
			}
		}
		System.gc();
	}

	/** Defines tooltips for status bar */
	void setStatusTooltip(final String text) {
		final StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		sb.append(text);
		sb.append("</html>");
		statusBar.setToolTipText(sb.toString());
	}

	/** Defines static tooltips for frame components */
	void setDefaultTooltips() {
		final ToolTipManager ttm = ToolTipManager.sharedInstance();
		ttm.setInitialDelay(2 * ttm.getInitialDelay());
		ttm.setReshowDelay(2 * ttm.getReshowDelay());
		ttm.setDismissDelay(2 * ttm.getDismissDelay());
		final String metaKey = IJ.isMacOSX() ? "Cmd" : "Ctrl";
		prompt.setToolTipText("<html>Prompt shortcuts:<br>"
				+ "&emsp;&uarr; &darr;&ensp; Move to list<br>"
				+ "&emsp;! &emsp; Console mode<br>"
				+ "&emsp;&crarr;&ensp; Open filtered item <br>"
				+ "&emsp;" + metaKey + "+1&ensp; Open 1<sup>st</sup> hit<br>"
				+ "&emsp;" + metaKey + "+2&ensp; Open 2<sup>nd</sup> hit<br>"
				+ "&emsp;" + metaKey + "+3&ensp; Open 3<sup>rd</sup> hit</html>");
		table.setToolTipText("<html>Navigation shortcuts:<br>"
				+ "&emsp;&uarr; &darr;&ensp; Select items<br>"
				+ "&emsp;&crarr;&emsp; Open item<br>"
				+ "&emsp;&larr;&emsp; Parent directory<br>"
				+ "&emsp;&rarr;&emsp; Expand selected folder<br>"
				+ "&ensp;A-Z&ensp; Alphabetic scroll<br>"
				+ metaKey + "+&uarr;, &lArr;&ensp;Search prompt</html>");
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
		for (final Component item : optionsMenu.getComponents()) {
			if (item instanceof JMenuItem) {
				final String label = ((JMenuItem) item).getText();
				if (label.equals("Add to Favorites")
						|| label.equals("Reveal Path")
						|| label.equals("Print Current List")
						|| label.equals("Refresh File List")) {
					item.setEnabled(!isConsoleMode());
				} else if (label.contains("Console")) {
					final String cLabel = (isConsoleMode()) ? "Exit Console Mode"
							: "Enter Console Mode";
					((JMenuItem) item).setText(cLabel);
				}
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
			optionsMenu.show(optionsButton, optionsButton.getWidth()/2, 0);
		} else if (b == openButton) {
			openItem(selectedItem);
		} else if (b == closeButton) {
			quit();
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
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
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

	}

	/** Validates the specified search term */
	boolean validQuery(final String query) {
		return !(query.isEmpty() || query.equals(PROMPT_PLACEHOLDER));
	}


	/** Implements ActionListeners for the "options" dropdown menu */
	private class OptionsActionListener implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
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
				resetFileList("Contents reloaded...");
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

	/**
	 * Implements all JTable customizations, including alternate row coloring
	 * and ensuring that table auto-resizes while displaying horizontal
	 * scroll-bar as necessary. The latter is mainly a hack to bypass some
	 * historical limitation of JTable and may need to be removed.
	 * 
	 * Sources:
	 * http://stackoverflow.com/a/15015445
	 * http://www.camick.com/java/source/TableRowRenderingTip.java
	 * http://www.camick.com/java/source/TableColumnAdjuster.java
	 */
	@SuppressWarnings("serial")
	private class ScrollableTable extends JTable {

		private boolean trackViewportWidth = false;
		private boolean initiated = false;
		private boolean ignoreUpdates = false;

		@Override
		public Component prepareRenderer(final TableCellRenderer renderer,
				final int row, final int column) {
			final Component c = super.prepareRenderer(renderer, row, column);

			if (!isRowSelected(row)) {

				// Alternate row colors
				c.setBackground((row % 2 == 0) ? getBackground() : new Color(
						245, 245, 245));

				// Differentiate folders from files
				// final int modelRow = convertRowIndexToModel(row);
				// final String item = (String) getModel().getValueAt(modelRow,0);
				// if (item.endsWith(File.separator))
				// c.setFont(c.getFont().deriveFont(Font.BOLD));
			}
			return c;
		}

		@Override
		protected void initializeLocalVars() {
			super.initializeLocalVars();
			initiated = true;
			updateColumnWidth();
		}

		@Override
		public void addNotify() {
			super.addNotify();
			updateColumnWidth();
			getParent().addComponentListener(new ComponentAdapter() {
				@Override
				public void componentResized(final ComponentEvent e) {
					invalidate();
				}
			});
		}

		@Override
		public void doLayout() {
			super.doLayout();
			if (!ignoreUpdates) {
				updateColumnWidth();
			}
			ignoreUpdates = false;
		}

		protected void updateColumnWidth() {
			if (getParent() != null) {
				int width = 0;
				for (int col = 0; col < getColumnCount(); col++) {
					int colWidth = 0;
					for (int row = 0; row < getRowCount(); row++) {
						final int prefWidth = getCellRenderer(row, col)
								.getTableCellRendererComponent(this,
										getValueAt(row, col), false, false,
										row, col).getPreferredSize().width;
						colWidth = Math.max(colWidth, prefWidth
								+ getIntercellSpacing().width);
					}

					final TableColumn tc = getColumnModel().getColumn(
							convertColumnIndexToModel(col));
					tc.setPreferredWidth(colWidth);
					width += colWidth;
				}

				Container parent = getParent();
				if (parent instanceof JViewport) {
					parent = parent.getParent();
				}

				trackViewportWidth = width < parent.getWidth();
			}
		}

		@Override
		public void tableChanged(final TableModelEvent e) {
			super.tableChanged(e);
			if (initiated) {
				updateColumnWidth();
			}
		}

		public boolean getScrollableTracksViewportWidth() {
			return trackViewportWidth;
		}

		@Override
		protected TableColumnModel createDefaultColumnModel() {
			final TableColumnModel model = super.createDefaultColumnModel();
			model.addColumnModelListener(new TableColumnModelListener() {
				@Override
				public void columnAdded(final TableColumnModelEvent e) {
				}

				@Override
				public void columnRemoved(final TableColumnModelEvent e) {
				}

				@Override
				public void columnMoved(final TableColumnModelEvent e) {
					if (!ignoreUpdates) {
						ignoreUpdates = true;
						updateColumnWidth();
					}
				}

				@Override
				public void columnMarginChanged(final ChangeEvent e) {
					if (!ignoreUpdates) {
						ignoreUpdates = true;
						updateColumnWidth();
					}
				}

				@Override
				public void columnSelectionChanged(final ListSelectionEvent e) {
				}
			});
			return model;
		}
	}

	/** Implements TableHeader ("path bar") customizations */
	private static class HeaderRenderer implements TableCellRenderer {

		final DefaultTableCellRenderer renderer;

		public HeaderRenderer(final JTable table) {
			renderer = (DefaultTableCellRenderer) table.getTableHeader()
					.getDefaultRenderer();
		}

		@Override
		public Component getTableCellRendererComponent(final JTable table,
				final Object value, final boolean isSelected,
				final boolean hasFocus, final int row, final int col) {
			final JLabel lbl = (JLabel) renderer.getTableCellRendererComponent(
					table, value, isSelected, hasFocus, row, col);
			lbl.setHorizontalAlignment(JLabel.LEFT);
			lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
			return lbl;
		}
	}

}
