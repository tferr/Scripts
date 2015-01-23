# BARlib.py
# IJ BAR: https://github.com/tferr/Scripts#scripts
#
# Common BAR library to be placed in BAR/lib. This file hosts methods to
# be used across all your scripts. To add these scripting additions, append
# the following to your Jython scripts:
#    import bar, sys
#    sys.path.append(bar.Utils.getLibDir())
#    import BARlib as lib
# Then, call functions as usual:
#    lib.confirmLoading()
#


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
def gcd(a,b):
    """Returns the greatest common divisor between 2 numbers"""
    while(b): a,b = b,a%b
    return a

def sphereCalc(r):
    """Returns surface area and volume of a sphere of radius r"""
    import math
    sph_area = 4*math.pi*(r**2)
    sph_vol = 4/3*math.pi*(r**3)
    return (sph_area, sph_vol)
