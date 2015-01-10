# Process_Folder_PY.py
# IJ BAR snippet https://github.com/tferr/Scripts/tree/master/Snippets
#
# This python snippet implements a generic, reusable script that batch processes
# a folder of images of a certain type. (Python alternative to the IJ1 built-in
# command Process>Batch>Macro.
#
# It is composed of two functions:
#   1. getFileList(<directory>):
#      Retrieves the recursive list of files in <directory>, excluding all files
#      that do not have the desired extension
#   2. processImage(<image>):
#      Container function that holds the image processing routines to be applied
#      to individual files
#
# Processed images are saved as .tif in a "Processed" sub-directory of the chosen
# input directory.
#
# See also:
# https://github.com/tferr/Scripts/tree/master/Snippets#python
# https://github.com/tferr/Scripts/blob/master/Snippets/Process_Folder_IJM.ijm


import csv, os
from ij import IJ, ImagePlus
from ij.io import DirectoryChooser
from ij.measure import ResultsTable


# Returns a list containing the file paths in the specified directory
# path. The list is recursive (includes subdirectories) and will only
# include files matching the specified extensions.
def getFileList(directory):
    files = []
    for dirpath, dirnames, filenames in os.walk(directory):
        for f in filenames:
            if f.endswith((".tif", ".TIF", ".stk", ".STK")):
                files.append(os.path.join(dirpath, f))
    return files


# This function processes individual files (the argument <image>
# being an ImagePlus object). In this example, image is renamed using
# a random string. More details at http://stackoverflow.com/a/10501355
def processImage(image):
    import uuid
    image.setTitle( str(uuid.uuid4()) )
    image.show()



# Retrieve input directory
srcDir = DirectoryChooser("Choose input directory").getDirectory()

# Retrieve list of filtered files
files = getFileList(str(srcDir));

if files:

    # Define output directory and create it if needed
    outDir = srcDir +"Processed"+ os.sep
    if not os.path.exists(outDir): os.makedirs(outDir)

    # Create a CSV table documenting processed files
    csvPath = outDir + "_ProcessedFileList.csv"
    csvFile = open(csvPath, 'w')
    csvWriter = csv.writer(csvFile)

    headers = ['Original path','Processed path']
    csvWriter.writerow(headers)

    # Process list of images
    for (counter, f) in enumerate(files):

        # Open each image and process it
        imp = IJ.openImage(f)
        processImage(imp)

        # Save processed image in outDir
        newpath= outDir + imp.getTitle() +".tif"
        IJ.saveAsTiff(imp, newpath)

        # Log paths of processed files
        csvWriter.writerow([f, newpath])

    # Display CSV log
    csvFile.close()
    rt = ResultsTable.open(csvPath)
    rt.show("_ProcessedFileList.csv")

else:
    # Inform no filtered files were found
    IJ.error("No files to process.")
