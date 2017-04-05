//@ImagePlus imp
//@UIService uiservice

import ij.IJ
import ij.plugin.ContrastEnhancer
import ij.plugin.LutLoader
import ij.plugin.ZProjector
import fiji.plugin.trackmate.gui.GuiUtils
import org.apache.commons.io.FilenameUtils


def isTimeSeq(imp) {
	def overlay = imp.getOverlay()
	def roi = imp.getRoi()
	GuiUtils.userCheckImpDimensions(imp)
	if (overlay)
		imp.setOverlay(overlay)
	if (roi)
		imp.setRoi(roi)
	imp.getNFrames() > 1
}

def isTimeSeq(imp, uiservice) {
	def validImp = isTimeSeq(imp)
	if (!validImp)
		uiservice.showDialog("Time sequence (ImageStack) required.",
			"Invalid input image")
	validImp
}

def isSingleChannelTimeseq(imp, uiservice) {
	def validImp = (imp.getNDimensions() > 3) ? false : isTimeSeq(imp)
	if (!validImp)
		uiservice.showDialog("Single channel XY time sequence required.",
			"Invalid input image")
	validImp
}

def displayWithViridis(imp) {
	def viridisPath = IJ.getDir("luts") + "mpl-viridis.lut"
	if (new File(viridisPath).exists())
		imp.setLut(LutLoader.openLut(viridisPath))
	def ce = new ContrastEnhancer()
	ce.stretchHistogram(imp, 0.5)
	imp.updateAndDraw()
	imp.show()
}

def projAVG(imp, start, end) {
	proj(imp, ZProjector.AVG_METHOD, start, end)
}

def projSD(imp, start, end) {
	proj(imp, ZProjector.SD_METHOD, start, end)
}

def proj(imp, method, start, end) {
	def zp = new ZProjector(imp)
	zp.setMethod(method)
	zp.setStartSlice(start)
	zp.setStopSlice(end)
	zp.doProjection()
	return zp.getProjection()
}

def splitFileExtension(filename) {
	def fname = FilenameUtils.removeExtension(filename)
	def extension = FilenameUtils.getExtension(filename)
	return [fname, extension]
}
