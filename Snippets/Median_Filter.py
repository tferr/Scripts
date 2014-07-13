# Median_Filter.py
# IJ BAR snippet https://github.com/tferr/Scripts/tree/master/Snippets
#
# This python[1] snippet performs a filtering operation on the frontmost (i.e.,
# active) image and exemplifies how to create a generic, reusable script based
# on the ImageJ API[2].
#
# The getSettings(ImagePlus) function will only prompt the user for parameters
# if the global variables xradius, yradius, zradius are undefined (i.e., None).
#
# [1] https://github.com/tferr/Scripts/tree/master/Snippets#python
# [2] http://imagej.nih.gov/ij/developer/api/
#     http://jenkins.imagej.net/job/ImageJ1-javadoc/javadoc/


from ij import IJ, ImagePlus, WindowManager
from ij.gui import GenericDialog
from ij.plugin import Filters3D

# Variables defining the filter radius
xradius = yradius = zradius = None

def getSettings(img):
    """This function assesses (by returning a boolean value) if the filter can
    be applied to the image passed as argument. Will ask the user for new values
    if current parameters are undefined."""
    global xradius, yradius, zradius
    canProceed = True

    if not img:
        IJ.error("No images open.")
        print ">>>> No image to work with!"
        canProceed = False

    # Get new values if at least one of the parameters is 'null'
    if None in (xradius, yradius, zradius):
        gd = GenericDialog("Median Filter")
        gd.addNumericField("X radius:", 2.0, 1)
        gd.addNumericField("Y radius:", 2.0, 1)
        gd.addNumericField("Z radius:", 2.0, 1)
        gd.showDialog()
        if gd.wasCanceled():
            print ">>>> User canceled dialog!"
            canProceed = False
        else:
            xradius = gd.getNextNumber();
            yradius = gd.getNextNumber();
            zradius = gd.getNextNumber();
 
    return canProceed


# Get active image and store it in the variable 'img'
img = WindowManager.getCurrentImage()

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