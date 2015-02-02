/* BARlib.js
 * IJ BAR: https://github.com/tferr/Scripts#scripts
 *
 * Common BAR library (https://github.com/tferr/Scripts/tree/master/lib#lib) to be
 * placed in BAR/lib. This file can host functions to be used across your scripts.
 * To load these scripting additions, append the following to your JavaScript files:
 *
 *     importClass(Packages.bar.Utils);
 *     load(Utils.getLibDir() + "BARlib.js");
 *     lib = new BARlib();
 *
 * Then, call functions using, e.g.:
 *     lib.confirmLoading();
 */

function BARlib() {

	///////////  UTILITIES  ///////////
	/** Acknowledges accessibility to this file */
	this.confirmLoading = function() {
		importClass(Packages.ij.IJ);
		IJ.showMessage("BAR lib successfully loaded!");
	}

	/** Returns text from the system clipboard or an empty string if no text was found */
	this.getClipboardText = function() {
		importClass(Packages.bar.Utils);
		return Utils.getClipboardText();
	}

	/** Returns a random uuid */
	this.randomString = function() {
		return java.util.UUID.randomUUID().toString();
	}

	///////////  CALCULATIONS  ///////////
	/** Smooths 1D data according to the specified window */
	this.getSimpleMovingAverage = function(values, window) {
		if (window<1) return values;
		svalues = new Array();
		for (var i=0; i<values.length; i++) {
			svalues[i] = 0; n = 0;
			for (j=Math.max(0, i-window); j<Math.min(values.length, i+window); j++) {
				svalues[i] += values[j]; n++;
			}
			svalues[i] /= n;
		}
		return svalues;
	}

	/** Returns the greatest common divisor between 2 numbers */
	this.gcd = function(a, b) {
		if (b==0)
			return a;
		else
			return this.gcd(b, a%b);
	}

	/** Returns the greatest common divisor between 2 numbers using Commons Math */
	this.gcdCommons = function(a, b) {
		importClass(Packages.org.apache.commons.math3.util.ArithmeticUtils);
		return ArithmeticUtils.gcd(a,b);
	}

	/** Returns surface area and volume of a sphere of radius r */
	this.sphereCalc = function(r) {
		sph_area = 4.0 * Math.PI * (r*r);
		sph_vol = 4.0/3.0 * Math.PI * (r*r*r);
		return [sph_area, sph_vol];
	}

}
