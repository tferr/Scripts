# NN_Distances.py
# IJ BAR snippet https://github.com/tferr/Scripts/tree/master/Snippets
#
# Calculates the closest pair of points from a 2D/3D list of centroid coordinates (opened in the IJ
# 'Results' table) calling another BAR script to plot the frequencies of nearest neighbor distances.
#
# TF 20150810

import math, sys
from ij import IJ, Menus
from bar import Utils
import ij.measure.ResultsTable as RT

# Specify column headings listing x,y,z positions
xHeading, yHeading, zHeading = "X", "Y", "Z"

# Retrieve valid data from the Results table
rt = Utils.getResultsTable();

# Retrive x,y positions
try:
    x = rt.getColumn(rt.getColumnIndex(xHeading))
    y = rt.getColumn(rt.getColumnIndex(yHeading))
except:
    x = y = None

if not None in (x, y):

    # Retrive z positions. Ignore positions if column is not found
    try:
        z = rt.getColumn(rt.getColumnIndex(zHeading))
    except:
        IJ.log("Z-column not found: Assuming 2D distances...")
        z = [0]*len(x)

    # Calculate distances for all positions. Retrieve NNs distances
    for i in range(len(x)):
        minDx = sys.maxint
        nearest = 0
        for j in range(len(x)):
            if i==j: continue
            dx = (x[i]-x[j])**2
            dy = (y[i]-y[j])**2
            dz = (z[i]-z[j])**2
            dst = math.sqrt(dx+dy+dz)
            if dst>0 and dst<minDx:
                minDx = dst
                nearest = j+1
        rt.setValue("NN pair", i, nearest)
        rt.setValue("NN distance", i, minDx);

    # Display appended results
    rt.showRowNumbers(True)
    rt.show("Results")

    # Display distributions
    dp = "Distribution Plotter"
    if dp in Menus.getCommands().keySet().toArray():
        IJ.run(dp, "parameter=[NN distance] automatic=Freedman-Diaconis");
    else:
        IJ.error("File missing", dp+" not found.\nPlease check your BAR installation.")

else:
    IJ.error("Invalid Results Table","Data for X,Y positions not found.")