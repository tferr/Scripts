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

import java.awt.AWTEvent;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Point;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import bar.Utils;
import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.gui.ImageWindow;
import ij.gui.NonBlockingGenericDialog;
import ij.gui.Plot;
import ij.gui.PlotWindow;
import ij.gui.YesNoCancelDialog;
import ij.measure.ResultsTable;
import ij.plugin.Colors;
import ij.plugin.PlugIn;
import ij.plugin.frame.Recorder;
import ij.util.Tools;

/**
 * Interactively creates a multi-series plot (XY with or without error bars, and
 * vector fields) from existing ImageJ tables and plots, the system clipboard or
 * imported spreadsheet data. Mix and match of multiple data sources is allowed,
 * and it is also possible to append series to existing plots. It also provides
 * a GUI to advanced features of the {@link Plot} API that are usually not
 * accessible from ImageJ's GUI. It was originally implemented as a BeanShell
 * script.
 */
public class InteractivePlotter implements PlugIn {

	/** Definitions of series shapes as per ij.gui.Plot.toShape() */
	private final String[] S_LABELS = { "line", "x", "box", "connected", "triangle", "dot", "circle", "cross" };

	/** The command name associated with this plugin */
	private final String CMD_LABEL = "Interactive Plotter";

	/** ResultsTable from which data is retrieved */
	private ResultsTable rt;

	/** Flag monitoring the closure of plot's window */
	private boolean pwClosed;

	private Plot plot;
	private PlotWindow pw;
	private String legend = "";
	private int datasetCounter;
	private String dShape = "line";
	private String dColor = "red";
	private String dColor2 = "black";
	private boolean buildLegend = true;
	private boolean autoNextValues = true;
	private boolean autoNextColor = true;
	private boolean autoNextShape;
	private static NonBlockingGenericDialog prompt;
	private String[] colChoices;
	private boolean vectorData;
	protected JPopupMenu optionsMenu;
	private JMenuItem customizePlotJMI;
	private JMenuItem changeTargetPlotWindowJMI;
	private Button datasetButton;
	private final String DIALOG_LOC_KEY = "bar.PRloc";

	public static void main(final String[] args) {
		new ij.ImageJ();
		final InteractivePlotter plugin = new InteractivePlotter();
		plugin.run("");
	}

	@Override
	public synchronized void run(final String arg) {

		if (prompt != null && prompt.isVisible()) {
			prompt.toFront();
			return;
		}

		if (Recorder.record) {
			Recorder.recordString("// Most features of " + CMD_LABEL
					+ " are not recordable. Use the " + ((Recorder.scriptMode())
							? "ij.gui.Plot API (http://javadoc.imagej.net)" : "IJ built-in macro functions")
					+ " instead\n");
		}

		rt = getTable();
		if (rt != null)
			showDialog();

	}

	/**
	 * Adds a dataset to the plot according to the settings retrieved by
	 * {@link #promptListener}. Plot is created if it does not exist, i.e., if
	 * the method has not been called before.
	 */
	void addDataset(final String datasetLabel, final double[] x1, final double[] y1, final double[] x2,
			final double[] y2, final boolean vectorField) {

		final boolean newPlot = plot == null;
		if (newPlot) {
			final double[] xLimits = Tools.getMinMax(x1);
			final double[] yLimits = Tools.getMinMax(y1);
			if (xLimits[0] == Double.MAX_VALUE || yLimits[0] == Double.MAX_VALUE) {
				final YesNoCancelDialog query = new YesNoCancelDialog((Frame) prompt.getParent(), "Invalid data?",
						"Some column(s) do not seem to contain numeric data. Limits\n"
								+ "of axes may need to be set manually. Proceed nevertheless?");
				if (!query.yesPressed())
					return;
			}
			plot = new Plot(WindowManager.makeUniqueName("Plotted Results"), "X", "Y", Plot.DEFAULT_FLAGS);
			plot.setLimits(xLimits[0], xLimits[1], yLimits[0], yLimits[1]);
			pw = showPlot();
		}

		// Add the dataset (with vectors, all column choices must be valid)
		plot.setColor(dColor, dColor2);
		if (vectorField)
			plot.drawVectors(x1, y1, x2, y2);
		else {
			plot.addPoints(x1, y1, Plot.toShape(dShape));
			if (x2 != null)
				plot.addHorizontalErrorBars(x2);
			if (y2 != null)
				plot.addErrorBars(y2);
		}

		// Update plot legend, series tally, and axes limits
		if (buildLegend) {
			if (!newPlot)
				legend += "\n";
			legend += datasetLabel;
			plot.setColor(Color.BLACK);
			plot.addLegend(legend);
		}
		plot.setLimitsToFit(true);
		datasetCounter++;
	}

