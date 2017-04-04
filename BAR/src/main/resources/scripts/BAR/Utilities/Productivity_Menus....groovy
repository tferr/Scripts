//@String(label="Place which tool in the IJ toolbar?", choices={"Calibration Menu", "List Folder Menu", "Shortcuts Menu"}, style="radioButtonVertical") tool
//@boolean(label="Open file after installation") open
//@ImageJ ij
//@UIService uiservice


import bar.Runner

runner = new Runner(ij.getContext())
runner.installIJ1Macro("/tools/${tool.replaceAll(" ", "")}.ijm", true)
if (!runner.scriptLoaded())
	uiservice.showDialog("Unfortunately installation failed. See console for details", "Error")
else if (open)
	runner.openLastLoadedResource()
