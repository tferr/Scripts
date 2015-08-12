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
package bar;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.jfree.chart.JFreeChart;
import org.w3c.dom.DOMImplementation;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.DefaultFontMapper;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

import ij.IJ;
import ij.io.SaveDialog;

/**
 * Provides utility methods that improve ImageJ's plotting capabilities. Most
 * methods require third-party libraries bundled with Fiji, including
 * <a href= "http://javadoc.imagej.net/JFreeChart/" target="_blank">JFreeChart
 * </a>, <a href= "http://itextpdf.com" target="_blank">iText </a> and
 * <a href= "http://xmlgraphics.apache.org/batik/" target="_blank">Batik</a>.
 */
public class PlotUtils {

	/**
	 * Exports a JFreeChart to a SVG file using the
	 * <a href= "http://xmlgraphics.apache.org/batik/" target="_blank">Batik SVG
	 * Toolkit</a>, bundled with Fiji. This method is taken from
	 * {@linkplain http://dolf.trieschnigg.nl/jfreechart/}
	 *
	 * @param chart
	 *            the <a href= "http://javadoc.imagej.net/JFreeChart/" target=
	 *            "_blank">JFreeChart </a> to export.
	 * @param bounds
	 *            the Rectangle delimiting the boundaries within which the chart
	 *            should be drawn.
	 * @param file
	 *            the output (destination) file.
	 * @throws IOException
	 *             if writing to output file fails.
	 * @see #exportChartAsSVG(JFreeChart, Rectangle)
	 * @see #exportChartAsPDF(JFreeChart, Rectangle)
	 * @see #exportChartAsPDF(JFreeChart, Rectangle, File)
	 */
	public static void exportChartAsSVG(final JFreeChart chart, final Rectangle bounds, final File file)
			throws IOException {

		// Get a DOMImplementation and create an XML document
		final DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
		final org.w3c.dom.Document document = domImpl.createDocument(null, "svg", null);

		// Create an instance of the SVG Generator
		final SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

		// draw the chart in the SVG generator
		chart.draw(svgGenerator, bounds);

		// Write svg file
		final OutputStream outputStream = new FileOutputStream(file);
		final Writer out = new OutputStreamWriter(outputStream, "UTF-8");
		svgGenerator.stream(out, true /* use css */);
		outputStream.flush();
		outputStream.close();
	}

	/**
	 * Exports the specified JFreeChart to a SVG file using the
	 * <a href="http://xmlgraphics.apache.org/batik/" target="_blank">Batik SVG
	 * Toolkit</a>, , assumed to be installed as third party-library in the
	 * ImageJ {@code jars} directory (Batik is bundled with Fiji). The
	 * destination file is specified by the user in a save dialog prompt. An
	 * error message is displayed if the file could not be saved. Does nothing
	 * if {@code chart} is {@code null}.
	 *
	 * @param chart
	 *            the <a href="http://javadoc.imagej.net/JFreeChart/" target=
	 *            "_blank">JFreeChart </a> to export.
	 * @param bounds
	 *            the Rectangle delimiting the boundaries within which the chart
	 *            should be drawn.
	 * @see #exportChartAsSVG(JFreeChart, Rectangle, File)
	 * @see #exportChartAsPDF(JFreeChart, Rectangle)
	 * @see #exportChartAsPDF(JFreeChart, Rectangle, File)
	 */
	public static void exportChartAsSVG(final JFreeChart chart, final Rectangle bounds) {
		exportChart(chart, bounds, ".svg");
	}

	/**
	 * Exports the specified JFreeChart to a PDF file using
	 * <a href="http://itextpdf.com" target="_blank">iText</a>, bundled with
	 * Fiji. The destination file is specified by the user in a save dialog
	 * prompt. An error message is displayed if the file could not be saved.
	 * Does nothing if {@code chart} is {@code null}.
	 *
	 * @param chart
	 *            the <a href="http://javadoc.imagej.net/JFreeChart/" target=
	 *            "_blank">JFreeChart </a> to export.
	 * @param bounds
	 *            the Rectangle delimiting the boundaries within which the chart
	 *            should be drawn.
	 * @see #exportChartAsPDF(JFreeChart, Rectangle)
	 * @see #exportChartAsSVG(JFreeChart, Rectangle)
	 * @see #exportChartAsSVG(JFreeChart, Rectangle, File)
	 */
	public static void exportChartAsPDF(final JFreeChart chart, final Rectangle bounds, final File f)
			throws FileNotFoundException, DocumentException {

		final int margin = 0; // page margins

		// Initialize writer
		final Document document = new Document(new com.itextpdf.text.Rectangle(bounds.width, bounds.height), margin,
				margin, margin, margin);
		final PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(f));

