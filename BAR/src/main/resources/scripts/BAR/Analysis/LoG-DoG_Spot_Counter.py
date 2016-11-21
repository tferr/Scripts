# @Integer(label="First channel (Ch1)", description="Target channel of first detector",value="1") channel_1
# @String(label="Ch1 Detector", description="Detection algorithm", choices={"LoG", "DoG"}, style="radioButtonHorizontal") detector_ch1
# @Double(label="Ch1 Estimated spot size",description="Estimated diameter in physical units",min=0.001,max=100,style="scroll bar",value=7.200) diameter_ch1
# @Double(label="Ch1 Quality cutoff",description="Spots with lower quality than this are ignored' than this",min=1,max=100,style="scroll bar",value=3.5) threshold_ch1
# @ColorRGB(label="Ch1 Marker color",value="magenta") color_ch1

# @String(value=" ", visibility="MESSAGE") spacer
# @Integer(label="Second channel (Ch2)", description="Target channel of second detector, if present",value="2") channel_2
# @String(label="Ch2 Detector", description="Detection algorithm", choices={"LoG", "DoG"}, style="radioButtonHorizontal") detector_ch2
# @Double(label="Ch2 Estimated spot size",description="Estimated diameter in physical units",min=0.001,max=1000,style="scroll bar",value=1.080) diameter_ch2
# @Double(label="Ch2 Quality cutoff",description="Spots with lower quality than this are ignored' than this",min=1,max=100,style="scroll bar",value=70.5) threshold_ch2
# @ColorRGB(label="Ch2 Marker color",value="yellow") color_ch2

# @String(value=" ", visibility="MESSAGE") spacer
# @String(label="Image group",description="Used to group data in Results table", value="Control image") group
# @Boolean(label="3D stacks: Analyze projection", value=false) project_image
# @Boolean(label="Display console log", value=false) open_console

# @ImagePlus image
# @LogService lservice
# @UIService uiservice

'''
LoG-DoG_Spot_Counter.py
https://github.com/tferr/Scripts/

Detects particles in a multichannel image using TrackMate LoG/DoG (Laplacian/
Difference of Gaussian) segmentation[1,2]. Detected centroids are displayed in
the non-destructive image overlay and total counts shown in the Results table.
The script was written for counting PLA (Proximity ligation Assay) foci in
tissue counterstained for DAPI and WGA, but can be applied to similar images.
It also exemplifies how to script TrackMate[3].

NB:
 - If an area ROI exists, it will be used to confine detection
 - Toggling Color mode ('Image>Color>Channels Tools...') allows you to display
   only the spots detected for the active channel
 - The 'Group' field can be used to generate box plots of the data using 'BAR>
   Data Analysis>Create Boxplot'

TF 201611
[1] http://imagej.net/TrackMate
[2] http://imagej.net/TrackMate_Algorithms#Spot_detectors
[3] http://imagej.net/Scripting_TrackMate
'''

from fiji.plugin.trackmate import Model, Logger, Settings, TrackMate
from fiji.plugin.trackmate.detection import DetectorKeys as DK, \
    LogDetectorFactory, DogDetectorFactory
from org.scijava.util import ColorRGB
from java.awt import Color
from ij import ImagePlus
from ij.gui import Overlay, PointRoi
from ij.measure import Calibration, ResultsTable


def colorRGBtoColor(colorRGB):
    """Converts a org.scijava.util.ColorRGB into a java.awt.Color"""
    return Color(colorRGB.getRed(), colorRGB.getGreen(), colorRGB.getBlue())


def error(msg):
    """ Displays an error message """
    uiservice.showDialog(msg, "Error")


def getOverlay(imp):
    """ Returns an image overlay cleansed of spot ROIs from previous runs """
    overlay = imp.getOverlay()
    if overlay is None:
        return Overlay()
    for i in range(0, overlay.size()-1):
        roi_name = overlay.get(i).getName()
        if roi_name is not None and "Spots" in roi_name:
            overlay.remove(i)
    return overlay