	/**
	 * Generates abscissae from row numbers of the specified ResultsTable.
	 * Useful for datasets in which only Y-values need to be plotted.
	 */
	double[] generateX(final ResultsTable table) {
		final int size = table.getCounter();
		final double[] incX = new double[size];
		for (int i = 0; i < size; i++)
			incX[i] = i;
		return incX;
	}

	/**
	 * Retrieves a list of column choices for the specified ResultsTable. The
	 * "Label" column (containing non-numeric data) is excluded).
	 */
	private String[] getColumnChoices(final ResultsTable table) {
		final int n = table.getLastColumn();
		final String[] cChoices = new String[n + 2];
		for (int i = 0; i <= n; i++)
			cChoices[i] = table.getColumnHeading(i);
		cChoices[n + 1] = "*None*";
		return cChoices;
	}

	/**
	 * Activates the Checkbox of a RadioButtonGroup() in a GenericDialog
	 *
	 * @param panel
	 *            the {@link Panel} to which the RadioButtonGroup
	 *            ({@link java.awt.CheckboxGroup}) was added as per
	 *            {@link GenericDialog#addRadioButtonGroup}.
	 * @param label
	 *            the label of the Checkbox to be activated
	 */
	private void activateRadioCheckbox(final Panel panel, final String label) {
		final Component[] components = panel.getComponents();
		for (int i = 0; i < components.length; i++) {
			final Component c = components[i];
			if (c instanceof Checkbox) {
				final Checkbox cb = (Checkbox) c;
				if (cb.getLabel().equals(label)) {
					cb.setState(true);
					return;
				}
			}
		}
		return;
	}

	/**
	 * Creates and displays the plugin's GUI. Position of dialog is stored using
	 * the ImageJ preferences mechanism. All the values in the prompt are
	 * retrieved in real time using the {@link DialogListener} interface
	 */
	private void showDialog() {

		if (colChoices == null)
			colChoices = getColumnChoices(rt);

		prompt = new NonBlockingGenericDialog(CMD_LABEL);
		prompt.addChoice("         X-values", colChoices, "*None*");
		prompt.addChoice(" X-error bars", colChoices, "*None*");
		prompt.addChoice("         Y-values", colChoices, colChoices[0]);
		prompt.addChoice(" Y-error bars", colChoices, "*None*");
		prompt.setInsets(0, 20, 0);
		prompt.addCheckbox("Vector field series", vectorData);
		prompt.addMessage("");
		prompt.addRadioButtonGroup("Style:", S_LABELS, 4, 2, dShape);
		prompt.addRadioButtonGroup("Color:", Colors.colors, 5, 2, dColor);

		// Add the "Add Dataset" button to the dialog
		datasetButton = new Button("       Add Series " + String.valueOf(datasetCounter + 1) + "       ");
		datasetButton.addActionListener(prompt);
		final Panel buttonPanel = new Panel();
		buttonPanel.add(datasetButton);
		prompt.addPanel(buttonPanel, GridBagConstraints.SOUTH, new Insets(10, 0, 0, 0));
		prompt.addCheckbox("Add to legend", buildLegend);

		prompt.addHelp("");
		prompt.setHelpLabel("Options...");
		prompt.setOKLabel("Close");
		prompt.hideCancelButton();
		optionsMenu = createOptionsMenu();
		prompt.addDialogListener(promptListener);
		prompt.addWindowFocusListener(new WindowAdapter() {
			@Override
			public void windowGainedFocus(final WindowEvent e) {
				updateDatasetButton();
			}

			@Override
			public void windowLostFocus(final WindowEvent e) {
				updateDatasetButton();
			}

		});

		// Display prompt at last screen position
		final Point loc = Prefs.getLocation(DIALOG_LOC_KEY);
		if (loc != null) {
			prompt.centerDialog(false);
			prompt.setLocation(loc);
		}

		// Update prompt through promptListener before displaying it
		new ActionEvent(prompt, ActionEvent.ACTION_PERFORMED, "startup");
		prompt.showDialog();
		Prefs.saveLocation(DIALOG_LOC_KEY, prompt.getLocation());

		// Dialog closure: Update plot before dismissing prompt
		if (prompt.wasOKed() && plot != null)
			plot.setLimitsToFit(true);

	}

