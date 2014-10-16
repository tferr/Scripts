/* Apply_Threshold_To_ROI.ijm
 * IJ BAR: https://github.com/tferr/Scripts#scripts
 *
 * IJ1 macro that applies threshold levels to the active area ROI, or the entire image if none
 * exists. Useful when an image can only be segmented in a stepwise manner through application of
 * different threshold values to sub-regions of the image, as in tiled fields of view (see also
 * 'Clear Thresholded Pixels.ijm').
 *
 * Works with grayscale images but in the case of multi-dimensional stacks, threshold is only
 * applied to the Z-dimension. With 8-bit images thresholded values are se to to 255, with
 * 16/32-bit images to the maximum of active ROI.
 * Examples on how to call it from other scripts (see http://fiji.sc/BAR for details):
 *
 *   attr = getDirectory("plugins") +"Scripts"+ File.separator +"BAR"+ File.separator
 *          +"Segmentation"+ File.separator +"Apply_Threshold_To_ROI.ijm";
 *   IJ.runMacroFile(attr, "");          // show dialog prompt
 *   IJ.runMacroFile(attr, "current")    // only active slice is considered
 *   IJ.runMacroFile(attr, "preceding")  // apply threshold up to active slice
 *   IJ.runMacroFile(attr, "subsequent") // apply threshold after active slice
 *
 * TF 2014.10
 */

getThreshold(lower, upper);
if (lower==-1 && upper==-1)
	{ showStatus("No threshold was set!"); wait(300); exit(); }

optns = newArray("current slice only", "all slices", "preceding slices", "subsequent slices");
Stack.getDimensions(null, null, null, depth, null);
Stack.getPosition(channel, currentSlice, frame);
scope = getArgument();
start = 1; end = 1;

if (scope=="" && depth>1) {
	Dialog.create("Apply Threshold To ROI");
	Dialog.addChoice("Apply to:", optns);
	Dialog.show();
	scope = Dialog.getChoice();
} else if (startsWith(scope, "all")) {
	start = 1; end = depth;
} else if (startsWith(scope, "preceding")) {
	start = 1; end = currentSlice;
} else if (startsWith(scope, "subsequent")) {
	start = currentSlice; end = depth;
} else {
	start = currentSlice; end = currentSlice;
}

setBatchMode(true);
setupUndo();
value = getMax();
for (i=start; i<=end; i++) {
	Stack.setPosition(channel, i, frame);
	changeValues(lower, upper, value);
}
Stack.setPosition(channel, currentSlice, frame);
setThreshold(lower, upper);
setBatchMode(false);


function getMax() {
	max = 255;
	if (bitDepth()!=8)
		getStatistics(null, null, null, max);
	return max;
}
