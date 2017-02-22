# Python05-2.py
# IJ BAR: https://github.com/tferr/Scripts#scripts
####################################################
# 5.2 Scripting ImageJ: Creating an empty image (II)
####################################################

# Now that we have the documentation opened in our browsers,
# It will be easy to create the empty image:

import ij.gui.NewImage as NI

# First we define all the arguments
title = "My Image"	# The image title
width = 512		# Image dimension: width
height = 512	# Image dimension: height
n_slices = 10	# Image dimension: depth
bit_depth = 8	# Acceptable values: 8, 16, 24 (RGB) and 32 (float)
options = NI.FILL_RAMP # other choices:  FILL_BLACK, FILL_WHITE

# Now we create the image (an ImagePlus object)
image = NI.createImage(title, width, height, n_slices, bit_depth, options)

# Finally we show it!
image.show()
