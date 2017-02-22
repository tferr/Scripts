# Python05-4.py
# IJ BAR: https://github.com/tferr/Scripts#scripts
####################################################
# 5.4 Scripting ImageJ: Creating an empty image (IV)
####################################################

# What about changing the ROI properties (color, name
# name, dimensions, etc.) of the Rectangle created in
# the previous exercise? Lets ask ourselves about it:
from ij import IJ
import ij.gui.YesNoCancelDialog as dialog

# Let's prompt us for the question:
question_prompt = dialog(
    IJ.getInstance(),				# 1st argument: Parent frame
    "What about ROI properties?",	# 2nd argument: Dialog title
    "Can you change the ROI color, its name, etc?"	# 3rd argument: Dialog message
    )	# see http://javadoc.imagej.net/ImageJ1/index.html?ij/gui/YesNoCancelDialog.html

# Let's get the answer:
if question_prompt.cancelPressed():	# Was the dialog canceled?
    IJ.showMessage("Refuse to answer? Are you tired?")
elif question_prompt.yesPressed():	# Was the answer "Yes"?
    IJ.showMessage("Cool. Go ahead!")
else: # If none of the above, the remaining option was "No"
    IJ.showMessage("Hmmm... Did you look into the ij.gui.Roi API?")

# Tip 1: You will find useful methods in:
# - http://javadoc.imagej.net/ImageJ1/index.html?ij/gui/Roi.html
# - http://javadoc.imagej.net/ImageJ1/index.html?ij/ImagePlus.html
#
# Tip 2: Let the recorder do it for you
# - Plugins> Macros> Record...
# - Select a scripting language (e.g. BeanShell)
# - Set your prefered options in Edit>Selection>Properties
#   Q1: What was the string produced by the recorder?
#   Q2: How can it be incorporated in the previous exercise?