	/** Tries to guess a {@link ResultsTable} column contains numeric data */
	private boolean validColumn(final String columnHeading) {
		return !columnHeading.equals("Label") && !columnHeading.equals("*None*")
				&& rt.getColumnIndex(columnHeading) != ResultsTable.COLUMN_NOT_FOUND;
	}

	/** Class used to retrieve options from prompt in in real-time */
	DialogListener promptListener = new DialogListener() {

		// This method is invoked by the GenericDialog upon changes
		@Override
		public boolean dialogItemChanged(final GenericDialog ignored, final java.awt.AWTEvent e) {

			if (e != null && e.toString().contains("Options...")) {
				updateOptionsMenu();
				optionsMenu.show((Button) e.getSource(), 0, 0);
				return true;
			}

			// Read parameters from dialog
			final int x1Col = prompt.getNextChoiceIndex();
			final int x2Col = prompt.getNextChoiceIndex();
			final int y1Col = prompt.getNextChoiceIndex();
			final int y2Col = prompt.getNextChoiceIndex();
			vectorData = prompt.getNextBoolean();
			dShape = prompt.getNextRadioButton();
			dColor = prompt.getNextRadioButton();
			buildLegend = prompt.getNextBoolean();

			// Define the dataset: A vector field or a X,Y series (using
			// incremental x-values if user chose a non-numeric column)
			double[] x1 = validColumn(colChoices[x1Col]) ? rt.getColumnAsDoubles(x1Col) : null;
			String x1Label = "        X-values";
			if (!vectorData && x1 == null) {
				x1 = generateX(rt);
				x1Label = "X-values (gen.)";
			}
			final boolean validX1 = x1 != null;
			final String datasetLabel = colChoices[y1Col];
			final double[] y1 = validColumn(datasetLabel) ? rt.getColumnAsDoubles(y1Col) : null;
			final boolean validY1 = y1 != null;
			final double[] x2 = validColumn(colChoices[x2Col]) ? rt.getColumnAsDoubles(x2Col) : null;
			final boolean validX2 = x2 != null;
			final double[] y2 = validColumn(colChoices[y2Col]) ? rt.getColumnAsDoubles(y2Col) : null;
			final boolean validY2 = y2 != null;

			// Assess if all dataset values are valid
			boolean validData = true;
			if (vectorData)
				validData = validX1 && validY1 && validX2 && validY2;
			else
				validData = validX1 && validY1;

			// Update dialog components if dialog is being displayed
			if (prompt.isShowing()) {

				datasetButton.setEnabled(validData);

				// FIXME: Indices of choice labels empirically determined
				updatePromptLabel(0, (vectorData ? "X-start (X1)" : x1Label), (validX1 ? Color.BLACK : Color.GRAY));
				updatePromptLabel(2, (vectorData ? "X-end (X2)" : "X-error bars"),
						(validX2 ? Color.BLACK : Color.GRAY));
				updatePromptLabel(4, (vectorData ? "Y-start (Y1)" : "      Y-values"),
						(validY1 ? Color.BLACK : Color.GRAY));
				updatePromptLabel(6, (vectorData ? "Y-end (Y2)" : "Y-error bars"),
						(validY2 ? Color.BLACK : Color.GRAY));

			}

			// Check if loadTableAndReloadPrompt(ResultsTable) or reload has
			// been called
			if (e != null && e.toString().contains("reload")) {
				prompt.getButtons()[0].setEnabled(validData);

				// Check if the 'Add dataset' button has been pressed
			} else if (e != null && e.getSource() == datasetButton) {
				if (pwClosed) {
					pw = showPlot();

				} else {

					// Plot data
					if (validData) {
						addDataset(datasetLabel, x1, y1, x2, x2, vectorData);
						datasetButton.setLabel("Add Series " + (datasetCounter + 1));

					} else {
						showMessage("Invalid data", "Chosen column(s) do not seem to contain numeric data.");
						return false;
					}

					// Auto-select defaults for next series
					if (autoNextValues) {
						@SuppressWarnings("unchecked")
						final Vector<Choice> choices = prompt.getChoices();
						final int y1idx = (datasetCounter % (colChoices.length - 1));
						choices.elementAt(2).select(y1idx);
					}
					if (autoNextShape) {
						final Panel shapeRadioButtonGroupPanel = (Panel) prompt.getComponent(11); // FIXME:
																									// Component
																									// index
																									// empirically
																									// determined
						activateRadioCheckbox(shapeRadioButtonGroupPanel, getNextItem(S_LABELS, dShape));
					}
					if (autoNextColor) {
						final Panel colorRadioButtonGroupPanel = (Panel) prompt.getComponent(13); // FIXME:
																									// Component
																									// index
																									// empirically
																									// determined
						activateRadioCheckbox(colorRadioButtonGroupPanel,
								getNextItem(Colors.colors, dColor, Colors.colors.length - 2)); // skip
																								// white
																								// color
					}
				}

				// Plot a single dataset if called from macro
			} else if (validData && IJ.macroRunning()) {
				addDataset(datasetLabel, x1, y1, x2, x2, vectorData);
			}

			return validData;
		}

	};

