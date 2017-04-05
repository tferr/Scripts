//@String(value="<html>This script normalizes the time-course of a fluorescent signal against<br>a resting state (F0), as required in, e.g., Calcium imaging experiments.<br>It does not correct for photobleaching.", visibility="MESSAGE") msg1
//@String(label="Normalization:", choices={"F/F0", "Delta-F", "F-F0", "(F-F0)/F0", "All"}) output
//@Integer(label="F0: Average of frames 1 to ",min=1,value="10") f0end
//@ImagePlus imp
//@UIService uiservice

import bar.Utils
import ij.plugin.ImageCalculator


def loadLib(resourcePath) {
	url = Utils.getBARresource(resourcePath)
	gcl = new GroovyClassLoader()
	cls = gcl.parseClass(new GroovyCodeSource(url))
	(GroovyObject) cls.newInstance()
}

def getFdivF0(imp, impF0) {
	def roi = getRoi(imp)
	def ic = new ImageCalculator()
	def fDivF0 = ic.run("Divide create 32-bit stack", imp, impF0)
	if (roi)
		fDivF0.setRoi(roi)
	return fDivF0
}

def getFminusF0(imp, impF0) {
	def roi = getRoi(imp)
	def ic = new ImageCalculator()
	def impFminusF0 = ic.run("Subtract create 32-bit stack", imp, impF0)
	if (roi)
		impFminusF0.setRoi(roi)
	return impFminusF0
}

def getFminusF0divF0(imp, impF0) {
	return getFdivF0(getFminusF0(imp, impF0), impF0)
}

def getDeltaF(imp) {
	def roi = getRoi(imp)
	def imp2 = imp.duplicate()
	def stack1 = imp.getStack()
	def stack2 = imp2.getStack()
	stack1.deleteSlice(1);
	stack2.deleteSlice(stack1.getSize())
	def ic = new ImageCalculator()
	def impDeltaF = ic.run("Subtract create stack", imp2, imp)
	if (roi)
		impDeltaF.setRoi(roi)
	return impDeltaF
}

def getRoi(imp) {
	def roi = imp.getRoi()
	imp.killRoi()
	return roi
}


utils = loadLib("/scripts/BAR/Utils.groovy")
if (!utils.isSingleChannelTimeseq(imp, uiservice))
	return

if (output.contains("All"))
	output = "F/F0 Delta-F F-F0 (F-F0)/F0"

nameExt = utils.splitFileExtension(imp.getTitle())
f0 = utils.projAVG(imp, 1, f0end)

if (output.contains("F/F0")) {
	f_div_f0 = getFdivF0(imp, f0)
	f_div_f0.setTitle(nameExt[0] +"_FdivF0."+ nameExt[1])
	utils.displayWithViridis(f_div_f0)
}

if (output.contains("Delta-F")) {
	delta_f_up = getDeltaF(imp)
	delta_f_up.setTitle(nameExt[0] +"_DeltaF_up."+ nameExt[1])
	utils.displayWithViridis(delta_f_up)
}

if (output.contains("F-F0")) {
	f_minus_f0 = getFminusF0(imp, f0)
	f_minus_f0.setTitle(nameExt[0] +"_FminusF0."+ nameExt[1])
	utils.displayWithViridis(f_minus_f0)
}

if (output.contains("(F-F0)/F0")) {
	f_minus_f0_div_f0 = getFminusF0divF0(imp, f0)
	f_minus_f0_div_f0.setTitle(nameExt[0] +"_FminusF0divF0."+ nameExt[1])
	utils.displayWithViridis(f_minus_f0_div_f0)
}
