/* Segment_Profile.ijm
 * IJ BAR: https://github.com/tferr/Scripts#scripts
 *
 * This IJ1 tool takes a straight line and extracts the segments within that line that are above
 * threshold levels (i.e., it measures the width of the peaks of the profile plot of a given
 * straight line. It can be used to cross-section objects, or measure repetitive motifs in
 * patterned structures, e.g., muscle sarcomeres or annual tree rings. Built-in help is available
 * by double-clicking on the Tool's icon.
 *
 * See also: https://github.com/tferr/Scripts/blob/master/Data_Analysis/README.md#find-peaks, a
 * script that retrieve peaks (local maxima and minima) from a line profile.
 *
 * Non-BAR users: place this file in ImageJ/plugins/Tools/. Re-start ImageJ. Load it using the
 * ">>" drop-down menu in the main ImageJ window
 *
 * TF 2014.02
 */

var upper= -1, minLength, count, lockedLevels;

macro "AutoRun" {
	if (nImages!=0) promptForOptions();
}

macro "Segment Profile Tool - C037 L04f6 L0af9" {
	if (!lockedLevels)
		getThreshold(lower, upper);
	if (upper==-1) {
		showMessageWithCancel("No threshold", "Image is not thresholded or no threshold value\n"
			+ "was set in the Options prompt. Adjust levels?");
		run("Threshold...");
		exit();
	}
	count = Overlay.size;
	getCursorLoc(x, y, z, flags);
	xstart = x; ystart = y;
	x2 = x; y2 = y;
	while (flags&16!=0) {
	    if (isKeyDown("alt")) {
	    	removeSegments(1); exit;
	    }
		getCursorLoc(x, y, z, flags);
		//showStatus("Angle: "+ atan((y-ystart)/(x-xstart)*180/PI));
		if (flags&16==0) {
		   dx= x2-xstart; dy= y2-ystart;
		}
		if (x!=x2 || y!=y2)
			makeLine(xstart, ystart, x, y);
	}
	segmentStraightLine(upper, minLength);
	run("Select None");
}

macro "Segment Profile Tool Options" { promptForOptions(); }


function segmentStraightLine(value, lngth) {
	// get the slope, the y intercept and angle (0-90 deg) of line: y=tan(angle)*(x-a)+b
	getLine(xA, yA, xB, yB, null);
	m = (yB-yA)/(xB-xA); b = yA-(m*xA); angle = atan(abs(m))*180/PI;

	// assume just one quadrant: ensure line has been drawn from 'left>right', 'down>up'
	leftX = minOf(xA, xB); downY = maxOf(yA, yB);

	// get number of coordinates that define the line in 1-pixel increments: the largest
	// of the horizontal/vertical displacement
	if (angle<=45)
		ccLength = round(abs(xA-xB))+1;
	else
		ccLength = round(abs(yA-yB))+1;

	// create the array of points that define the line
	segX = newArray(ccLength); segY = newArray(ccLength);
	for (i=0; i<ccLength; i++) {
		if (angle==90) {
			segX[i] = leftX; segY[i] = downY-i;
		} else if (angle<=45) {
			segX[i] = leftX+i; segY[i] = (m*segX[i])+b;
		} else {
			segY[i] = downY-i; segX[i] = (segY[i]-b)/m;
		}
	}

	// determine the n. of segments to be created
	counter1 = 0; counter2 = 0;
	for (i=1; i<ccLength; i++) {
		currentPixel = getPixel(segX[i],segY[i]);
		previousPixel = getPixel(segX[i-1],segY[i-1]);
		if (currentPixel>value && previousPixel<=value) counter1++;
		if (currentPixel<=value && previousPixel>value) counter2++;
	}
	if (counter1==0 && counter2==0)
		{ beep; showStatus("No Boundaries detected!"); return; }

	// define the coordinates of segment boundaries
	x1 = newArray(counter1); y1 = newArray(counter1); j = 0;
	x2 = newArray(counter2); y2 = newArray(counter2); k = 0;
	for (i=1; i<ccLength; i++) {
		currentPixel = getPixel(segX[i],segY[i]);
		previousPixel = getPixel(segX[i-1],segY[i-1]);
		if (currentPixel>value && previousPixel<=value) {
			x1[j] = (segX[i]+segX[i-1])/2; y1[j++] = (segY[i]+segY[i-1])/2;
		} else if (currentPixel<=value && previousPixel>value) {
			x2[k] = (segX[i]+segX[i-1])/2; y2[k++] = (segY[i]+segY[i-1])/2;
		}
	}

	// create segments
	counter = minOf(counter1,counter2); out = 0;
	for (i=0; i<counter; i++) {
		dx = sqrt((x2[i]-x1[i])*(x2[i]-x1[i])+(y2[i]-y1[i])*(y2[i]-y1[i]));
		toScaled(dx);
		if (dx>lngth) {
			// would be nice to assign a name to the overlay
			Overlay.drawLine(x1[i],y1[i],x2[i],y2[i]);
			Overlay.add;
		} else
			out++;
	}
	Overlay.show;
	showStatus(counter-out +" segment(s) created ("+ counter +" detected)");
}

