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

import ij.IJ;
import ij.ImagePlus;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;
import ij.process.ImageProcessor;
import java.awt.AWTEvent;
import java.awt.Rectangle;

/**
 * Performs gradient computation using the method proposed by Shen and Castan[1], an alternative to
 * Canny-Deriche filtering.
 *
 * 2014.06, Tiago Ferreira
 *      - Works with 16-bit and 32-bit images and stacks (multithreadable)
 *      - If present, only area ROI is processed
 *      - Added "preview" mode and undo support
 * 2004.07, Maxime Pinchon
 *      - Algorithm implementation[2]. This initial version no longer works with IJ
 *
 * [1] Shen and Castan, CVGIP, 1992, 54 (2) 112-133. http://dx.doi.org/10.1016/1049-9652(92)90060-B
 * [2] http://imagej.nih.gov/ij/plugins/inserm514/Documentation/Shen_Castan_514/Shen_Castan_514.html
 *
 **/
public class ShenCastan implements ExtendedPlugInFilter, DialogListener {

	private ImagePlus imp;
	private boolean canceled;
	private PlugInFilterRunner pfr;
	private final int flags = DOES_ALL-DOES_8C-DOES_RGB|SUPPORTS_MASKING|KEEP_PREVIEW|PARALLELIZE_STACKS;

	private static double f = 0.50d; // smoothing factor
	double[] tmpresX; // x coords of the gradient
	double[] tmpresY; // y coords of the gradient

	/** Returns types of supported images */
	public int setup(final String arg, final ImagePlus imp) {
		this.imp = imp;
		return flags;
	}

	/** Dialog prompt */
	public int showDialog(final ImagePlus imp, final String command, final PlugInFilterRunner pfr) {
		this.pfr = pfr;

		final String msg = "<html>Shen-Castan filtering is an edge detection technique. It is an alternative<p>"
				+ "to other popular approaches such as Canny-Deriche filtering.<p> <p>"
				+ "The Shen-Castan coefficient corresponds to a smooting factor <i>alpha</i>.<p>"
				+ "<i>Alpha</i> can vary between 0 (high smoothing, suitable for noisy images)<p>"
				+ "and 1 (no smoothing, suitable for non-noisy images).";

		final GenericDialog gd = new GenericDialog(command);
		gd.addSlider("Coefficient:", 0.0001d, 1.0001d, f);
		gd.addPreviewCheckbox(pfr);
		gd.addDialogListener(this);
		gd.addHelp(msg);
		gd.showDialog();
		if (gd != null && gd.wasCanceled())
			return DONE;
		if (!dialogItemChanged(gd, null)) // read parameters
			return DONE;
		else
			return IJ.setupDialog(imp, flags);
	}

	/** Read dialog parameters (during preview or after dialog prompt) */
	public boolean dialogItemChanged(final GenericDialog gd, final AWTEvent e) {
		f = gd.getNextNumber();
		if (f < 0) f = 0d;
		if (f > 1) f = 1d;
		canceled = gd.invalidNumber();
		if (gd.wasOKed() && canceled) {
			IJ.error("Value is invalid.");
			return false;
		}
		return true;
	}

	public void setNPasses(final int nPasses) {}

	/** run method */
	public void run(final ImageProcessor ip) {
		if (canceled)
			return;
		ip.setSnapshotCopyMode(true);
		findEdges(ip);
		ip.setSnapshotCopyMode(false);
	}

