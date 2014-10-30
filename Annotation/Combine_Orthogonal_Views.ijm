/*
 * Combine_Orthogonal_Views.ijm
 * IJ BAR: https://github.com/tferr/Scripts#scripts
 *
 * Appends top, bottom and side views to a stack in a similar way to Image>Stacks>Ortogonal Views
 * but extending projected views to the entire volume rather than displaying single slices.
 *
 * Notes:
 * - Empty spaces in the image canvas will be painted with background color[1] (frame around the
 *   Color Picker Tool in the ImageJ toolbar).
 * - Projections are produced by the ij.plugin.Slicer plugin[2] (Image>Stacks>Reslice). Currently,
 *   Slicer cannot be scripted directly (its API is rather simple[3]). For this reason, this file
 *   was written using the ImageJ macro language as there is no real advantage in choosing more
 *   sophisticated scripting languages
 *
 * [1] http://imagej.nih.gov/ij/docs/guide/146-28.html#fig:CPtool
 * [2] http://imagej.nih.gov/ij/developer/source/ij/plugin/Slicer.java.html
 * [3] http://imagej.nih.gov/ij/developer/api/ij/plugin/Slicer.html
 *
 * Tiago Ferreira, v1.0.0 2014.10.30
 */


// Open a suitable sample image if none can be found
if (nImages==0)
 newSampleImage();
if (nSlices()==1 || Stack.isHyperstack)
 newSampleImage();

// Gather details on active image
imgId= getImageID();
imgTitle = getTitle();
imgWidth = getWidth();
imgHeight = getHeight();
imgDepth = nSlices();
getVoxelSize(vWidth, vHeight, vDepth, vUnit);

// Do not display any intermediate images
setBatchMode(true);

// Process entire image, ignoring straight line ROIs
run("Select None");

// Create projections with equal n. of slices renamed after respective view
views = newArray("Top", "Left", "Bottom", "Right");
for (i=0; i<views.length; i++) {
	selectImage(imgId);
	if (i%2) { // top and bottom
		spacing = imgWidth/imgDepth * vWidth;
		rOption = " rotate";
	} else { // left and right
		spacing = imgHeight/imgDepth * vWidth; //NB: ij.plugin.Slicer ignores voxel height (2014.10)
		rOption = " ";
	}
	run("Reslice [/]...", "output="+ spacing +" start="+ views[i] + rOption);
	rename(views[i]);
}

// Resize top view and combine it
stretchCanvas("Top", imgWidth, -1);
comb1 = combineStacks("Top", imgTitle, true);

// Resize bottom view and combine it
stretchCanvas("Bottom", imgWidth, -1);
comb2 = combineStacks(comb1, "Bottom", true);

// Resize left view and combine it
canvasHeight = getHeight();
stretchCanvas("Left", -1, canvasHeight);
comb3 = combineStacks("Left", comb2, false);

// Resize right view and combine it
stretchCanvas("Right", -1, canvasHeight);
comb4 = combineStacks(comb3, "Right", false);

// Display result
setVoxelSize(vWidth, vHeight, vDepth, vUnit);
setBatchMode("exit & display");


/* Prompts user for a suitable sample image */
function newSampleImage() {
	showMessageWithCancel("A stack is required but none was found.\nOpen sample image?");
	run("T1 Head (2.4M, 16-bits)");
}

/* Runs Image>Stacks>Tools>Combine..., returning the name of the combined stack */
function combineStacks(stack1title, stack2title, vertically) {
	options = "stack1=["+ stack1title +"] stack2=["+ stack2title +"] ";
	if (vertically) options += "combine";
	run("Combine...", options);
	newStackTitle = ""+ stack1title +"-"+ stack2title;
	rename(newStackTitle);
	return newStackTitle;
}

/* Runs Image>Adjust>Canvas Size... Specified dimensions are ignored if set to -1 */
function stretchCanvas(image, newWidth, newHeight) {
	selectImage(image);
	if (newWidth==-1) newWidth = getWidth();
	if (newHeight==-1) newHeight = getHeight();
	run("Canvas Size...", "width="+ newWidth +" height="+ newHeight +" position=Center");
}
