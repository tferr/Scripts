# Python04.py
# IJ BAR: https://github.com/tferr/Scripts#scripts
####################################################
# 4. Functions and modules
####################################################

# Functions are defined with <def>. Return values are
# specified by a <return> statement. Here is a function
# without arguments:
def today():
    """This function returns a string of today's date"""
    import datetime		# https://docs.python.org/2/library/datetime.html
    td = datetime.date.today()
    return td

print "[Line 16]", "today() returned", today()
print "[Line 17]", "str(today()) returned", str(today())	# https://docs.python.org/2/library/functions.html?highlight=str()


# A Function with arguments:
def minFunction(a, b):
    """This function returns the smallest of two arguments"""
    if a < b:
        return a
    else:
        return b

print "[Line 28]", minFunction(10,20), "is smaller"


# NB: Python already features a built-in function that returns the
# smallest of two or more arguments. Try this:
print "[Line 33]", "The smallest of all is", min(10,20,-10,"-20")	# https://docs.python.org/2/library/functions.html?highlight=min()


# A practical scenario: Try to import something that may not be present.
# If we cannot find it, we try to define it in some other way so that the
# script can progress. In this example, we are trying to use the function
# <theBestDrinkEver> from the (rather fictitious) module <cocktail>
def myCocktail(a, b):
    """This function mixes a and b in the right amount
       if the fictitious cocktail module is installed """
try:
    from cocktail import theBestDrinkEver
    print "[Line 45]", "theBestDrinkEver was imported"
except ImportError:
    print "[Line 47]", "Please install the cocktail package!"
    def theBestDrinkEver():
        return "Cuba Libre!"

print "[Line 51]", "The best drink ever is " + theBestDrinkEver()


# Modifying global variables from within functions: Python wants you to
# confirm that the function is really expected to modify a global variable
# declared outside its scope, by explicitly using the <global> keyword:
best_rum = "Bacardi"

def set_best_rum_to_brugal():
    global best_rum
    best_rum = "Brugal"

def set_best_rum_to_abuelo():
    best_rum = "Ron Abuelo"

print "[Line 66]", "Best rum: " + best_rum
set_best_rum_to_brugal()
print "[Line 68]", "Best rum: " + best_rum
set_best_rum_to_abuelo() # This will have no effect
print "[Line 70]", "Best rum: " + best_rum

## To know more:
# http://www.jython.org/jythonbook/en/1.0/DefiningFunctionsandUsingBuilt-Ins.html