#@Context context
#@UIService uiService

import math, sys
from ij import IJ
from bar import Utils
import ij.measure.ResultsTable as RT


def plot_distributions():
	from bar import Runner, Utils
	from java.util import HashMap

	rt = Utils.getResultsTable()
	options = HashMap()
	options.put("parameter", "X")
	runner = Runner(context)
	runner.runScript("Data_Analysis", "Distribution_Plotter.ijm", options)
	if not runner.scriptLoaded():
		uiService.showDialog("Distribution of NN distances not plotted.\nCheck console for details", "Error")


plot_distributions()