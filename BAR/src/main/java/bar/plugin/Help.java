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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;

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

import net.imagej.ImageJ;
import net.imagej.ui.swing.updater.ImageJUpdater;

import org.scijava.app.StatusService;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.platform.PlatformService;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

import bar.Utils;


/** Implements the {@literal About BAR...} command */
@Plugin(type = Command.class, menu = { @Menu(label = "BAR"), @Menu(label = "Help", weight = 0.01d),
		@Menu(label = "About & Resources...") })
public class Help implements Command {

	@Parameter
	private static CommandService commandService;

	@Parameter
	private static StatusService statusService;

	@Parameter
	private static PlatformService platformService;

	@Parameter
	private static UIService uiService;

	/** Parameters **/
	private static JFrame frame;
	private static String FRAME_TITLE = "About BAR...";

	public static void main(final String... args) throws Exception {
		final ImageJ ij = net.imagej.Main.launch(args);
		ij.command().run(Help.class, true);
	}

	@Override
	public void run() {

		Utils.shiftClickWarning();

		// Check if "About" window is already being displayed
		if (frame == null) {
			try {
				UIManager.setLookAndFeel(UIManager
						.getSystemLookAndFeelClassName());
			} catch (final Exception ignored) {
			}
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					displayGUI();
				}
			});
		} else {
			frame.setVisible(true);
			frame.toFront();
		}

	}

	/** Displays the customized "About..." dialog. */
	void displayGUI() {

		frame = new JFrame(FRAME_TITLE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent we) {
				quit();
			}
		});

		// Allow frame to be dismissed using the keyboard
		final KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(
				KeyEvent.VK_ESCAPE, 0);
		@SuppressWarnings("serial")
		final Action escapeAction = new AbstractAction() {
			@Override
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
		final String inLineSpacer = "&nbsp;&nbsp;|&nbsp;&nbsp;";
		htmlPane.setText("<HTML>"
				+ "<head>"
				+ "<style type=\"text/css\">"
				+ "h3 {margin-bottom:0; margin-top:15} "
				+ "a {color:#002390; text-decoration:none} "
				+ "</style>"
				+ "</head>"
				+ "<div WIDTH=390>"
				+ "<h3>BAR version "
				+ Utils.VERSION + " " + Utils.BUILD_DATE
				+ "</h3>"
				+ "A curated collection of <i>Broadly Applicable Routines</i> for ImageJ"
				+ "<h3>Author/Maintainer</h3>"
				+ "<a href='http://imagej.net/User:Tiago'>Tiago Ferreira</a> with vital contributions "
				+ "from <a href='https://github.com/tferr/Scripts#contributors'>many others</a>."
				+ "<h3>Citation</h3>"
				+ "<a href='https://zenodo.org/badge/latestdoi/8709403'>"
				+ "Ferreira et al (" + Utils.BUILD_YEAR +")"
				+ ". (Click for Zenodo DOI)</a>"
				+ "<h3>Development</h3>"
				+ "<a href='"+ Utils.getSourceURL() + "'>Source</a>" + inLineSpacer
				+ "<a href='"+ Utils.getSourceURL() + "/releases'>Release History</a>" + inLineSpacer
				+ "<a href='"+ Utils.getSourceURL() + "/issues'>Issues</a>" + inLineSpacer
				+ "<a href='"+ Utils.getSourceURL() + "/issues?q=is%3Apr'>Pull requests</a>" + inLineSpacer
				+ "<a href='"+ Utils.getJavadocURL() + "'>API</a>"
				+ "<h3>License</h3>"
				+ "<a href='http://opensource.org/licenses/GPL-3.0'>GNU General Public License (GPL)</a>"
				+ "</div></HTML>");
		// Ensure panel is scrolled to the top
		htmlPane.setCaretPosition(0);

		// Make URLs browsable
		htmlPane.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(final HyperlinkEvent e) {
				if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType()))
					try {
						platformService.open(e.getURL());
					} catch (final IOException ignored) {
						// ignored
					}
			}
		});

		// Panel to hold HTML pane
		final JScrollPane scrollPane = new JScrollPane(htmlPane);
		scrollPane.setPreferredSize(new Dimension(420, 220));
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

		addHeaderLabel(buttonPanel, "ImageJ Resources:");
		button = URLButton("Search Portal", "http://search.imagej.net");
		buttonPanel.add(button);
		button = URLButton("Forum", "http://forum.imagej.net");
		buttonPanel.add(button);
		button = URLButton("Javadocs", "http://javadoc.imagej.net");
		buttonPanel.add(button);


		addHeaderLabel(buttonPanel, "Utilities:");
		button = plainButton("<html>Open <i>My Routines</i>");
		buttonPanel.add(button);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				Utils.revealFile(Utils.getMyRoutinesDir());
			}
		});
		button = plainButton("Check for Updates...");
		buttonPanel.add(button);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				quit();
				commandService.run(ImageJUpdater.class, true);
			}
		});
		frame.add(buttonPanel, BorderLayout.WEST);
		frame.pack();
		frame.setVisible(true);

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
	JButton URLButton(final String label, final String url) {
		final JButton button = new JButton(label);
		button.setMaximumSize(new Dimension(Integer.MAX_VALUE, button
				.getMinimumSize().height));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				try {
					platformService.open(new URL(url));
				} catch (final IOException exc) {
					exc.printStackTrace();
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
			@Override
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

	/** Custom instructions could go here */
	void quit() {
		frame.dispose();
	}

}