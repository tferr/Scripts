# BARlib.rb
# IJ BAR: https://github.com/tferr/Scripts#scripts
#
# Common BAR library (https://github.com/tferr/Scripts/tree/master/lib#lib) to be
# placed in BAR/lib. This file can host functions to be used across your scripts.
# To load these scripting additions, append the following to your JRuby files:
#
#    java_import "bar.Utils"
#    require "#{Utils.getLibDir}" + "BARlib.rb"
#
# Then, initiate the BARlib class, calling methods as usual:
#    lib = BARlib.new()
#    lib.confirmLoading
#

class BARlib
	##### Utilities #####
	# Acknowledges accessibility to this file
	def confirmLoading
		java_import "ij.IJ"
		IJ.showMessage "BAR lib successfully loaded!"
	end

	# Returns text from the system clipboard or an empty string if no text was found
	def getClipboardText
		java_import "bar.Utils"
		Utils.getClipboardText
	end

	# Returns a random uuid
	def randomString
		java_import "java.util.UUID"
		UUID.randomUUID().toString()
	end


	##### CALCULATIONS #####
	# Smooths 1D data according to the specified window. Returns the original data
	# if window is not a positive integrer. http://stackoverflow.com/a/27895529
	# explains the rationale for this implementation
	def getSimpleMovingAverage(values, window)
		if (window>0) && (window.kind_of? Integer)
			values.each_cons(window).map { |v| (v.reduce(&:+).to_f/window) }
		else
			values
		end
	end

	# Returns the greatest common divisor between 2 numbers
	def gcd(a, b)
		(b==0) ? a : gcd(b, a%b)
	end

	# Returns the greatest common divisor between 2 numbers using Commons Math
	def gcdCommons(a, b)
		java_import "org.apache.commons.math3.util.ArithmeticUtils"
		ArithmeticUtils.gcd(a,b);
	end

	# Returns surface area and volume of a sphere of radius r
	def sphereCalc(r)
		sph_area = 4 * Math::PI * r**2
		sph_vol = 4/3.0 * Math::PI * r**3
		[sph_area, sph_vol]
	end
end
