/* BARlib.ijm
 * IJ BAR: https://github.com/tferr/Scripts#scripts
 *
 * Common BAR library (http://imagej.net/BAR#BAR_lib) to be placed in BAR/lib.
 * This file can host functions to be used across your scripts. To load these
 * scripting additions, append the following to your macro files:
 *
 *     libPath = call('bar.Utils.getLibDir') + 'BARlib.ijm';
 *     libContents = File.openAsString(libPath);
 *     call('ij.macro.Interpreter.setAdditionalFunctions', libContents);
 *
 * Once a new instance of the macro interpreter is initiated (http://imagej.net/BAR#FAQ),
 * you can confirm the availability of new additions by running:
 *
 *     confirmLoading();
 */


/** Acknowledges accessibility to this file */
function confirmLoading() {
	showMessage("BAR lib successfully loaded!");
}

/** Returns text from the system clipboard or an empty string if no text was found */
function getClipboardText() {
	return String.paste; //IJM already has a built-in function for this task
}

/** 
 *  Returns a random string of the specified length. If the boolean flag spacers
 *  is true, hyphens are used to improve readability of randomized sequence
 */
function randomString(length, spacers) {
	template = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	nChars = lengthOf(template);
	string = "";
	for (i=0; i<length; i++) {
		idx = maxOf(0, round(random()*nChars-1));
		string += substring(template, idx, idx+1);
		if (spacers && i%5==0) string += "_";
	}
	return string;
}

/** Returns a random UUID using java.util.UUID */
function randomUUID() {
	return call("java.util.UUID.randomUUID");
}


/** Smooths 1D data according to the specified window */
function getSimpleMovingAverage(values, window) {
	if (window<1) return values;
	n = values.length;
	svalues = newArray(n); // all items in array are initialized to zero
	for (i=0; i<n; i++) {
		for (nw=0, j=maxOf(0, i-window); j<minOf(n, i+window); j++) {
			svalues[i] += values[j]; nw++;
		}
		svalues[i] /= nw;
	}
	return svalues;
}

/** Returns the greatest common divisor between 2 numbers */
function gcd(a, b) {
	if (b==0)
		return a;
	else
		return gcd(b, a%b);
}

/** Returns the greatest common divisor between 2 numbers using Commons Math */
function gcdCommons(a, b) {
	return call("org.apache.commons.math3.util.ArithmeticUtils.gcd", a, b);
}

/** Returns surface area and volume of a sphere of radius r */
function sphereCalc(r) {
	sph_area = 4.0 * PI * (r*r);
	sph_vol = 4.0/3.0 * PI * (r*r*r);
	return newArray(sph_area, sph_vol);
}
