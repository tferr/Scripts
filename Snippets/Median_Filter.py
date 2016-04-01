#@ImagePlus img
#
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


from ij import IJ, ImagePlus
from ij.gui import GenericDialog
from ij.plugin import Filters3D

# Variables defining the filter radius
xradius = yradius = zradius = None

def getSettings(img):
    """This function assesses (by returning a boolean value) if the
    filter can be applied to the image passed as argument. Will ask
    the user for new values if current parameters are undefined."""
    global xradius, yradius, zradius

    # Get new values if at least one of the parameters is 'null'
    if None in (xradius, yradius, zradius):
        gd = GenericDialog("Median Filter")
        gd.addNumericField("X radius:", 2.0, 1)
        gd.addNumericField("Y radius:", 2.0, 1)
        gd.addNumericField("Z radius:", 2.0, 1)
        gd.showDialog()
        xradius = gd.getNextNumber()
        yradius = gd.getNextNumber()
        zradius = gd.getNextNumber()
 
    return gd.wasOKed()

# Can we proceed?
if getSettings(img):

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