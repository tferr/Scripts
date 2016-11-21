# @Dataset dataset
# @ImagePlus imp
# @LogService logsvc
# @String(label="Ch1 Detector",description="Detection algorithm",choices={"LoG", "DoG"}, value="LoG") detector_ch1
# @Double(label="Ch1 Estimated spot size",description="Estimated diameter in physical units",value=7.200,min=0.001) diameter_ch1
# @Double(label="Ch1 Quality cutoff",description="Spots with lower quality than this are ignored",value=3.500) threshold_ch1
# @ColorRGB(label="Ch1 Marker color",value="magenta") color_ch1
# @String(label="Ch2 Detector",description="Detection algorithm",choices={"LoG", "DoG"}, value="DoG") detector_ch2
# @Double(label="Ch2 Estimated spot size ",description="Estimated diameter in physical units",value=1.080,min=0.001) diameter_ch2
# @Double(label="Ch2 Quality cutoff",description="Spots with lower quality than this are ignored' than this",value=70.50) threshold_ch2
# @ColorRGB(label="Ch2 Marker color",value="yellow") color_ch2
# @String(label="Group",description="Used to group data in Results table", value="Control image") group
# @Boolean(label="Run silently",description="Disable debug mode?",value=true) silent

'''
LoG-DoG_Spot_Counter.py
https://github.com/tferr/Scripts/

Detects particles in a multichannel image using TrackMate[1]'s LoG/DoG (Laplacian/
Difference of Gaussian) segmentation[2,3]. Detected centroids are displayed in
the non-destructive image overlay and total counts reported in the Results table.
The script was written for counting PLA (Proximity ligation Assay) foci in tissue
counterstained for DAPI and WGA, but can be applied to similar images. It also
exemplifies how to script TrackMate[4].

Tips:
 - Toggling Color mode ("Image>Color>Channels Tools...") allows you to display
   only the spots detected for the active channel
 - The "Group" field can be used to generate grouped box plots of the data using
   BAR>Data Analysis>Create Boxplot

TF 201607
[1] http://imagej.net/TrackMate
[2] http://imagej.net/Getting_started_with_TrackMate
[3] http://imagej.net/TrackMate_Algorithms#Spot_detectors
[4] http://imagej.net/Scripting_TrackMate
'''

from fiji.plugin.trackmate import Model, Logger, Settings, TrackMate
from fiji.plugin.trackmate.detection import DetectorKeys as DK, LogDetectorFactory, DogDetectorFactory
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
        from ij import IJ, Macro
        IJ.showMessage(message)
        raise RuntimeError(Macro.MACRO_CANCELED if silent else message)

def projectionImage(imp):
    """Returns the MIP of the specified ImagePlus (a composite stack)"""
    from ij.plugin import ZProjector
    roi_exists = imp.getRoi() is not None
    imp.deleteRoi()
    zp = ZProjector(imp)
    zp.setMethod(ZProjector.MAX_METHOD)
    zp.setStartSlice(1)
    zp.setStopSlice(imp.getNSlices())
    zp.doHyperStackProjection(True)
    mip_imp = zp.getProjection()
    mip_imp.setCalibration(imp.getCalibration())
    if roi_exists:
        mip_imp.restoreRoi()
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
table.setLabel(imp.getTitle(), table.getCounter()-1)

# Initialize counts
spots_ch1 = 0
spots_ch2 = 0

# Ch1 detection. NB: trackmate GUI accepts diameter not radius
logger("Processing Ch1...")
settings.detectorFactory = LogDetectorFactory() if "LoG" in detector_ch1 else DogDetectorFactory()
setDetectorSettings(settings, CHANNEL_1, diameter_ch1/2, threshold_ch1)
trackmate = TrackMate(settings)
if trackmate.execDetection():
    spots_ch1 = extractCounts(trackmate.model, CHANNEL_1)
else:
    logger(str(trackmate.getErrorMessage()), True)

# Ch2 detection. NB: trackmate GUI accepts diameter not radius
logger("Processing Ch2...")
settings = trackmate.getSettings()
settings.detectorFactory = LogDetectorFactory() if "LoG" in detector_ch2 else DogDetectorFactory()
setDetectorSettings(settings, CHANNEL_2, diameter_ch2/2, threshold_ch2)
if trackmate.execDetection():
    spots_ch2 = extractCounts(trackmate.model, CHANNEL_2, "small")
else:
    logger(str(trackmate.getErrorMessage()), True)

# Log ratios and remaining details
table.addValue("Ratio", float('nan') if spots_ch1==0 else spots_ch2/float(spots_ch1))
table.addValue("Group", group)

# Display results
imp.setOverlay(overlay)
table.show("Results")
logger("Concluded analysis")
