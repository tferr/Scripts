/* Threshold_From_Background.ijm
 * IJ BAR: https://github.com/tferr/Scripts#scripts
 *
 * Sets the threshold as the ROI average plus a factor of its standard deviation. To call it from
 * other scripts (see http://imagej.net/BAR for details):
 *
 *   tfbpath = call("bar.Utils.getSegmentationDir")+"Threshold_From_Background.ijm";
 *   runMacro(tfbpath, 3);
 *
 * TF 2014.06
 */

if (selectionType==-1)
    exit("A background-defining ROI is\nrequired but none was found.");

factor = getArgument();
if (factor=="")
    factor = getNumber("Multiplier (Mean + ??*SDeviation):", 3);
getStatistics(area, mean, min, max, std);
setThreshold(mean+factor*std, pow(2, bitDepth));
