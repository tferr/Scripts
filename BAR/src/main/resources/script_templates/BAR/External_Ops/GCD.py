# @BAR bar
# @float a
# @float b

# This script distributed through the BAR update site[1] exemplifies how Ops[1]
# can be added outside the core imagej-ops project. In this case, it exemplifies
# how an existing function provided by a BAR lib[2] can be converted to an Op.
# The full tutorial by Mark Hiner is available at http://imagej.net/Adding_new_ops
#
# [1] http://imagej.net/BAR
# [2] http://imagej.net/ImageJ_Ops

print("Greatest common divisor of " + str(a) + " and " + str(b) + " is: " + str(bar.gcd(a, b)))
