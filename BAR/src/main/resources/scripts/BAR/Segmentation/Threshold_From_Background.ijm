/* Threshold_From_Background.ijm
 * IJ BAR: https://github.com/tferr/Scripts
 *
 * Sets the threshold as the ROI average plus a factor of its standard deviation.
 * Python example on how to call it from other scripts (see http://imagej.net/BAR for details):
 *
 * #@Context context
 * from bar import Runner
 * runner = Runner(context)
 * arg = "3"
 * runner.runBARMacro("Segmentation/Threshold_From_Background.ijm", arg)
 * print("Macro exited: %s " % runner.scriptLoaded())
 */

if (selectionType==-1)
    exit("A background-defining ROI is\nrequired but none was found.");

factor = getArgument();
if (factor=="")
    factor = getNumber("Multiplier (Mean + ??*SDeviation):", 3);
getStatistics(area, mean, min, max, std);
setThreshold(mean+factor*std, pow(2, bitDepth));
