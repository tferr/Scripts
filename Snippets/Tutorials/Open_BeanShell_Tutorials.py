import os, java.io.File
from ij import IJ
from bar import Utils
from org.scijava import Context
from net.imagej.ui.swing.script import TextEditor

# Specify the directory containing the files to be opened
dir = Utils.getSnippetsDir() + "Tutorials" + os.sep

# Specify the extension of the files to be opened
ext = ".bsh"

# If directory exists, create a new Script Editor window
# and open each filtered file on a dedicated tab
if Utils.fileExists(dir):
    context = IJ.runPlugIn("org.scijava.Context", "")
    editor = TextEditor(context)
    for (root, dirnames, filenames) in os.walk(dir):
        for filename in filenames:
            if "_" not in filename and filename.endswith(ext):
                path = os.path.join(root, filename)
                editor.open(java.io.File(path))

    # Select first tab, rename editor's window and display it
    editor.switchTo(0)
    editor.setVisible(True)
    editor.setTitle("BAR Tutorial Files" + ext)
