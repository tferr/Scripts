/* Threshold_From_Background.ijm
 * IJ BAR: https://github.com/tferr/Scripts#scripts
 *
 * Sets the threshold as the ROI average plus a factor of its standard deviation. To call it from
 * other scripts (see http://fiji.sc/BAR for details):
 *
 *   tfb = getDirectory("plugins") +"Scripts"+ File.separator +"BAR"+ File.separator
 *         +"Segmentation"+ File.separator +"Threshold_From_Background.ijm";
 *   runMacro(tfb, factor);
 *
 * TF 2014.06
 */

if (selectionType==-1)
    exit("No background ROI selected");

factor = getArgument();
if (factor=="")
    factor = getNumber("Multiplier (Mean + ??*SDeviation):", 3);
getStatistics(area, mean, min, max, std);
setThreshold(mean+factor*std, pow(2, bitDepth));