	private String getNextItem(final String[] array, final String currentItem) {
		return getNextItem(array, currentItem, array.length - 1);
	}

	private String getNextItem(final String[] array, final String currentItem, final int maxAllowedIdx) {
		for (int i = 0; i < array.length; i++) {
			if (array[i].equals(currentItem))
				return array[(i + 1 > maxAllowedIdx) ? 0 : i + 1];
		}
		return array[0];
	}

	private void updatePromptLabel(final int index, final String newLabel, final Color color) {
		try {
			synchronized (prompt.getTreeLock()) {
				final Label label = ((Label) prompt.getComponent(index));
				label.setAlignment(Label.RIGHT);
				label.setText(newLabel);
				label.setForeground(color);
			}
		} catch (final ArrayIndexOutOfBoundsException | ClassCastException ignored) {
			// Do nothing: index is not valid
		}
	}

	private void updateOptionsMenu() {
		final boolean exitinsPlot = pw != null && plot != null;
		customizePlotJMI.setEnabled(exitinsPlot);
		changeTargetPlotWindowJMI.setEnabled(exitinsPlot);
	}

	private class PlotInstance {
		private final PlotWindow plotWindow;
		@SuppressWarnings({ "hiding" })
		private Plot plot;
		private String title;

		private PlotInstance(final PlotWindow plotWindow) {
			this.plotWindow = plotWindow;
			if (plotWindow != null) {
				this.title = plotWindow.getTitle();
				this.plot = plotWindow.getPlot();
			}
		}
	}

	private void setDestinationPlot() {
		if (plot == null)
			return;

		final String NEW_PLOT_LABEL = "New plot window";
		final ArrayList<PlotInstance> plots = getPlots();
		final ArrayList<String> choices = new ArrayList<>();
		for (final PlotInstance p : plots)
			choices.add(p.title);
		choices.add(NEW_PLOT_LABEL);

		final GenericDialog gd = new GenericDialog("Set Destination Plot");
		final int cols = (choices.size() < 18) ? 1 : 2;
		final int rows = (choices.size() % cols > 0) ? choices.size() / cols + 1 : choices.size() / cols;
		gd.addRadioButtonGroup("Append datasets to:", choices.toArray(new String[choices.size()]), rows, cols,
				NEW_PLOT_LABEL);
		showAsSubDialog(gd);
		if (!gd.wasOKed())
			return;

		final String choice = gd.getNextRadioButton();
		if (choice.equals(NEW_PLOT_LABEL)) {
			resetPlot();
			updateDatasetButton();
		} else {
			final int idx = choices.indexOf(choice);
			pw = plots.get(idx).plotWindow;
			plot = plots.get(idx).plot;
			datasetCounter = plot.getPlotObjectDesignations().length;
			WindowManager.toFront(pw);
		}
		updateDatasetButton();

	}

