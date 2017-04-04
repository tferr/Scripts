# Clipboard_to_Results.py
# IJ BAR: https://github.com/tferr/Scripts#scripts
#
# Imports numeric values copied to the clipboard into the Results table. It is
# now deprecated. It was of utility prior to BAR v1.1.7, when BARs that analyzed
# tabular data could only read values from the main IJ "Results" table. It is
# included here as a programming example.
#
# Requirements: Requires BAR_-XX.jar to be installed in the plugins folder of IJ
#
# NB: When copying data from withing IJ (e.g., lists from histograms or plot profiles),
# Use Edit>Options>Input/Output... to specify if column headers/row numbers should be
# copied to the clipboard


import os, tempfile
from bar import Utils as barUtils
from ij import IJ
from ij.plugin.filter import Analyzer
import ij.measure.ResultsTable as RT


fd, path = tempfile.mkstemp()
try:
    os.write(fd, barUtils.getClipboardText())
    os.close(fd)
    rt = RT.open(path) #IOException if getClipboardText()==""
    if Analyzer.resetCounter():
        rt.show("Results")
except:
    IJ.showMessage("Could not place clipboard into Results table.")
finally:
    os.remove(path)
