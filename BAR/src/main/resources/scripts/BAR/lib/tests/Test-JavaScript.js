// Test-JavaScript.js
// IJ BAR: https://github.com/tferr/Scripts#scripts
//
// Tests correct loading of BARlib


importClass(Packages.bar.Utils);
load(Utils.getLibDir() +"BARlib.js");


function test(returnValue) {
	print("Test " + (counter++) +": "+ returnValue);
}


lib = new BARlib();
counter = 1;

test(typeof lib.confirmLoading === "function");
test(typeof lib.getClipboardText === "function");
test(lib.randomString());
test(lib.getSimpleMovingAverage(new Array(1.0,2.0,3.0,4.0),2));
test(lib.gcd(4,2));
test(lib.gcdCommons(4,2));
test(lib.sphereCalc(10));
