//@UIService uiservice

/* BARlib.groovy
 * IJ BAR: https://github.com/tferr/Scripts#scripts
 *
 * Common BAR library (http://imagej.net/BAR#BAR_lib) to be placed in BAR/lib. This file
 * can host functions/methods to be used across your scripts. To load these scripting
 * additions, append the following to your Groovy files:
 *
 *    import bar.Utils
 *    file = new File(Utils.getLibDir() + "BARlib.groovy")
 *    BARlib = new GroovyClassLoader().parseClass(file)
 *
 * Then, initiate the BARlib class, calling methods as usual:
 *    lib = BARlib.newInstance()
 *    lib.confirmLoading()
 */

import bar.Utils

class BARlib {

	/** Acknowledges accessibility to this file */
	def confirmLoading() {
		uiservice.showDialog("BAR lib successfully loaded!")
	}

	/** Returns text from the system clipboard or an empty string if no text was found */
	def getClipboardText() {
		Utils.getClipboardText()
	}

	/** Returns a random uuid */
	def randomString() {
		java.util.UUID.randomUUID().toString()
	}

	/**
	 * Smooths 1D data according to the specified window. Returns
	 * the original data if window is not a positive integrer.
	 */
	def getSimpleMovingAverage(values, window) {
		if (window<1 || !(window instanceof Integer))
			return values
		def svalues = []
		def lastI = values.size()-1
		for (i in 0..lastI) {
        	svalues[i] = 0; def n = 0
        	for (j in Math.max(0, i-window)..Math.min(lastI, i+window)) {
				svalues[i] += values[j]; n++
			}
			svalues[i] /= n
		}
		return svalues
	}

	/** Returns the greatest common divisor between 2 numbers */
	def gcd(a, b) {
		(b==0) ? a : gcd(b, a%b)
	}

	/** Returns the greatest common divisor between 2 numbers using Commons Math */
	def gcdCommons(a, b) {
		org.apache.commons.math3.util.ArithmeticUtils.gcd(a,b)
	}

	/** Returns surface area and volume of a sphere of radius r */
	def sphereCalc(r) {
		def sph_area = 4.0 * Math.PI * (r*r)
		def sph_vol = 4.0/3.0 * Math.PI * (r*r*r)
		[sph_area, sph_vol]
	}

}