		document.open();
		final PdfContentByte cb = writer.getDirectContent();
		final PdfTemplate tp = cb.createTemplate(bounds.width, bounds.height);

		// Draw the chart. Release resources upon completion
		final Graphics2D g2 = tp.createGraphics(bounds.width, bounds.height, new DefaultFontMapper());
		chart.draw(g2, bounds);
		g2.dispose();

		// Write to file
		cb.addTemplate(tp, 0, 0);

		document.close();
	}

	/**
	 * Exports the specified JFreeChart to a PDF file using
	 * <a href="http://itextpdf.com" target="_blank">iText</a>, assumed to be
	 * installed as third party-library in the ImageJ {@code jars} directory
	 * (iText is bundled with Fiji). The destination file is specified by the
	 * user in a save dialog prompt. An error message is displayed if the file
	 * could not be saved. Does nothing if {@code chart} is {@code null}.
	 *
	 * @param chart
	 *            the <a href="http://javadoc.imagej.net/JFreeChart/" target=
	 *            "_blank">JFreeChart </a> to export.
	 * @param bounds
	 *            the Rectangle delimiting the boundaries within which the chart
	 *            should be drawn.
	 * @see #exportChartAsPDF(JFreeChart, Rectangle, File)
	 * @see #exportChartAsSVG(JFreeChart, Rectangle)
	 * @see #exportChartAsSVG(JFreeChart, Rectangle, File)
	 */
	public static void exportChartAsPDF(final JFreeChart chart, final Rectangle bounds) {
		exportChart(chart, bounds, ".pdf");
	}

	/**
	 * Exports the specified JFreeChart to a SVG or PDF file. Destination file
	 * is specified by the user in a save dialog prompt. An error message is
	 * displayed if the file could not be saved. Does nothing if {@code chart}
	 * is {@code null}.
	 *
	 * @param chart
	 *            the <a href="http://javadoc.imagej.net/JFreeChart/" target=
	 *            "_blank">JFreeChart </a> to export.
	 * @param bounds
	 *            the Rectangle delimiting the boundaries within which the chart
	 *            should be drawn.
	 * @param extension
	 *            The file extension. Either ".svg" or ".pdf"
	 * @see #exportChartAsSVG(JFreeChart, Rectangle)
	 * @see #exportChartAsPDF(JFreeChart, Rectangle)
	 */
	static void exportChart(final JFreeChart chart, final Rectangle bounds, final String extension) {
		if (chart == null)
			return;
		final String defaultName = (chart.getTitle() == null) ? "Chart" : chart.getTitle().getText();
		final SaveDialog sd = new SaveDialog("Export graph as...", defaultName, extension);
		if (sd.getFileName() == null)
			return;
		final File saveFile = new File(sd.getDirectory(), sd.getFileName());
		if ((saveFile != null) && saveFile.exists()) {
			if (!IJ.showMessageWithCancel("Export graph...",
					saveFile.getAbsolutePath() + " already exists.\nReplace it?"))
				return;
		}
		try {
			if (extension.toLowerCase().endsWith(".svg"))
				exportChartAsSVG(chart, bounds, saveFile);
			else if (extension.toLowerCase().endsWith(".pdf"))
				exportChartAsPDF(chart, bounds, saveFile);
			IJ.showStatus("Graph saved, " + saveFile.getAbsolutePath());
		} catch (final Exception e) {
			IJ.error("Error", "Saving to " + saveFile.getAbsolutePath() + " failed");
			if (IJ.debugMode)
				IJ.handleException(e);
			return;
		}
	}

}