	/**
	 * Displays a message dialog taking into account the position of the main
	 * plugin prompt
	 */
	private void showMessage(final String title, final String msg) {
		final GenericDialog gd = new GenericDialog(title);
		gd.addMessage(msg);
		gd.setLocationRelativeTo(prompt);
		gd.hideCancelButton();
		gd.showDialog();
	}

	private void setPreferences() {
		final GenericDialog gd = new GenericDialog("Preferences");
		gd.addChoice("Secondary style color:", Colors.colors, dColor2);
		gd.setInsets(0, 0, 0);
		gd.addMessage("(Used by styles 'box', 'circle', 'connected', etc.)");
		gd.setInsets(30, 0, 0);
		gd.addMessage("After plotting a series:");
		gd.setInsets(0, 20, 0);
		gd.addCheckbox("Auto-select_next_Y-values", autoNextValues);
		gd.addCheckbox("Auto-select_next_shape", autoNextShape);
		gd.addCheckbox("Auto-select_next_color", autoNextColor);
		showAsSubDialog(gd);
		if (gd.wasOKed()) {
			dColor2 = Colors.colors[gd.getNextChoiceIndex()];
			autoNextValues = gd.getNextBoolean();
			autoNextShape = gd.getNextBoolean();
			autoNextColor = gd.getNextBoolean();
		}
	}

	private void setTemplate() {
		if (plot == null)
			return;
		final ArrayList<PlotInstance> plots = getPlots();
		if (plots == null || plots.size() == 0) {
			showMessage("No Plots Available", "No open plots to be used as template!");
			return;
		}
		final GenericDialog gd = new GenericDialog("Apply Template");
		final String[] choices = new String[plots.size()];
		for (int i = 0; i < choices.length; i++)
			choices[i] = plots.get(i).title;
		gd.addChoice("Use_this_plot as template:", choices, null);
		showAsSubDialog(gd);
		if (!gd.wasOKed())
			return;
		plot.useTemplate(plots.get(gd.getNextChoiceIndex()).plot);
		plot.updateImage();
	}

	private ArrayList<PlotInstance> getPlots() {
		final ArrayList<PlotInstance> plots = new ArrayList<>();
		final int[] ids = WindowManager.getIDList();
		if (ids != null) {
			for (final int id : ids) {
				final ImagePlus pImp = WindowManager.getImage(id);
				if (pImp == null)
					continue;
				final ImageWindow win = pImp.getWindow();
				if (win == null)
					continue;
				if (win instanceof PlotWindow) {
					if (win.equals(pw))
						continue;
					plots.add(new PlotInstance((PlotWindow) win));
				}
			}
		}
		return plots;
	}

