/* Apply_Threshold_To_ROI.ijm
 * IJ BAR: https://github.com/tferr/Scripts
 *
 * IJ1 macro that applies threshold levels to the active area ROI, or the entire image if none
 * exists. Useful when an image can only be segmented in a stepwise manner through application of
 * different threshold values to sub-regions of the image, as in tiled fields of view (see also
 * 'Clear Thresholded Pixels.ijm').
 *
 * Works with grayscale images but in the case of multi-dimensional stacks, threshold is only
 * applied to the Z-dimension of the active channel/frame. With 8-bit images thresholded values
 * are set to to 255, with 16/32-bit images to 65535.
 * Python example on how to call it from other scripts (see http://imagej.net/BAR for details):
 *
 * #@Context context
 * from bar import Runner
 * runner = Runner(context)
 * arg = "current"  # "", "preceding", "subsequent"
 * runner.runBARMacro("Segmentation/Apply_Threshold_To_ROI.ijm", arg)
 * print("Macro exited: %s " % runner.scriptLoaded())
 *
 */

getThreshold(lower, upper);
if (lower==-1 && upper==-1)
	exit("A thresholded image is required but none was found.");

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
}
if (startsWith(scope, "all")) {
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
setBatchMode(false);
setThreshold(lower, upper);
updateDisplay();

function getMax() {
	max = 255;
	if (bitDepth()>8)
		max = 65535;
	return max;
}
