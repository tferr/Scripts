//@Integer(label="Channel",min=1,max=10,style="scroll bar",value="1") channel
//@Boolean(label="Plot mean ± SE of plotted ROIs", value=false) plotMean
//@Boolean(label="Average Z-dimension (if present)", value=true) averageZ
//@Boolean(label="Use unique colors", value=false) uniqueColors
//@String(value=" ", visibility="MESSAGE") spacer
//@String(label="Plot only ROIs whose name contains", value="", description="<html>Only ROIs containing this string will be considered.<br>Leave blank to consider all ROIs.") filterROI
//@String(label="ROIs source", choices={"ROI Manager", "Image Overlay"}) source

//@ImagePlus imp
//@UIService uiservice

import ij.IJ;
import ij.gui.Plot;
import ij.measure.Measurements;
import ij.process.ImageStatistics;
import ij.plugin.frame.RoiManager;

import org.scijava.util.Colors;
import org.scijava.ui.awt.AWTColors;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;


def getUniqueColors(n) {
    def rgbColors = Colors.values().toArray()
    def random = new Random()
    def colors = []
    for (i in 0..n) {
        def idx = (i < rgbColors.length) ? i : random.nextInt(rgbColors.length)
        colors.add(AWTColors.getColor(rgbColors[idx]))
    }
    return colors
}

def getROIMeanAcrossTime(roi) {
    def stack = imp.getStack()
    def values = []
    for (frame in 1..frames) {
        def dStats = new DescriptiveStatistics()
        for (slice in zStart..zEnd) {
            def index = imp.getStackIndex(channel, slice, frame)
            def ip = stack.getProcessor(index)
            ip.setRoi(roi)
            def ipStats = ImageStatistics.getStatistics(ip, Measurements.MEAN, cal)
            dStats.addValue(ipStats.mean)
        }
        values.add(dStats.getMean())
    }
    return values
}

def plotAverageOfAllSeries(plot) {
    def rt = plot.getResultsTable()
    if (rt == null)
        return
    def avg = []
    def std = []
    for (row in 0..<rt.getCounter()) {
        def dStats = new DescriptiveStatistics()
        for (col in 1..rt.getLastColumn())
            dStats.addValue(rt.getValueAsDouble(col, row))
        avg.add(dStats.getMean())
        std.add(dStats.getStandardDeviation()/Math.sqrt(dStats.getN()))
    }
    plot.setColor("cyan", "blue")
    plot.addPoints(rt.getColumnAsDoubles(0), avg as double[], Plot.CONNECTED_CIRCLES)
    plot.addErrorBars(std as double[])
}

def error(msg) {
    uiservice.showDialog(msg, "Error")
}

def getROIs(source, filterString) {
    def rois = []
    switch (source) {
        case ~/.*[Oo]verlay$/:
            overlay = imp.getOverlay()
            if (overlay != null && overlay.size > 0)
                rois = overlay.toArray()
            break
        default:
            def rm = RoiManager.getInstance()
            if (rm!=null && rm.getCount() > 0)
                rois = rm.getRoisAsArray()
            break
    }
    if (!filterString.isEmpty()) {
        def excludedROIs = []
        rois.each {
            name = it.getName()
            if (name!= null && !name.contains(filterString))
                excludedROIs.add(it)
        }
        rois = rois - excludedROIs
    }
    return rois
}


def rm = RoiManager.getInstance()
if (rm==null && rm.getCount() == 0) {
    error("The ROI Manager is empty")
    return
}

cal = imp.getCalibration()
frames = imp.getNFrames()
slices = imp.getNSlices()
if (frames==1)
    frames = slices

if (frames == 1) {
    error("Time sequence (ImageStack) required.")
    return
}

zStart = zEnd = imp.getZ()
if (averageZ) {
	zStart = 1
	zEnd = slices
}
xvalues = (1..frames) as float[]

def plot = new Plot("ROI Profiles " + imp.getTitle() + " Ch" + channel, "Time (frame n.)", "Mean")
def rois = rm.getRoisAsArray()
def colors = (uniqueColors) ? getUniqueColors(rm.getCount()) : [].withDefault {AWTColors.getColor(Colors.GRAY)}

rois.eachWithIndex { roi, idx ->
    def name = roi.getName()
    def filter = !filterROI.isEmpty() && name != null
    if (filter && name.contains(filterROI))
        return // continue: exit from the closure
    plot.setColor(colors[idx])
    plot.addPoints(xvalues, getROIMeanAcrossTime(roi) as float[], null, Plot.LINE, name)
}

if (plot.getPlotObjectDesignations().length==0)
    return
if (plotMean)
    plotAverageOfAllSeries(plot)
plot.setLimitsToFit(true)
plot.show()
