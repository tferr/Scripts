/* Clear_Thresholded_Pixels.ijm
 * IJ BAR: https://github.com/tferr/Scripts
 *
 * IJ1 macro that sets to zero thresholded pixels inside the active area ROI, or the entire image if
 * no ROI exists. Useful for semi-automated segmentation, complementing 'Apply Threshold To ROI' and
 * the Paintbrush/Pencil Tools.
 *
 * Works with grayscale images but in the case of multi-dimensional stacks threshold is only applied
 * to the Z-dimension of the active channel/frame.
 * Python example on how to call it from other scripts (see http://imagej.net/BAR for details):
 *
 * #@Context context
 * from bar import Runner
 * runner = Runner(context)
 * arg = "all slices"  # "current slice only", "preceding slices", "subsequent slices"
 * runner.runBARMacro("Segmentation/Clear_Thresholded_Pixels.ijm", arg)
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
	Dialog.create("Clear Thresholded Pixels");
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
for (i=start; i<=end; i++) {
	Stack.setPosition(channel, i, frame);
	changeValues(lower, upper, 0);
}
Stack.setPosition(channel, currentSlice, frame);
setBatchMode(false);
setThreshold(lower, upper);
updateDisplay();
