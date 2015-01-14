# Process_Folder_PY.py
# IJ BAR snippet https://github.com/tferr/Scripts/tree/master/Snippets
#
# This python script implements a reusable batch processor[1] that processes a
# folder of files of a certain type (Jython alternative to the IJ1 built-in
# command Process>Batch>Macro).
#
# It is composed of two functions:
#   1. getFileList(<directory>):
#      Retrieves the recursive list of files in <directory>, excluding all files
#      that do not have specified extension(s)
#   2. myRoutines(<image>):
#      Container function that holds the image processing routines to be applied
#      to individual files
#
# Processed images are saved as .tif in a sub-directory of the chosen input
# directory named after <OUT_SUBDIR>.
#
# Resources:
# [1] http://fiji.sc/BAR#Batch_Processors
# https://github.com/tferr/Scripts/tree/master/Snippets#batch-processors
# https://github.com/tferr/Scripts/tree/master/Snippets#python


import csv, os
from ij import IJ, ImagePlus
from ij.io import DirectoryChooser
from ij.measure import ResultsTable
from bar import Utils


# Returns a list containing the file paths in the specified directory
# path. The list is recursive (includes subdirectories) and will only
# include files matching the specified extensions.
def getFileList(directory):
    extensions = (".tif", ".stk", ".oib")
    files = []
    for (dirpath, dirnames, filenames) in os.walk(directory):
        if OUT_SUBDIR in dirnames: dirnames.remove(OUT_SUBDIR)
        for f in filenames:
            if f.endswith(extensions):
                files.append(os.path.join(dirpath, f))
    return files


# This function processes individual files (the argument <image>
# being an ImagePlus object). In this example, image is renamed using
# a random string. See http://fiji.sc/BAR#Python for details
def myRoutines(image):
    import uuid
    image.setTitle( str(uuid.uuid4()) )

# Define the name of the output subdirectory
OUT_SUBDIR = "_Processed"

# Retrieve input directory
src_dir = DirectoryChooser("Choose input directory").getDirectory()

# Retrieve list of filtered files
files = getFileList(str(src_dir));

if files:

    # Define output directory and create it if needed
    out_dir = src_dir + OUT_SUBDIR + os.sep
    if not os.path.exists(out_dir): os.makedirs(out_dir)

    # Create a CSV table documenting processed files
    csvPath = out_dir + "_ProcessedFileList.csv"
    csvExists = os.path.exists(csvPath)
    csvFile = open(csvPath, 'a')
    csvWriter = csv.writer(csvFile)

    # Specify column headings
    if not csvExists:
        headers = ['Original path','Processed path']
        csvWriter.writerow(headers)

    # Process list of images
    for (counter, f) in enumerate(files):

        # Display progress
        IJ.showStatus("Processing file "+ str(counter+1) +"/"+ str(len(files)))

        # Open each image and process it
        imp = IJ.openImage(f)
        myRoutines(imp)

        # Save processed image in out_dir (enforcing .tif extension)
        newpath = os.path.splitext(out_dir + imp.getTitle())[0] +".tif"
        IJ.saveAsTiff(imp, newpath)
        imp.close()

        # Log paths of processed files
        csvWriter.writerow([f, newpath])

    # Display CSV log
    csvFile.close()
    rt = ResultsTable.open(csvPath)
    rt.show("_ProcessedFileList.csv")

    # Proudly inform that processing terminated
    if IJ.showMessageWithCancel("All done","Reveal output directory?"):
        Utils.revealFile(out_dir);

else:
    # Inform no filtered files were found
    IJ.error("No matches for the selected extension(s).")
