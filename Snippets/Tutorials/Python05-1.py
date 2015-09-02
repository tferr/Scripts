# Python05-1.py
# IJ BAR: https://github.com/tferr/Scripts#scripts
####################################################
# 5.1 Scripting ImageJ: Creating an empty image (I)
####################################################

# Lets create an empty image with a ROI on it using the ImageJ
# API. Don't know which methods to call? Don't worry, it is all
# well documented on http://javadoc.imagej.net! (No need to
# memorize the URL: Use "BAR> About BAR..." to access it!)
#
# How do we know which methods to use? There are several ways:
# 1. Javadocs
#   Open the documentation for the IJ1 API and search its index!
#
# 2. Search the source code
#   2.1. Using, e.g., the IJ Search Portal ("BAR> About BAR...")
#   2.2. Using the source button of Command Finder (Ctrl/Cmd + F)
#
#
# IJ features ij.plugin.BrowserLauncher, a java plugin that can
# open URLs in your browser, So lets hack through option 1!


# We'll need the documentation for ij.ImagePlus, ij.gui.NewImage,
# and ij.gui.Roi classes (We'll come back to these later on).
# Because Javadocs reflect the package structure of its Java
# project, we can easily define the full Javadoc URLs:
root_url = "http://javadoc.imagej.net/ImageJ1/index.html?"
class_urls = ["ij/ImagePlus", "ij/gui/Roi", "ij/gui/NewImage"]

import ij.plugin.BrowserLauncher as BL

for url in class_urls:
    BL.openURL(root_url + url + ".html")	# http://javadoc.imagej.net/ImageJ1/ij/plugin/BrowserLauncher.html#openURL(java.lang.String)

# Now you just need to press "Run" :)