	private void setPlotOptions() {

		if (plot == null)
			return;

		final int DEF_LINE_WIDTH = 1;
		final int DEF_MAX_INTERVALS = 12;
		final int DEF_TICK_LENGTH = 7;
		final int DEF_MINOR_TICK_LENGTH = 3;
		final int[] NUM_DEFAULTS = { DEF_LINE_WIDTH, DEF_MAX_INTERVALS, DEF_TICK_LENGTH, DEF_MINOR_TICK_LENGTH };
		final String DEF_BACKGROUND_COLOR = "white";
		final String title = plot.getTitle();
		final GenericDialog ogd = new GenericDialog("Options for " + title);
		ogd.setInsets(0, 10, 10);
		ogd.addMessage("This prompt allows you access customizations that\n"
				+ "are not accessible through the plot's \"More \u00bb\" menu");
		if (!pwClosed)
			ogd.addStringField("Plot title:", title, 27);
		ogd.addSlider("Line width:", 1, 20, 1);
		ogd.addSlider("Max. n. of intervals:", 1, 40, 12);
		ogd.addSlider("Major ticks length:", 1, 14, 7);
		ogd.addSlider("Minor ticks length:", 1, 14, 3);
		ogd.addChoice("Backgrond color:", Colors.colors, DEF_BACKGROUND_COLOR);
		final Panel buttonPanel = new Panel();
		final Button fontButton = new Button("  Text & Font...  ");
		fontButton.addActionListener(ogd);
		buttonPanel.add(fontButton);
		final Button templateButton = new Button("Apply Template...");
		templateButton.addActionListener(ogd);
		buttonPanel.add(templateButton);
		ogd.addPanel(buttonPanel, GridBagConstraints.EAST, new Insets(0, 0, 0, 0));
		ogd.hideCancelButton();
		ogd.addHelp("");
		ogd.setHelpLabel("Apply Defaults");
		ogd.addDialogListener(new DialogListener() {
			@Override
			public boolean dialogItemChanged(final GenericDialog gd, final AWTEvent e) {

				if (e != null && e.toString().contains("Apply Defaults")) {
					@SuppressWarnings("unchecked")
					final Vector<TextField> nFields = gd.getNumericFields();
					for (final TextField field : nFields)
						field.setText(Integer.toString(NUM_DEFAULTS[nFields.indexOf(field)]));
					@SuppressWarnings("unchecked")
					final Vector<Choice> nChoices = gd.getChoices();
					nChoices.firstElement().select(DEF_BACKGROUND_COLOR);
				} else if (e != null && e.toString().contains("Font")) {
					setPlotFont();
					return true;
				} else if (e != null && e.toString().contains("Template")) {
					setTemplate();
					return true;
				}

				plot.setLineWidth((int) ogd.getNextNumber());
				plot.setMaxIntervals((int) ogd.getNextNumber());
				plot.setTickLength((int) ogd.getNextNumber());
				plot.setBackgroundColor(Colors.colors[ogd.getNextChoiceIndex()]);
				plot.updateImage();
				return true;

			}
		});
		showAsSubDialog(ogd);
		if (!ogd.wasOKed())
			return;
		if (!pwClosed)
			plot.getImagePlus().setTitle(WindowManager.makeUniqueName(ogd.getNextString()));

	}

	private void showAsSubDialog(final GenericDialog gd) {
		gd.setLocationRelativeTo(prompt);
		gd.showDialog();
		if (Recorder.record)
			Recorder.setCommand(Recorder.getCommand());
	}

	private void setPlotFont() {

		if (plot == null)
			return;

		final String[] FONTS = new String[] { Font.SANS_SERIF, Font.MONOSPACED, Font.SERIF };
		final String[] STYLES = { "Plain", "Bold", "Italic", "Bold+Italic" };
		final String[] JUSTIFICATIONS = { "Left", "Center", "Right" };
		final String[] SCOPES = { "Plot", "Both Axes Titles", "X-axis Title", "Y-axis Title" };
		final String[] CHOICE_DEFAULTS = { FONTS[0], STYLES[0], JUSTIFICATIONS[0], SCOPES[0] };
		final int[] INT_STYLES = { Font.PLAIN, Font.BOLD, Font.ITALIC, Font.BOLD + Font.ITALIC };
		final int[] INT_JUSTIFICATIONS = { Plot.LEFT, Plot.CENTER, Plot.RIGHT };
		final int DEF_SIZE = 12;
		final boolean DEF_ANTIALISED = true;

		final GenericDialog fgd = new GenericDialog("Font Options");
		fgd.addChoice("Type:", FONTS, CHOICE_DEFAULTS[0]);
		fgd.addChoice("Style:", STYLES, CHOICE_DEFAULTS[1]);
		fgd.addChoice("Justification:", JUSTIFICATIONS, CHOICE_DEFAULTS[2]);
		fgd.addSlider("Size:", 9, 24, DEF_SIZE);
		fgd.setInsets(0, 20, 15);
		fgd.addCheckbox("Antialiased text", DEF_ANTIALISED);
		fgd.addChoice("Apply to:", SCOPES, CHOICE_DEFAULTS[3]);
		fgd.hideCancelButton();
		fgd.addHelp("");
		fgd.setHelpLabel("Apply Defaults");
		fgd.addDialogListener(new DialogListener() {
			@Override
			public boolean dialogItemChanged(final GenericDialog gd, final AWTEvent e) {
				if (e != null && e.toString().contains("Apply Defaults")) {
					@SuppressWarnings("unchecked")
					final Vector<Choice> nChoices = gd.getChoices();
					for (final Choice choice : nChoices)
						choice.select(CHOICE_DEFAULTS[nChoices.indexOf(choice)]);
					((TextField) gd.getNumericFields().get(0)).setText(Integer.toString(DEF_SIZE));
					((Checkbox) gd.getCheckboxes().get(0)).setState(DEF_ANTIALISED);
				}

				final String type = FONTS[fgd.getNextChoiceIndex()];
				final int style = INT_STYLES[fgd.getNextChoiceIndex()];
				final int justification = INT_JUSTIFICATIONS[fgd.getNextChoiceIndex()];
				final int size = (int) fgd.getNextNumber();
				final int scopeIdx = fgd.getNextChoiceIndex();

				plot.setJustification(justification);
				plot.setAntialiasedText(fgd.getNextBoolean());
				final Font font = new Font(type, style, size);
				switch (scopeIdx) {
				case 1: // SCOPES[1]
					plot.setXLabelFont(font);
					plot.setYLabelFont(font);
					break;
				case 2: // SCOPES[2]
					plot.setXLabelFont(font);
					break;
				case 3: // SCOPES[3]
					plot.setYLabelFont(font);
					break;
				default:
					plot.setFont(font);
					plot.setXLabelFont(font);
					plot.setYLabelFont(font);
					break;
				}

				plot.updateImage();
				return true;
			}
		});
		showAsSubDialog(fgd);
		if (!fgd.wasOKed())
			return;
	}

