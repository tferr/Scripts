import csv, os
from ij import IJ, ImagePlus
from ij.io import DirectoryChooser
from ij.measure import ResultsTable

def getFileList(directory):
    files = []
    for dirpath, dirnames, filenames in os.walk(directory):
        for f in filenames:
            if f.endswith((".tif", ".TIF", ".stk", ".STK")):
                files.append(os.path.join(dirpath, f))
    return files


def processImage(image):
    import uuid
    image.setTitle( str(uuid.uuid4()) )
    image.show()



srcDir = DirectoryChooser("Choose input directory").getDirectory()
files = getFileList(str(srcDir));

if files:

    outDir = srcDir +"Processed"+ os.sep
    if not os.path.exists(outDir): os.makedirs(outDir)

    csvPath = outDir + "_ProcessedFileList.csv"
    csvFile = open(csvPath, 'w')
    csvWriter = csv.writer(csvFile)

    headers = ['Original path','Processed path']
    csvWriter.writerow(headers)

    for (counter, f) in enumerate(files):
        imp = IJ.openImage(f)
        processImage(imp)
        newpath= outDir + imp.getTitle() +".tif"
        IJ.saveAsTiff(imp, newpath)
        csvWriter.writerow([f, newpath])

    csvFile.close()
    rt = ResultsTable.open(csvPath)
    rt.show("_ProcessedFileList.csv")

else:
    IJ.error("No files to process.")
