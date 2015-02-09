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

		// Populate file list and initialize bookmarks
		filenames = new Vector<String>();
		setList();
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
		//dialog.setAlwaysOnTop(true);
		dialog.setVisible(true);
		dialog.toFront();
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

	void clearBookmarks() {
		optionsMenu.remove(optionsMenu.getItemCount() - 1);
		bookmarks.clear();
	}

	PopupMenu createOptionsMenu() {
		final PopupMenu popup = new PopupMenu();

		MenuItem mi = new MenuItem("Bookmark");
		mi.addActionListener(this);
		popup.add(mi);
		mi = new MenuItem("Reveal Path");
		mi.addActionListener(this);
		popup.add(mi);
		mi = new MenuItem("Print List");
		mi.addActionListener(this);
		popup.add(mi);
		popup.addSeparator();

		mi = new MenuItem("Go To...");
		mi.addActionListener(this);
		popup.add(mi);
		popup.addSeparator();

		mi = new MenuItem("Settings...");
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

	void changeDirectory(String newDir) {
		if (newDir == null || newDir.isEmpty())
			newDir = IJ.getDirectory("Choose new directory");
		if (newDir != null) {
			setPath(newDir);
			matchingString = "";
			prompt.setText("");
			updateStatus(path);
			updateList();
			list.setSelectedIndex(0);
			list.requestFocus();
		}
	}

	boolean isOpenable(String path) {
		if (path == null)
			return false;
		final String[] compiledExtensions = { ".jar", ".class", ".pyc" };
		path = path.toLowerCase();
		for (final String ext : compiledExtensions) {
			if (path.endsWith(ext))
				return false;
		}
		return true;
	}

	boolean isFolder(final String path) {
		return path != null && path.endsWith(File.separator);
	}

	void openItem(final String filename) {
		if (filename.equals("..")) {
			selectParentDirectory(path);
			return;
		} else if (isFolder(filename)) {
			selectSubDirectory(filename);
			return;
		} else if (isOpenable(filename)) {
			if (scriptFile(filename)) {
				if (ijmLegacy && filename.toLowerCase().endsWith("ijm"))
					Utils.openIJ1Script(path, filename);
				else
					Utils.openScript(path, filename);
			} else {
				IJ.open(path + filename);
			}
			if (closeOnOpen)
				dialog.dispose();
		}
	}

	/*
	private void maintainPanelWidth(final JScrollPane pane) {
		final int panelWidth = pane.getWidth();
		final int panelPreferredHeight = (int) pane.getPreferredSize().getHeight();
		final int panelMaximumHeight = (int) pane.getMaximumSize().getHeight();
		pane.setPreferredSize(new Dimension(panelWidth, panelPreferredHeight));
		pane.setMaximumSize(new Dimension(panelWidth, panelMaximumHeight));
	}
	*/

	boolean scriptFile(final String file) {
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

	void setList() {
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
		final Thread t1 = new Thread(new Runnable() {
			public void run() {
				setList();
				list.setListData(filenames);
			}
		});
		t1.start();
	}

	void updateStatus(String msg) {
		if (truncatedList) {
			status.setText("Displaying only " + maxSize + " items...");
			status.setToolTipText("Double-click to change list size");
		} else {
			final int length = msg.length();
			final int MAX_LENGTH = (IJ.isMacOSX()) ? 30 : 25;
			if (length > MAX_LENGTH)
				msg = " ..." + msg.substring(length - MAX_LENGTH);
			status.setText(msg);
			status.setToolTipText("Double-click to change directory");
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
			openItem(selectedItem);
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
				openItem(selectedItem);
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
				openItem(selectedItem);
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
		if (e.getSource() != list && e.isPopupTrigger())
			optionsMenu.show(optionsButton, e.getX(), e.getY());
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		if (e.getSource() != list && e.isPopupTrigger())
			optionsMenu.show(optionsButton, e.getX(), e.getY());
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
