# Python03.py
# IJ BAR: https://github.com/tferr/Scripts#scripts
####################################################
# 3. Import Jython modules and Java classes
####################################################

# Importing a Jython module:
import math
print "[Line 09]", "Python PI=", math.pi # https://docs.python.org/2/library/math.html


# Importing a Java class
import java.lang.Math
print "[Line 14]", "  Java PI=", java.lang.Math.PI # http://javadoc.imagej.net/Java7/index.html?java/lang/Math.html


# The import statement can occur anywhere in a script
my_drinks = ["beer", "gin", "rum", "vodka"]
import random
print "[Line 20]", "A random drink:", random.choice(my_drinks) # https://docs.python.org/2/library/random.html


# The import statement comes in a variety of forms:
import ij.IJ
print "[Line 25]", "We're running IJ", ij.IJ.getVersion() # http://javadoc.imagej.net/ImageJ1/index.html?ij/IJ.html

from ij import IJ
print "[Line 28]", "We're still running IJ", IJ.getVersion()

from ij import IJ as myFavoriteProgram
print "[Line 31]", "My favorite program: IJ", myFavoriteProgram.getVersion()

## To know more: http://www.jython.org/jythonbook/