def getSpots(imp, channel, detector_type, radius, threshold, overlay,
             roi_type="large", roi_color=ColorRGB("blue")):
    """ Performs the detection, adding spots to the image overlay
    :imp:           The image (ImagePlus) being analyzed
    :channel:       The target channel
    :detector_type: A string describing the detector: "LoG" or "DoG"
    :radius:        Spot radius (NB: trackmate GUI accepts diameter)
    :threshold:     Quality cutoff value
    :overlay:       The image overlay to store spot (MultiPoint) ROIs
    :roi_type:      A string describing how spot ROIs should be displayed
    :returns:       The n. of detected spots
    """
    settings = Settings()
    settings.setFrom(imp)
    settings.detectorFactory = (LogDetectorFactory() if "LoG" in detector_type
                                else DogDetectorFactory())
    settings.detectorSettings = {
        DK.KEY_DO_SUBPIXEL_LOCALIZATION: False,
        DK.KEY_DO_MEDIAN_FILTERING: True,
        DK.KEY_TARGET_CHANNEL: channel,
        DK.KEY_RADIUS: radius,
        DK.KEY_THRESHOLD: threshold,
    }
    trackmate = TrackMate(settings)
    if not trackmate.execDetection():
        lservice.error(str(trackmate.getErrorMessage()))
        return 0
    model = trackmate.model
    spots = model.getSpots()
    count = spots.getNSpots(False)
    ch_id = "Spots Ch%d" % channel
    if count > 0:
        roi = None
        cal = imp.getCalibration()
        t_pos = imp.getT()
        if (t_pos > 1):
            lservice.warn("Only frame %d was considered..." % t_pos)
        for spot in spots.iterable(False):
            x = cal.getRawX(spot.getFeature(spot.POSITION_X))
            y = cal.getRawY(spot.getFeature(spot.POSITION_Y))
            z = spot.getFeature(spot.POSITION_Z)
            if z == 0 or not cal.pixelDepth or cal.pixelDepth == 0:
                z = 1
            else:
                z = int(z // cal.pixelDepth)
            imp.setPosition(channel, z, t_pos)
            if roi is None:
                roi = PointRoi(int(x), int(y), imp)
            else:
                roi.addPoint(imp, x, y)
        roi.setStrokeColor(colorRGBtoColor(roi_color))
        if "large" in roi_type:
            roi.setPointType(3)
            roi.setSize(4)
        else:
            roi.setPointType(2)
            roi.setSize(1)
        overlay.add(roi, ch_id)
    return count


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


def main():


    n_channels = image.getNChannels()
    if channel_1  > n_channels and channel_2  > n_channels:
        error("Image does not contain specified channel(s)")
        return

    if open_console:
        uiservice.getDefaultUI().getConsolePane().show()
    lservice.info("Analyzing " + image.getTitle())

    # 2D / 3D analysis?
    if project_image and image.getNSlices() > 1:
        lservice.info("Retrieving MIP")
        image = projectionImage(image)
        image.show()

    # Prepare overlay and Results table
    overlay = getOverlay(image)
    table = ResultsTable.getResultsTable()
    table.incrementCounter()
    table.setLabel(image.getTitle(), table.getCounter()-1)

    # Perform detection
    spots_ch1 = spots_ch2 = float('nan')
    if channel_1 <= n_channels:
        lservice.info("Processing Ch%d" % channel_1)
        spots_ch1 = getSpots(image, channel_1, detector_ch1, diameter_ch1/2,
                             threshold_ch1, overlay, "large", color_ch1)

    if channel_2 <= n_channels:
        lservice.info("Processing Ch%d" % channel_2)
        spots_ch2 = getSpots(image, channel_2, detector_ch2, diameter_ch2/2,
                             threshold_ch2, overlay, "small", color_ch2)

    # Show results
    lservice.info("Displaying spot ROIs and results...")
    image.setOverlay(overlay)
    table.addValue("# " + "Spots Ch%d" % channel_1, spots_ch1)
    table.addValue("# " + "Spots Ch%d" % channel_2, spots_ch2)
    table.addValue("Ratio Ch%d/Ch%d" % (channel_1, channel_2),
            (float('nan') if spots_ch1 == 0 else spots_ch2 / float(spots_ch1)))
    table.addValue("Group", group)
    table.show("Results")
    lservice.info("Analysis concluded")


if __name__ == '__main__':
    main()
