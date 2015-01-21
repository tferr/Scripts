# Clipboard_to_Results.py
# IJ BAR: https://github.com/tferr/Scripts#scripts
#
# Imports numeric values copied to the clipboard into the Results table. Useful, since
# BARs that analyze tabular data can only read values from the main IJ "Results" table
#
# NB: When copying data from withing IJ (e.g., lists from histograms or plot profiles),
# Use Edit>Options>Input/Output... to specify if column headers/row numbers should be
# copied to the clipboard


import os, sys, tempfile, bar
from ij import IJ
from ij.plugin.filter import Analyzer
import ij.measure.ResultsTable as RT


# Extend the search path to /BAR/lib/
sys.path.append(bar.Utils.getLibDir())

# Import common functions in /BAR/lib/BARlib.py
import BARlib as lib

fd, path = tempfile.mkstemp()
try:
    os.write(fd, lib.getCliboardText())
    os.close(fd)
    rt = RT.open(path) #IOException if getClipboard()==""
    if Analyzer.resetCounter():
        rt.show("Results")
except:
    IJ.error("Could not place clipboard into Results table.")
finally:
    os.remove(path)
