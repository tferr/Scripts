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
 */

requires("1.49q"); // In older versions maskID is diplayed in batch mode if hyperstack
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
	}
setBatchMode(false);


function helpMsg() {
	msg = '<html>'
	+ '<div WIDTH=420>'
	+ 'Since bitmap objects are discretised into a regular lattice of pixels, '
	+ 'circularity is highly affected by the particle size and may not be '
	+ 'valid for very small sizes. Here are some calculations using IJ 1.49t: '
	+ '<table align="center">'
	+ '  <tr>'
	+ '    <th>Particle size (pixels)</th>'
	+ '    <th>Circularity range</th>'
	+ '  </tr>'
	+ '  <tr align="center">'
	+ '    <td>1</td>'
	+ '    <td>Always 1</td>'
	+ '  </tr>'
	+ '  <tr align="center">'
	+ '    <td>2</td>'
	+ '    <td>0.79&mdash;1</td>'
	+ '  </tr>'
	+ '  <tr align="center">'
	+ '    <td>3</td>'
	+ '    <td>0.52&mdash;1</td>'
	+ '  </tr>'
	+ '  <tr align="center">'
	+ '    <td>4</td>'
	+ '    <td>0.39&mdash;1</td>'
	+ '  </tr>'
	+ '  <tr align="center">'
	+ '    <td>&hellip;</td>'
	+ '    <td>&hellip;</td>'
	+ '  </tr>'
	+ '  <tr align="center">'
	+ '    <td>10</td>'
	+ '    <td>0.31&mdash;1</td>'
	+ '  </tr>'
	+ '  <tr align="center">'
	+ '    <td>64</td>'
	+ '    <td>0.02&mdash;1</td>'
	+ '  </tr>'
	+ '</table>'
	+ 'This means that e.g., if you set <i>Size</i> to <tt>&le;4</tt> and '
	+ '<i>Circularity</i> to <tt>0.0-0.3</tt> no filtering operation will '
	+ 'be performed because it is not possible to fullfill both conditions. '
	+ 'You can read more about shape descriptors on the '
	+ '<a href="http://imagej.nih.gov/ij/docs/guide/">IJ User guide</a> and '
	+ '<a href="http://fiji.sc/Category:Particle_analysis">Fiji website</a>.'
	+ '</div>'
	+ '</html>';
	return msg;
}
