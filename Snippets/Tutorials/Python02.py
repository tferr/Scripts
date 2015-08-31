# Python02.py
# IJ BAR: https://github.com/tferr/Scripts#scripts
####################################################
# 2. If statements and for loops
####################################################

a,b = 10,20
my_list = [1, 2, 3, 4]

# An if statement: Indentation is critical in python!
if a in my_list:
	print "Line 12:", a, "is in the list"
elif b in my_list:
	print "Line 14:", b, "is in the list"
else:
    print "Line 16:", "Neither", a, "or", b, "are in in the list"

# A simple for loop:
print "Line 19:", "Integers between 5 and 10:"
for i in range(5, 10):
    print "  Line 21 (for loop):", i

# Looping through lists:
# NB: For Python's buil-in functions (such as <str()> see
# https://docs.python.org/2/library/functions.html
print "Line 26:", "A cuba libre takes:"
for ingredient in ["Rum", "Coke", "Lime"]:
    print "  Line 29 (for loop):", ingredient

## To address later on: enumerate(). See documentation at
## https://docs.python.org/2/library/functions.html#enumerate
