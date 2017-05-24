#@UIService ui

# BARlib.rb
# IJ BAR: https://github.com/tferr/Scripts
#
# Template BAR library (http://imagej.net/BAR#BAR_lib) to be placed in BAR/lib. This file
# demonstrates how functions/methods in a common file can be shared across your scripts.
# To load such scripting additions, append the following to your JRuby files:
#
#    java_import "bar.Utils"
#    require "#{Utils.getLibDir}" + "BARlib.rb"
#
# Then, initiate the BARlib class, calling methods as usual:
#    lib = BARlib.new()
#    lib.confirmLoading
#
# (See resources in BAR>Help for more details)


class BARlib

	# Acknowledges accessibility to this file
	def confirmLoading
		uiservice.showDialog "BAR lib successfully loaded!"
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

	# Smooths 1D data according to the specified window. Returns the original data
	# if window is not a positive integer
	def getSimpleMovingAverage(values, window)
		if (window<1) || (!window.is_a? Integer)
			return values
		end
		svalues = Array.new(values.length)
		for i in 0..values.length-1
			svalues[i] = 0; n = 0
			for j in [0, i-window].max..[values.length, i+window].min-1
				svalues[i] += values[j]; n += 1
			end
			svalues[i] /= n
		end
		return svalues
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
