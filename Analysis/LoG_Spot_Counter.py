# @Dataset dataset
# @ImagePlus imp
# @LogService logsvc
# @Double(label="Ch1 detector radius",description="Estimated spot radius in physical units",value=3.750,min=0.001) radius_ch1
# @Double(label="Ch1 detector threshold",description="Ignore spots with lower 'quality' than this",value=2.500) threshold_ch1
# @ColorRGB(label="Ch1 counter color",value="magenta") color_ch1
# @Double(label="Ch2 detector radius",description="Estimated spot radius in physical units",value=0.6505,min=0,001) radius_ch2
# @Double(label="Ch2 detector threshold",description="Ignore spots with lower 'quality' than this",value=150) threshold_ch2
# @ColorRGB(label="Ch2 counter color",value="yellow") color_ch2
# @String(label="Group",description="Used to group data in Results table", value="Control image") comment
# @Boolean(label="Run silently",description="Disable debug mode?",value=true) silent

'''
LoG_Spot_Counter.py
https://github.com/tferr/Scripts/

Counts particles in a multichannel image using TrackMate[1]'s LoG (Laplacian of
Gaussian) segmentation, optimized for particles between ~5 and ~20 pixels in
diameter[2] (For more details: http://imagej.net/Scripting_TrackMate).

TF 201607
[1] http://imagej.net/TrackMate
[2] http://imagej.net/Getting_started_with_TrackMate
'''

from fiji.plugin.trackmate import Model, Logger, Settings, TrackMate
from fiji.plugin.trackmate.detection import DetectorKeys as DK, LogDetectorFactory
from ij.gui import Overlay, PointRoi
from ij.measure import Calibration, ResultsTable as RT
from java.awt import Color


CHANNEL_1 = 1 # Target channel for 1st spot detector
CHANNEL_2 = 2 # Target channel for 2nd spot detector


def ColorRGBtoColor(color):
    """Converts a org.scijava.util.ColorRGB into a java.awt.Color"""
    return Color(color.getRed(), color.getGreen(), color.getBlue())

def extractCounts(model, ch, roi_type = "large"):
    """
    Adds detected spots to the image overlay and logs counts to Results table
    :model:     The trackmate.Model
    :ch:        The target channel
    :roi_type:  A string describing how spot ROIs should be displayed
    :return:    The n. of spots detected by trackmate.detection.LogDetector
    """
    if silent: # global variable
        model.setLogger(Logger.VOID_LOGGER)
    spots = model.getSpots()
    count = spots.getNSpots(False)
    ch_id = "Spots Ch" + str(ch)
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
        roi.setPosition(ch)
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
            from ij import IJ, Macro
            from java.lang import RuntimeException
            IJ.showMessage(message)
            raise RuntimeException(Macro.MACRO_CANCELED)
        elif exit:
            import sys
            sys.exit(message)

def projectionImage(imp):
    """Returns the MIP of the specified ImagePlus (a composite stack)"""
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
    """Converts a trackmate.SpotCollection to a ij.gui.PointRoi"""
    global imp
    roi = None
    cal = imp.getCalibration()
    for spot in spotCollection.iterable(visibleSpotsOnly):
        x = cal.getRawX(spot.getFeature(spot.POSITION_X))
        y = cal.getRawY(spot.getFeature(spot.POSITION_Y))
        if roi is None:
            roi = PointRoi(x, y)
        else:
            roi.addPoint(x, y)
    return roi

def setDetectorSettings(settings, channel, radius, threshold):
    """Settings map for fiji.plugin.trackmate.detection.LogDetector"""
    settings.detectorSettings = {
        DK.KEY_DO_SUBPIXEL_LOCALIZATION : False,
        DK.KEY_DO_MEDIAN_FILTERING : True,
        DK.KEY_TARGET_CHANNEL : channel,
        DK.KEY_RADIUS : radius,
        DK.KEY_THRESHOLD : threshold,
    }

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

# Ch1 detection. NB: trackmate GUI accepts diameter not radius
logger("Processing Ch1...")
settings.detectorFactory = LogDetectorFactory()
setDetectorSettings(settings, CHANNEL_1, radius_ch1, threshold_ch1)
trackmate = TrackMate(settings)
if trackmate.execDetection():
    spots_ch1 = extractCounts(trackmate.model, CHANNEL_1)
else:
    logger(str(trackmate.getErrorMessage()), True)

# Ch2 detection. NB: trackmate GUI accepts diameter not radius
logger("Processing Ch2...")
settings = trackmate.getSettings()
setDetectorSettings(settings, CHANNEL_2, radius_ch2, threshold_ch2)
if trackmate.execDetection():
    spots_ch2 = extractCounts(trackmate.model, CHANNEL_2, "small")
else:
    logger(str(trackmate.getErrorMessage()), True)

# Log ratios and remaining details
table.addValue("Ratio", float('nan') if spots_ch1==0 else spots_ch2/float(spots_ch1))
table.addValue("Comment", "" if not comment else comment)

# Display results
imp.setOverlay(overlay)
table.show("Results")
logger("Concluded analysis")