	/** Method implementing Shen-Castan edge detection */
	public void findEdges(final ImageProcessor ip) {

		final int width = ip.getWidth();
		final int height = ip.getHeight();
		final int slice = pfr.getSliceNumber();
		
		ip.setSliceNumber(slice);

		final int size = width * height;
		final double[] pixels = new double[size];
		final int type = imp.getType();
		switch (type) {
		case ImagePlus.GRAY8:
			final byte[] bsrc = (byte[]) ip.getPixels();
			for (int i = 0; i < size; i++)
				pixels[i] = (double) (bsrc[i] & 0xFF);
			break;
		case ImagePlus.GRAY16:
			final short[] ssrc = (short[]) ip.getPixels();
			for (int i = 0; i < size; i++)
				pixels[i] = (double) (ssrc[i] & 0xFFFF);
			break;
		case ImagePlus.GRAY32:
			final float[] fsrc = (float[]) ip.getPixels();
			for (int i = 0; i < size; i++)
				pixels[i] = (double) fsrc[i];
			break;
		default:
			IJ.error("Not a valid image");
			return;
		}

		double[] row1 = new double[width];
		double[] row2 = new double[width];
		tmpresX = new double[size];
		tmpresY = new double[size];
		final double[] tmpres = new double[size];
		final double a = ((1 - Math.exp(-f))) / (1 + Math.exp(-f));
		//double min = 1e15;
		double max = Double.MIN_VALUE; //-1e15;
		int offset;

		// Shen X
		for (int y = height; y-- > 0;) { // for all the rows

			offset = y * width;
			row1[0] = a * pixels[offset];
			for (int x = width; x-- > 1;)
				row1[width-x] = a * ((pixels[offset+width-x]) - row1[width-x-1]) + row1[width-x-1];

			row2[width-1] = a * row1[width-1];
			for (int x = width; x-- > 1;)
				row2[x-1] = a * ((pixels[offset+x-1]) - row2[x]) + row2[x];

			for (int x = width; x-- > 0;) {
				tmpresX[offset+x] = f * (row2[x] - row1[x]);
				// if (tmpres[offset+x]>max) max = tmpres[offset+x];
				// if (tmpres[offset+x]<min) min = tmpres[offset+x];
			}
		}

		// Shen Y
		row1 = new double[height];
		row2 = new double[height];
		for (int x = width; x-- > 0;) { // for all the columns

			row1[0] = a * (pixels[x] - a * pixels[x]) + a * pixels[x];
			for (int y = height; y-- > 1;)
				row1[height-y] = a * ((pixels[width * (height-y) + x]) - row1[height-y-1]) + row1[height-y-1];

			row2[height-1] = a * row1[height-1];
			for (int y = height; y-- > 1;)
				row2[y-1] = a * ((pixels[width * (y-1) + x]) - row2[y]) + row2[y];

			for (int y = height; y-- > 0;) {
				tmpresY[width*y+x] = f * (row2[y] - row1[y]);
				//if (tmpres[width*y+x]>max) max = tmpres[width*y+x];
				//if (tmpres[width*y+x]<min) min = tmpres[width*y+x];
			}
		}

		// double den = max-min;
		for (int y = height; y-- > 0;) {
			offset = y * width;
			for (int x = width; x-- > 0;)
				if ( (tmpres[offset+x] = Math.sqrt(tmpresX[offset+x] * tmpresX[offset+x] + tmpresY[offset+x] * tmpresY[offset+x]) ) > max)
					max = tmpres[offset+x];// (255*(tmpres[offset+x]-min))/den));
		}

		// Get area ROI, if present
		int rx, ry, rwidth, rheight, index;
		final Rectangle r = ip.getRoi();
		if (r != null) {
			rx = r.x;
			ry = r.y;
			rwidth = r.width;
			rheight = r.height;
		} else {
			rx = 0;
			ry = 0;
			rwidth = width;
			rheight = height;
		}

		final double pMax = ip.getMax();
		switch (type) {
		case ImagePlus.GRAY8:
			final byte[] bres = (byte[]) ip.getPixels();
			for (int y = ry; y < (ry + rheight); y++) {
				for (int x = rx; x < (rx + rwidth); x++) {
					index = y * width + x;
					bres[index] = (byte) ((pMax * tmpres[index]) / max);
				}
			}
			ip.setPixels(bres);
			break;
		case ImagePlus.GRAY16:
			final short[] sres = (short[]) ip.getPixels();
			for (int y = ry; y < (ry + rheight); y++) {
				for (int x = rx; x < (rx + rwidth); x++) {
					index = y * width + x;
					sres[index] = (short) ((pMax * tmpres[index]) / max);
				}
			}
			ip.setPixels(sres);
			break;
		case ImagePlus.GRAY32:
			final float[] fres = (float[]) ip.getPixels();
			for (int y = ry; y < (ry + rheight); y++) {
				for (int x = rx; x < (rx + rwidth); x++) {
					index = y * width + x;
					fres[index] = (float) (tmpres[index]);
				}
			}
			ip.setPixels(fres);
			break;
		}

	}

	/** Returns the horizontal component of the computed gradient */
	public double[] getX() {
		return tmpresX;
	}

	/** Returns the vertical component of the computed gradient */
	public double[] getY() {
		return tmpresY;
	}
}