function removeSegments(idx) {
    n = Overlay.size;
	for (i=n; i>n-idx && i>0; i--)
		Overlay.removeSelection(i-1);
}

function promptForOptions() {
	msg = "<html>This tool extracts the segments within a straight line that are above<br>"
		+ "threshold levels. Extracted segments are stored in the image overlay.<br><br>"
		+ "<b>Smallest segment length</b><br>"
		+ "The minimum length segments can have. Shorter segments will be<br>"
		+ "ignored. Set it to 0 (the default) to include all segments within the<br>"
		+ "profiled line.<br><br>"
		+ "<b>Do not highlight threshold levels</b><br>"
		+ "If active, the specified threshold value is used, and thresholded<br>"
		+ "pixels are not highlighted.<br><br>"
		+ "<b>Undo last segmentation</b><br>"
		+ "Removes all segments from the last segmented line. Alt-click on<br>"
		+ "the image canvas to remove just the last segment.<br><br>"
		+ "<b>Run demo</b><br>"
		+ "If selected, a demonstrative segmentation of the <i>Tree Rings</i> sample<br>"
		+ "image is performed <br><br>"
		+ "<b>Handling of segments</b>"
		+ "<table >"
		+ "<tr><td>&emsp;<b>Delete last</b></td><td>Alt-click on image</td></tr>"
		+ "<tr><td>&emsp;<b>Labels</b></td><td>Use <i>Image>Overlay>Labels...</i> This allows activation<br>"
		+ "of individual segments by clicking on their labels</td></tr><br>"
		+ "<tr><td>&emsp;<b>Retrieval</b></td><td>Use <i>Image>Overlay>To ROI Manager</i></td></tr>"
		+ "</table>";
	Dialog.create('Segment Profile Options...');
	getPixelSize(unit, null, null);
	Dialog.addNumber('Smallest segment length:', minLength, 2, 6, unit);
	Dialog.setInsets(15, 0, 5);
	Dialog.addCheckbox('Do not highlight threshold levels. Use this value instead:', lockedLevels);
	Dialog.addNumber('Upper threshold level:', upper);
	Dialog.setInsets(10, 0, 0);
	Dialog.addCheckbox('Undo last segmentation', false);
	Dialog.setInsets(10, 0, 0);
	Dialog.addCheckbox('Run demo upon "OK" (internet connection required)', false);
	Dialog.setInsets(15, 0, 0);
	Dialog.addMessage("NB: Double-click on the tool icon to reconfigure these options");
	Dialog.addHelp(msg);
	Dialog.show;

	minLength = Dialog.getNumber;
	lockedLevels = Dialog.getCheckbox;
	if (lockedLevels) {
		upper = Dialog.getNumber; resetThreshold;
	}
	if (Dialog.getCheckbox) {
		removeSegments(count); count = 0;
	}
	if (Dialog.getCheckbox)
		runDemo();
}

function runDemo() {
	run("Tree Rings (48K)");
	run("8-bit");
	i = 0; start = getTime;
	while (i<5) {
		makeLine(160, 10+(30*i), 1796, 10+(30*i++), 4);
		segmentStraightLine(65, 4);
	}
	run("To ROI Manager");
	roiManager("Show All without labels");
	showStatus("Determining size of rainy season rings...: "+ d2s((getTime-start)/1000,3) +"s");
}
