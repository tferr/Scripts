# BARlib.py
# IJ BAR: https://github.com/tferr/Scripts#scripts
#
# Common BAR library (https://github.com/tferr/Scripts/tree/master/lib#lib) to be
# placed in BAR/lib. This file can host functions to be used across your scripts.
# To load these scripting additions, append the following to your Jython files:
#
#    import bar, sys
#    sys.path.append(bar.Utils.getLibDir())
#    import BARlib as lib
#
# Then, call functions as usual:
#    lib.confirmLoading()
#
# NB: Jython modules will be compiled when loaded. Compiled files are static and
# will not be recompiled automatically when the respective source is modified.
# If you change this file (BARlib.py), you'll have to delete the respective binary
#(BARlib$py.class) to force a new compilation. You can do so, using:
#
#    barUtils.zapCompiledLibs()
#
# (See BAR API ("BAR>About BAR...") for more details)

##### Utilities #####
def confirmLoading():
    """Acknowledges accessibility to this file"""
    from ij import IJ
    IJ.showMessage("BAR lib successfully loaded!")

def getClipboardText():
    """Returns text from the system clipboard or an empty string if no text was found"""
    import bar
    return bar.Utils.getClipboardText();

def randomString():
    """Returns a random uuid"""
    import uuid
    return str(uuid.uuid4())


##### CALCULATIONS #####
def getSimpleMovingAverage(values, window):
    """Smooths 1D data according to the specified window"""
    if (window<1): return values
    svalues = []
    for i in range(len(values)):
        svalues.append(0); n = 0
        for j in xrange(max(0, i-window), min(len(values), i+window)):
            svalues[i] += values[j]; n += 1
        svalues[i] /= n
    return svalues

def gcd(a,b):
    """Returns the greatest common divisor between 2 numbers"""
    while(b): a,b = b,a%b
    return a

def gcdCommons(a, b):
    """Returns the greatest common divisor between 2 numbers using Commons Math"""
    from org.apache.commons.math3.util import ArithmeticUtils
    return ArithmeticUtils.gcd(a,b)

def sphereCalc(r):
    """Returns surface area and volume of a sphere of radius r"""
    import math
    sph_area = 4 * math.pi * r**2
    sph_vol = 4/3.0 * math.pi * r**3
    return (sph_area, sph_vol)
