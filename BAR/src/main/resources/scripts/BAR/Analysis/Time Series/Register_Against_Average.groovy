//@ImagePlus imp
//@UIService uiservice

import bar.BAR
import ij.IJ


def loadLib(resourcePath) {
	url = BAR.getClass().getResource(resourcePath)
	gcl = new GroovyClassLoader()
	cls = gcl.parseClass(new GroovyCodeSource(url))
	(GroovyObject) cls.newInstance()
}

utils = loadLib("/scripts/Utils.groovy")
if (!utils.isSingleChannelTimeseq(imp, uiservice))
	return

stack = imp.getStack()
avg = utils.projAVG(imp, 1, stack.getSize())
stack.addSlice("AVG_ANCHOR", avg.getProcessor(), 0)
imp.setSlice(1)
for (method in ["Rigid Body", "Affine"])
	IJ.run(imp, "StackReg", "transformation=[$method]")
stack.deleteSlice(1)
imp.setStack(stack)
