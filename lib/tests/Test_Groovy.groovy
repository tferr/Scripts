// Test_Groovy.groovy
// IJ BAR: https://github.com/tferr/Scripts#scripts
//
// Tests correct loading of BARlib


import bar.Utils
file = new File(Utils.getLibDir() + "BARlib.groovy")
BARlib = new GroovyClassLoader(getClass().getClassLoader()).parseClass(file)

def test(returnValue) {
	print("Test " + (counter++) +": "+ returnValue +"\n")
}


lib = BARlib.newInstance()
counter = 1;

test(lib.metaClass.respondsTo(lib, "confirmLoading"))
test(lib.metaClass.respondsTo(lib, "getClipboardText"))
test(lib.randomString())
test(lib.getSimpleMovingAverage([1.0,2.0,3.0,4.0], 2))
test(lib.gcd(4,2))
test(lib.gcdCommons(4,2))
test(lib.sphereCalc(10))
