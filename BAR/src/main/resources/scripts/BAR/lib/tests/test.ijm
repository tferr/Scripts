// test.ijm
// IJ BAR: https://github.com/tferr/Scripts#scripts
//
// Tests correct loading of BARlib


libPath = call('bar.Utils.getLibDir') + 'BARlib.ijm';
libContents = File.openAsString(libPath);
call('ij.macro.Interpreter.setAdditionalFunctions', libContents);


function test(returnValue) {
	common = newArray("Test "+ counter++);
	final = Array.concat(common, returnValue);
	Array.print(final);
}

var counter = 1;

test("assumed");
test("assumed");

test(randomString(20, true));
values = newArray(1,2,3,4);
test(getSimpleMovingAverage(values,2));
test(gcd(4,2));
test(gcdCommons(4,2));
test(sphereCalc(10));
