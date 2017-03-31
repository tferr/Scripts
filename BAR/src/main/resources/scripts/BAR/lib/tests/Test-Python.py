# Test-Python.py
# IJ BAR: https://github.com/tferr/Scripts#scripts
#
# Tests correct loading of BARlib


import sys, bar
sys.path.append(bar.Utils.getLibDir())
import BARlib as lib

def test(function):
    global counter
    print("Test %s: %s" % (counter, function))
    counter += 1

counter = 1;

test(lib.confirmLoading)
test(lib.getClipboardText)
test(lib.randomString)
test(lib.getSimpleMovingAverage([1.0,2.0,3.0,4.0],2))
test(lib.gcd(4,2))
test(lib.gcdCommons(4,2))
test(lib.sphereCalc(10))
