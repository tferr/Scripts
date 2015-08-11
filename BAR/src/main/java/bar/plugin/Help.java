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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.DefaultEditorKit;

import bar.Utils;
import fiji.Debug;
import ij.IJ;
import ij.ImageJ;
import ij.Menus;
import ij.WindowManager;
import ij.plugin.BrowserLauncher;
import ij.plugin.PlugIn;

/** Implements the <i>BAR>About BAR...</i> command */
public class Help implements PlugIn {

	/** Parameters **/
	private JFrame frame;
	private static String FRAME_TITLE = "About BAR...";

	/**
	 * Calls {@link fiji.Debug#runFilter(String, String, String)
	 * fiji.Debug.runFilter()} so that the plugin can be debugged from an IDE
	 */
	public static void main(final String[] args) {
		Debug.run(FRAME_TITLE, "");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	public void run(final String arg) {

		Utils.shiftClickWarning();

		// Check if "About" window is already being displayed
		if (WindowManager.getWindow(FRAME_TITLE) == null) {
			try {
				UIManager.setLookAndFeel(UIManager
						.getSystemLookAndFeelClassName());
			} catch (final Exception ignored) {
			}
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					displayGUI();
				}
			});
		} else {
			IJ.selectWindow(FRAME_TITLE);
		}

	}

	/** Displays the customized "About..." dialog. */
	void displayGUI() {

		frame = new JFrame(FRAME_TITLE);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(final WindowEvent we) {
				quit();
			}

			public void windowActivated(final WindowEvent we) {
				if (IJ.isMacintosh() && frame != null) {
					IJ.wait(10);
					frame.setMenuBar(Menus.getMenuBar());
				}
			}
		});

		// Allow frame to be dismissed using the keyboard
		final KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(
				KeyEvent.VK_ESCAPE, 0);
		@SuppressWarnings("serial")
		final Action escapeAction = new AbstractAction() {
			public void actionPerformed(final ActionEvent e) {
				quit();
			}
		};
		frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
				.put(escapeKeyStroke, "ESCAPE");
		frame.getRootPane().getActionMap().put("ESCAPE", escapeAction);

		// Pane for HTML contents
		final JEditorPane htmlPane = new JEditorPane();
		htmlPane.setEditable(false);
		htmlPane.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		htmlPane.setContentType("text/html");
		addPopupMenu(htmlPane);

		// Change default font
		try {
			htmlPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES,
					true);
			htmlPane.setFont(UIManager.getFont("Label.font"));
		} catch (final Exception ignored) {
		}

		// Add contents
		htmlPane.setText("<HTML>"
				+ "<head>"
				+ "<style type=\"text/css\">"
				+ "h3 {margin-bottom:0; margin-top:15} "
				+ "a {color:#002390; text-decoration:none} "
				+ "</style>"
				+ "</head>"
				+ "<div WIDTH=390>"
				+ "<h3>BAR version "
				+ Utils.getVersion()
				+ "</h3>"
				+ "A curated collection of <i>Broadly Applicable Routines</i> for ImageJ"
				+ "<h3>Author/Maintainer</h3>"
				+ "<a href='http://imagej.net/User:Tiago'>Tiago Ferreira</a>"
				+ "<h3>Contributors</h3>"
				+ "Maxime Pinchon, Johannes Schindelin, Wayne Rasband, "
				+ "Jérôme Mutterer, Kota Miura, Jan Eglinger and <a href='https://github.com/tferr/Scripts#contributors'>many others</a>"
				+ "<h3>License</h3>"
				+ "<a href='http://opensource.org/licenses/GPL-3.0'>GNU General Public License (GPL)</a>"
				+ "<h3>Release Notes</h3>"
				+ "<a href='https://github.com/tferr/Scripts/releases'>All versions</a> | <a href='https://github.com/tferr/Scripts/releases/latest'>Latest version</a>"
				+ "<h3>Issues</h3>"
				+ "<a href='https://github.com/tferr/Scripts/issues?q='>All</a> | <a href='https://github.com/tferr/Scripts/issues?q=is%3Aopen'>Open</a> | "
				+ "<a href='https://github.com/tferr/Scripts/issues?q=is%3Aclosed'>Closed</a> | <a href='https://github.com/tferr/Scripts/issues?q=is%3Apr'>Pull requests</a>"
				+ "</div></HTML>");
		// Ensure panel is scrolled to the top
		htmlPane.setCaretPosition(0);

		// Make URLs browsable
		htmlPane.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(final HyperlinkEvent e) {
				if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType()))
					try {
						BrowserLauncher.openURL(e.getURL().toString());
					} catch (final IOException ignored) {
					}
			}
		});

		// Panel to hold HTML pane
		final JScrollPane scrollPane = new JScrollPane(htmlPane);
		scrollPane.setPreferredSize(new Dimension(415, 215));
		frame.add(scrollPane, BorderLayout.CENTER);

		// Panel to hold side buttons, all having fixed width
		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));

		addHeaderLabel(buttonPanel, "BAR Resources:");
		JButton button = URLButton("User Documentation", Utils.getDocURL());
		buttonPanel.add(button);
		button = URLButton("GitHub Documentation", Utils.getSourceURL()
				+ "#ij-bar");
		buttonPanel.add(button);
		button = URLButton("Source Code", Utils.getSourceURL());
		buttonPanel.add(button);
		button = URLButton("API", Utils.getJavadocURL());
		buttonPanel.add(button);

		addHeaderLabel(buttonPanel, "ImageJ Resources:");
		button = URLButton("Search Portal", "http://search.imagej.net");
		buttonPanel.add(button);
		button = URLButton("Javadocs", "http://javadoc.imagej.net");
		buttonPanel.add(button);


		addHeaderLabel(buttonPanel, "Utilities:");
		button = plainButton("Open BAR Directory");
		buttonPanel.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				Utils.revealFile(Utils.getBARDir());
			}
		});
		button = plainButton("Check for Updates...");
		buttonPanel.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				quit();
				IJ.doCommand("Update Fiji");
			}
		});
		frame.add(buttonPanel, BorderLayout.WEST);

		// Improve cross-platform rendering
		final ImageJ ij = IJ.getInstance();
		if (ij != null && !IJ.isMacOSX()) {
			final Image img = ij.getIconImage();
			if (img != null)
				try {
					frame.setIconImage(img);
				} catch (final Exception ignored) {
				}
		}

		frame.pack();
		frame.setVisible(true);
		WindowManager.addWindow(frame);

	}

	/** Adds a customized heading to the specified panel. */
	void addHeaderLabel(final JPanel p, final String label) {
		final JLabel lbl = new JLabel(label);
		lbl.setForeground(Color.DARK_GRAY);
		lbl.setBorder(new EmptyBorder(10, 3, 0, 0));
		final Font lblFont = lbl.getFont();
		lbl.setFont(lblFont.deriveFont((float) (lblFont.getSize() - 1.5)));
		p.add(lbl);
	}

	/**
	 * Constructs a JButton with the specified label that opens the specified
	 * URL in the user's Browser
	 */
	JButton URLButton(final String label, final String URL) {
		final JButton button = new JButton(label);
		button.setMaximumSize(new Dimension(Integer.MAX_VALUE, button
				.getMinimumSize().height));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				try {
					BrowserLauncher.openURL(URL);
				} catch (final Exception localException) {
					IJ.handleException(localException);
				}
			}
		});
		return button;

	}

	/** Returns a button w/ "maximized" width */
	JButton plainButton(final String label) {
		final JButton button = new JButton(label);
		button.setMaximumSize(new Dimension(Integer.MAX_VALUE, button
				.getMinimumSize().height));
		return button;
	}

	/** Adds a basic popupMenu to the specified pane */
	void addPopupMenu(final JEditorPane pane) {
		final JPopupMenu menu = new JPopupMenu();
		final ActionMap actionMap = pane.getActionMap();

		// Copy to clipboard
		final Action cAction = actionMap.get(DefaultEditorKit.copyAction);
		final JMenuItem cItem = new JMenuItem(cAction);
		cItem.setText("Copy");
		menu.add(cItem);

		// Select all
		final Action sAction = actionMap.get(DefaultEditorKit.selectAllAction);
		final JMenuItem sItem = new JMenuItem(sAction);
		sItem.setText("Select All");
		menu.add(sItem);

		// Invert colors
		final JMenuItem iItem = new JMenuItem("Invert Colors");
		iItem.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				final Color bColor = pane.getBackground();
				final Color fColor = pane.getForeground();
				pane.setBackground(fColor);
				pane.setForeground(bColor);
			}
		});
		menu.addSeparator();
		menu.add(iItem);

		// Disable irrelevant items
		pane.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(final MouseEvent e) {
				enableItems(e);
			}

			@Override
			public void mouseReleased(final MouseEvent e) {
				enableItems(e);
			}

			private void enableItems(final MouseEvent e) {
				if (e.isPopupTrigger()) {
					pane.requestFocusInWindow();
					final String selectedText = pane.getSelectedText();
					cItem.setEnabled(selectedText != null
							&& selectedText.length() > 0);
				}

			}
		});

		pane.setComponentPopupMenu(menu);
	}

	/** Disposes and unregisters main frame from WindowManager */
	void quit() {
		frame.dispose();
		WindowManager.removeWindow(frame);
	}

}