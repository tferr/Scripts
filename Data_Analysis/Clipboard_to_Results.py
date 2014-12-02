import os
from ij import IJ
from ij.plugin.filter import Analyzer
import ij.measure.ResultsTable as RT


def getClipboard():
    """Tries to extract text from the system clipboard"""
    from java.awt.datatransfer import DataFlavor
    from java.awt import Toolkit

    clipboard = Toolkit.getDefaultToolkit().getSystemClipboard()
    contents = clipboard.getContents(None)
    if (contents!=None) and contents.isDataFlavorSupported(DataFlavor.stringFlavor):
        return contents.getTransferData(DataFlavor.stringFlavor)
    else:
        return ""

def error():
    IJ.error("Could not place clipboard into Results table.")


s = getClipboard()
if s!="":
    try:
        path = IJ.getDirectory("temp") +"IJClipboardTable.csv"
        rtFile = open(path, "w")
        rtFile.write(s)
        rtFile.close()
        if os.path.isfile(path) and Analyzer.resetCounter():
            rt = RT.open(path)
            rt.show("Results")
    except:
        error()
else:
    error()