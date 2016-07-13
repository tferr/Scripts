# @Dataset dataset
# @ImagePlus imp
# @LogService logsvc
# @Double(label="Ch1 detector radius",value=3.750) radius_ch1
# @Double(label="Ch1 detector threshold",value=2.500) threshold_ch1
# @ColorRGB(label="Ch1 counter color",value="magenta") color_ch1
# @Double(label="Ch2 detector radius",value=0.6505) radius_ch2
# @Double(label="Ch2 detector threshold",value=150) threshold_ch2
# @ColorRGB(label="Ch2 counter color",value="yellow") color_ch2
# @Boolean(label="Run silently",value=false) silent

# LoG_Spot_Counter.py
# https://github.com/tferr/Scripts/
# Python script that uses trackmate to count particles in a multichannel image
# TF 201607

from fiji.plugin.trackmate import Model, Logger, Settings, TrackMate
from fiji.plugin.trackmate.detection import LogDetectorFactory
from ij.gui import Overlay, PointRoi
from ij.measure import Calibration, ResultsTable as RT
from java.awt import Color


def ColorRGBtoColor(color):
    """Converts a org.scijava.util.ColorRGB into a java.awt.Color"""
    return Color(color.getRed(), color.getGreen(), color.getBlue())

def extractCounts(trackmate, ch_id, roi_type = "large"):
    """Adds spots to the image Overlay and counts to the ResultsTable.
       Returns the total number of spots
    """
    global silent, overlay, table
    model = trackmate.model
    if silent:
        model.setLogger(Logger.VOID_LOGGER)
    spots = model.getSpots()
    count = spots.getNSpots(False)
    if count > 0:
        logger("Rendering overlay")
        roi = spotCollectionToROI(spots, False)
        if "large" in roi_type:
            roi.setStrokeColor(ColorRGBtoColor(color_ch1))
            roi.setPointType(3)
            roi.setSize(4)
        else:
            roi.setStrokeColor(ColorRGBtoColor(color_ch2))
            roi.setPointType(2)
            roi.setSize(1)
        overlay.add(roi, ch_id)
    table.addValue("# " + ch_id, count)
    return count

def logger(message, isError = False, exit = False):
    """Logs message/error when in 'debug' mode aborting script if requested"""
    global silent, logsvc
    if not silent:
        logsvc.error(message) if isError else logsvc.info(message)
    if exit:
        if silent: # stack trace ommited
            from ij import Macro
            from java.lang import RuntimeException
            raise RuntimeException(Macro.MACRO_CANCELED)
        elif exit:
            import sys
            sys.exit(message)

def projectionImage(imp):
    """Returns the MIP of the specified ImagePlus (composite stack)"""
    from ij.plugin import ZProjector
    zp = ZProjector(imp)
    zp.setMethod(ZProjector.MAX_METHOD)
    zp.setStartSlice(1);
    zp.setStopSlice(imp.getNSlices());
    zp.doHyperStackProjection(True)
    mip_imp = zp.getProjection()
    mip_imp.setCalibration(imp.getCalibration())
    return mip_imp

def spotCollectionToROI(spotCollection, visibleSpotsOnly):
    """Converts a trackmate.SpotCollection to a MultiPoint ROI"""
    global imp
    roi = None
    cal = imp.getCalibration()
    for spot in spotCollection.iterable(visibleSpotsOnly):
        x = cal.getRawX(spot.getFeature('POSITION_X'))
        y = cal.getRawY(spot.getFeature('POSITION_Y'))
        if roi is None:
            roi = PointRoi(x, y)
        else:
            roi.addPoint(x, y)
    return roi

def validDataset(dataset):
    """Assess if dataset is a multichannel 2D/3D image)"""
    from net.imagej.axis import Axes
    c = dataset.dimension(dataset.dimensionIndex(Axes.CHANNEL))
    t = dataset.dimension(dataset.dimensionIndex(Axes.TIME))
    return c > 1 and t <= 1

#Validate image. Ensure it has no ROIs. Project it as needed
if not validDataset(dataset):
    logger("Invalid Dataset: Dimensions are not suitable", True, True)
else:
    logger("Analyzing " + imp.getTitle())
if (imp.getNSlices()>1):
    logger("Retrieving MIP")
    imp = projectionImage(imp)
    imp.show();
imp.killRoi()

# Initalize Settings and Overlay
settings = Settings()
settings.setFrom(imp)
overlay = Overlay()

# Prepare Results table
table = RT.getResultsTable()
table.incrementCounter()
table.addValue("Label", imp.getTitle())

# Initialize counts
spots_ch1 = 0
spots_ch2 = 0

# Ch1 detection. NB: GUI accepts diameter not radius
logger("Processing Ch1...")
settings.detectorFactory = LogDetectorFactory()
settings.detectorSettings = {
    'DO_SUBPIXEL_LOCALIZATION' : False,
    'RADIUS' : radius_ch1,
    'TARGET_CHANNEL' : 1,
    'THRESHOLD' : threshold_ch1,
    'DO_MEDIAN_FILTERING' : True,
}
trackmate = TrackMate(settings)
if trackmate.execDetection():
    spots_ch1 = extractCounts(trackmate, "Nuclei", "large")
else:
    logger(str(trackmate.getErrorMessage()), True)

# Ch2 detection. NB: GUI accepts diameter not radius
logger("Processing Ch2...")
settings = trackmate.getSettings()
settings.detectorSettings = {
    'DO_SUBPIXEL_LOCALIZATION' : False,
    'RADIUS' : radius_ch2,
    'TARGET_CHANNEL' : 2,
    'THRESHOLD' : threshold_ch2,
    'DO_MEDIAN_FILTERING' : True,
}
if trackmate.execDetection():
    spots_ch2 = extractCounts(trackmate, "PLA", "small")
else:
    logger(str(trackmate.getErrorMessage()), True)

# Get ratios
table.addValue("Ratio", float('nan') if spots_ch1==0 else spots_ch2/float(spots_ch1))

# Display results
imp.setOverlay(overlay)
table.show("Results")
logger("Concluded analysis")
