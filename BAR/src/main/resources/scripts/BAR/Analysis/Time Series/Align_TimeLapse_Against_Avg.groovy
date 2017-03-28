//@ImagePlus imp
//@UIService uiservice

import ij.IJ
import ij.ImagePlus
import ij.plugin.ZProjector
import fiji.plugin.trackmate.gui.GuiUtils

def validateImp(imp) {
    GuiUtils.userCheckImpDimensions(imp)
    frames = imp.getNFrames()
    valid = imp.getNFrames() > 1
	if (!valid)
    	uiservice.showDialog("Time sequence required", "Invalid image")
    valid
}

def proj(imp, method) {
	def zp = new ZProjector(imp)
	zp.setMethod(method)
	zp.doProjection()
	return zp.getProjection()
}

def projAVG(imp) {
	proj(imp, ZProjector.AVG_METHOD)
}


if (!validateImp(imp))
	return

avg = projAVG(imp)
stack = imp.getStack()
stack.addSlice("AVG_ANCHOR", avg.getProcessor(), 0)
imp.setSlice(1)
for (method in ["Rigid Body", "Affine"]) {
	IJ.run(imp, "StackReg", "transformation=[$method]")
}
stack.deleteSlice(1)
imp.setStack(stack)
avg.show()
