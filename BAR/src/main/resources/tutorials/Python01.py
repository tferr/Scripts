# Python01.py
# IJ BAR: https://github.com/tferr/Scripts#scripts
####################################################
# 1. Basics
####################################################

# This is a comment (typically single line)
"""
Tripple quotes are used for large multi-line comments, and
typically to document functions and modules (Docstrings)
"""

# Python features a built-in print statement that outputs
# to the Script Editor's console ("Show Output" button). It
# accepts one or more arguments (a string, number, object,
# ...), separated by commas
# NB: In Python 3, the print statement became a function,
# and needs to be flanked by brackets. For details, see
# https://docs.python.org/3.0/whatsnew/3.0.html#print-is-a-function
print "Line 20:", "Hello!"

# Math works as expected:
a = 1+1		# Sum
b = 2*4		# Multiplication
c = 2**4	# Exponentiation (2^14 = 16)
print "Line 26:", a, b, c

# Division is slightly special:
print "Line 29:", "Integer division: 3/4=", 3/4
print "Line 30:", "Float division: 3/4=", 3.0/4.0

# BTW, you cal also multiply strings. Try this:
print "Line 33:", 3*"Hello!"

# Operators:
print "Line 36:", "(a is b):", (a is b)
print "Line 37:", "a==2:", a==2
print "Line 38:", "a is not b and b!=2:", a is not b and b!=2

# Working with lists: Lists are indexed sequences of items. The
# 1st index (item position) is zero, the 2nd index is one, etc.
my_list =[]				# An empty list
my_list = [1,2,3,4,5,6]	# A prefilled list
my_list[0] = "string"	# Assign new value to an initialized index
my_list.append(7)		# Append new item to list
print "Line 46:", my_list
my_list.pop()			# Remove last element from list
print "Line 48:", my_list

# Working with dictionaries: Dictionaries store mappings between
# <keys> and <values>. Keys can be unique strings or numbers (or
# any immutable type). Values can be anything: a string, a number,
# an expression, or any other object.
my_dict = {}								# An empty dictionary
my_dict = {"John":1234, "my_list":my_list}	# A prefilled dictionary
my_dict['Cubalibre?'] = "Rum and Coke"		# Add a key:value pair

# Create a list from a generator expression (the <value>) and
# associate it to a variable (the <key>)
a = "Bit-depths"
my_dict[a] = [2**x for x in (1, 8, 16, 32)]

# Looking up values:
print "Line 64:", my_dict["Cubalibre?"]
print "Line 65:", my_dict["my_list"]
print "Line 66:", my_dict[a]
try:
    my_var = my_dict["Mojito"]
except KeyError:
    print "Line 70:", "Bummer: Mojito is not in the dictionary"

## To address later on: Tuples and Sets. See documentation at
## https://docs.python.org/2.7/tutorial/datastructures.html
