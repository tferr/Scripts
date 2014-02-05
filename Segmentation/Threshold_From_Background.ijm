/* Threshold_From_Background.ijm
 * https://github.com/tferr/Scripts#scripts
 * Sets the threshold as the ROI average plus a factor of its standard deviation
 * To call it from other macro: runMacro(pathToThisFile, factor);
 */

if (selectionType==-1)
    exit("No background ROI selected");

factor = getArgument();
if (factor=="")
    factor = getNumber("Multiplier (Mean + ??*SDeviation):", 3);
getStatistics(area, mean, min, max, std);
setThreshold(mean+factor*std, pow(2, bitDepth));
