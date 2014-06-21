/* Clear_Thresholded_Pixels.ijm
 * IJ BAR: https://github.com/tferr/Scripts#scripts
 *
 * IJ1 macro that sets to zero thresholded pixels inside the active area ROI, or the entire image if
 * no ROI exists. Useful for semi-automated segmentation, complementing 'Apply Threshold To ROI' and
 * the Paintbrush/Pencil Tools.
 *
 * Works with grayscale images but in the case of multi-dimensional stacks threshold is only applied
 * to the Z-dimension. Examples on how to call it from other scripts (see http://fiji.sc/BAR for
 * details):
 *
 *   ctp = getDirectory("plugins") +"Scripts"+ File.separator +"BAR"+ File.separator
 *         +"Segmentation"+ File.separator +"Clear_Thresholded_Pixels.ijm";
 *   IJ.runMacroFile(ctp, "");          // show dialog prompt
 *   IJ.runMacroFile(ctp, "current")    // only active slice is considered
 *   IJ.runMacroFile(ctp, "preceding")  // clear pixels until active slice
 *   IJ.runMacroFile(ctp, "subsequent") // clear pixels from active slice
 *
 * TF 2014.06
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
	Dialog.create("Clear Thresholded Pixels");
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
for (i=start; i<=end; i++) {
	Stack.setPosition(channel, i, frame);
	changeValues(lower, upper, 0);
}
Stack.setPosition(channel, currentSlice, frame);
setBatchMode(false);
