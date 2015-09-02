# Python05-3.py
# IJ BAR: https://github.com/tferr/Scripts#scripts
####################################################
# 5.3 Scripting ImageJ: Creating an empty image (III)
####################################################

# Previously, we assembled all the code required to
# generate a new image. This is how it looked like:

import ij.gui.NewImage as NI

# First we define the image properties
title = "My Image"	# The image title
width = 512		# Image dimension: width
height = 512	# Image dimension: height
n_slices = 10	# Image dimension: depth
bit_depth = 8	# Acceptable values: 8, 16, 24 (RGB) and 32 (float)
options = NI.FILL_RAMP # Other choices: FILL_BLACK & FILL_WHITE

# Now we create the image (An ij.ImagePlus object)
image = NI.createImage(title, width, height, n_slices, bit_depth, options)


# We'll generate the code require to place a ROI right
# in the middle of our image! Here is how:

from ij.gui import Roi

# We define the ROI properties
rec_width = 100		# The width of our rectangular ROI
rec_height = 100	# The height of our rectangular ROI

# We create the ROI (A ij.gui.Roi object)
my_rectangle = Roi(
    width/2 - rec_width/2,	# 1st argument: x-positioning
    height/2 - rec_width/2, # 2nd argument: y-positioning
    rec_width,	# 3rd argument: ROI width
    rec_height	# 4th argument: ROI height
    )

# Now we just need to associate the ROI with our image. Did
# we say an image is an ImagePlus? Then, we'll look which
# methods in the ImagePlus class allow us to assigns our
# ROI with our image. Here is what we found:
image.setRoi(my_rectangle)

# Found it? Great! Then we'll proudly show everything!
image.show()
