/* Wipe_Background.ijm
 * IJ BAR: https://github.com/tferr/Scripts#scripts
 *
 * ImageJ macro that clears (sets to zero) clusters of thresholded pixels of defined
 * circularity & size within the active area ROI (or the whole image if no ROI exists).
 *
 * It tries to be as unobtrusive as possible and accepts any grayscale image including
 * multidimensional hyperstacks. Currently the filtering is restricted to the Z-dimension,
 * but it could be easily extended to other dimensions. It could be greatly simplified if
 * the goal was to deal exclusively with 2D/3D images
 *
 * Know issues:
 * - 5D stacks escape batch mode
 *
 * TF, 2014.10
 */

getThreshold(lower, upper);
if (lower==-1 && !is("binary"))
	exit("A thresholded or 8-bit binary image is required.");

optnsA = newArray("Active ROI", "Whole image");
optnsB = newArray("Current slice only", "All slices", "Preceding slices", "Subsequent slices");

imgID = getImageID();
Stack.getDimensions(null, null, channels, depth, frames);
Stack.getPosition(channel, activeSlice, frame);
start = activeSlice;
end = activeSlice;
roi = selectionType();
areaROI = roi>-1 && roi<5 || roi==9;
scope = optnsB[0];
whole = false;

setBatchMode(true);

	// Prompt for range and scope of analysis
	Dialog.create("Wipe Background - Filtering Particles");
		Dialog.addNumber("Size "+ fromCharCode(8804), 10, 0, 10, "pixels");
		Dialog.addString("Circ.", "0.00-1.00", 15);
		if (areaROI)
			Dialog.addChoice("Scope:", optnsA);
		if (depth>1)
			Dialog.addChoice("Apply to:", optnsB);
		Dialog.addMessage("Clusters of thresholded pixels\nwithin the range of the "+
				"specified\nsettings will be cleared (set to 0)");
	Dialog.show();

	size = Dialog.getNumber();
	circ = Dialog.getString();
	if (areaROI)
		if (Dialog.getChoice==optnsA[1])
			whole = true;
	if (depth>1) {
		scope = Dialog.getChoice();
		if (scope==optnsB[1]) {
			start = 1; end = depth;
		} else if (scope==optnsB[2]) {
			start = 1; end = activeSlice;
		} else if (scope==optnsB[3]) {
			start = activeSlice; end = depth;
		} else {
			start = activeSlice; end = activeSlice;
		}
	}

	// Create stack of noise particles
	analyzerArg = "size=0-"+ size +" pixel circularity="+ circ +" show=Masks ";
	if (scope!=optnsB[0])
		analyzerArg += " stack";
	if (whole)
		run("Select None");
	run("Analyze Particles...", analyzerArg);

	maskID = getImageID();
	if (maskID==imgID) {
		beep(); showStatus("No particles found!"); exit();
	} else {
		// Active image is now a mask from ParticleAnalyzer (background= 255). This mask
		// is never a multidimensional stack (IJ 1.48o), so we'll convert it. Then, chosen
		// noise within the selected range will be set to 0, all remaining pixels to 1
		run("Invert", "stack");
		if (channels*frames >1 && scope!=optnsB[0]) {
			run("Stack to Hyperstack...", "channels="+ channels +" slices="+ depth +" frames="+ frames);
			maskID = getImageID();
		}
		for (c=1; c<=channels; c++) {
			for (t=1; t<=frames; t++) {
				for (z=1; z<=depth; z++) {
					Stack.setPosition(c, z, t);
					if (z>=start && z<=end)
						run("Divide...", "value=255 slice");
					else
						run("Set...", "value=1 slice");
				}
			}
		}

		// Perform the filtering
		if (scope==optnsB[0])
			imageCalculator("Multiply stack", imgID, maskID);
		else
			imageCalculator("Multiply", imgID, maskID);

		// Restore original settings
		Stack.setPosition(channel, activeSlice, frame);
		if (areaROI) run("Restore Selection");
		setThreshold(lower, upper);
		closeImg(maskID);
	}
setBatchMode(false);

// For whatever reason (IJ 1.48o) some hyperstacks manage to be displayed in BatchMode
function closeImg(id) {
	selectImage(id); close();
}
