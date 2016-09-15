#@ImagePlus img
#@Double(label="X radius",value=2.0) xradius
#@Double(label="Y radius",value=2.0) yradius
#@Double(label="Z radius",value=2.0) zradius


# Median_Filter.py
# IJ BAR snippet https://github.com/tferr/Scripts/tree/master/Snippets
#
# This python[1] snippet performs a filtering operation on the active image and
# exemplifies how to create a generic, reusable script based on the IJ API[2].
#
# The getSettings(ImagePlus) function will only prompt the user for parameters
# if the global variables xradius, yradius, zradius are undefined (i.e., None).
#
# [1] https://github.com/tferr/Scripts/tree/master/Snippets#python
# [2] http://javadoc.imagej.net

from ij.plugin import Filters3D
from ij import ImagePlus

# Get the image stack within the ImagePlus of img
stack = img.getStack()

# Instantiate ij.plugin.Filters3D
f3d = Filters3D()

# Retrieve filtered stack
newStack = f3d.filter(stack, f3d.MEDIAN, xradius, yradius, zradius)

# Construct a new ImagePlus from the stack
fImg = ImagePlus("Filtered_"+img.getTitle(), newStack);

# Other processing could go here (...)
#IJ.run(fImg, "Shen-Castan Edge Detector", "coefficient=0.50");

# Display result
fImg.show()