	/** Forces data to be plotted in a new plot */
	private void resetPlot() {
		datasetCounter = 0;
		legend = "";
		plot = null;
		pw = null;
		pwClosed = false;
	}

	/**
	 * Displays the specified plot and returns its PlotWindow. A WindowListener
	 * is used to monitor it is closure.
	 */
	private PlotWindow showPlot() {
		if (plot == null)
			return null;
		pwClosed = false;
		pw = plot.show();
		pw.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(final WindowEvent e) {
				if (e.getWindow() == pw) {
					pwClosed = true;
					updateDatasetButton();
				}
			}
		});
		return pw;
	}

	/** Returns the ResultsTable to be used as data input. */
	ResultsTable getTable() {
		return Utils.getTable(prompt, false, rt == null, null);
	}

	private void loadTableAndReloadPrompt(final ResultsTable table) {

		if (table == null || prompt == null)
			return;

		rt = table;

		@SuppressWarnings("unchecked")
		final Vector<Choice> choices = prompt.getChoices();
		final Vector<String> previousValues = new Vector<>();
		for (final Choice choice : choices)
			previousValues.add(choice.getSelectedItem());
		colChoices = getColumnChoices(table);
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				for (final Choice choice : choices) {
					choice.removeAll();
					for (final String colChoice : colChoices)
						choice.add(colChoice);
					choice.select(previousValues.elementAt(choices.indexOf(choice)));
				}
				new ActionEvent(choices.elementAt(0), ActionEvent.ACTION_PERFORMED, "reload");
			}
		});
	}

	/** Updates the {@code datasetButton} label */
	private void updateDatasetButton() {
		if (pwClosed)
			datasetButton.setLabel("Reopen Closed Plot");
		else
			datasetButton.setLabel("Add Series " + (datasetCounter + 1));
	}

	/** Creates the options menu */
	private JPopupMenu createOptionsMenu() {
		final JPopupMenu popup = new JPopupMenu();
		final JMenuItem changeDataJMI = new JMenuItem("Change Input Data...");
		changeDataJMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (Recorder.record)
					Recorder.setCommand(Recorder.getCommand());
				loadTableAndReloadPrompt(getTable());
			}
		});
		popup.add(changeDataJMI);
		changeTargetPlotWindowJMI = new JMenuItem("Change Output Plot...");
		changeTargetPlotWindowJMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				setDestinationPlot();
			}
		});
		popup.add(changeTargetPlotWindowJMI);
		popup.addSeparator();
		customizePlotJMI = new JMenuItem("Customize Plot...");
		customizePlotJMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				setPlotOptions();
			}
		});
		popup.add(customizePlotJMI);
		popup.addSeparator();
		final JMenuItem preferencesJMI = new JMenuItem("Preferences...");
		preferencesJMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				setPreferences();
			}
		});
		popup.add(preferencesJMI);
		return popup;
	}

}
