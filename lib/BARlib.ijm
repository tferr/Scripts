/* BARlib.ijm
 * IJ BAR: https://github.com/tferr/Scripts#scripts
 *
 * Common BAR library to be placed in BAR/lib. This file hosts functions to
 * be used across all your macros. To add these scripting additions, run the
 * following _before_ running your macro files:
 *
 *     libPath = call('bar.Utils.getLibDir') + 'BARlib.ijm';
 *     libContents = File.openAsString(libPath);
 *     call('ij.macro.Interpreter.setAdditionalFunctions', libContents);
 *
 * Once a new instance of the macro interpreter is initiated, you can confirm
 * the availability of new additions by running:
 *
 *     confirmLoading();
 */


///////////  UTILITIES  ///////////
/** Acknowledges accessibility to this file */
function confirmLoading() {
	showMessage("BAR lib successfully loaded!");
}

/** Returns text from the system clipboard or an empty string if no text was found */
function getCliboardText() {
	return String.paste; //IJM already has a built-in function for this
}

/** Returns a random uuid */
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


///////////  CALCULATIONS  ///////////
/** Smooths 1D data according to the specified window */
function getSimpleMovingAverage(values, window) {
	if (window<1) return values;
	svalues = newArray(values.length);
	for (i=0; i<values.length; i++) {
		svalues[i] = 0; n = 0;
		for (j=maxOf(0, i-window); j<minOf(values.length, i+window); j++) {
			svalues[i] += values[j]; n++;
		}
		svalues[i] /= n;
	}
	return svalues;
}

/** Returns the greatest common divisor between 2 numbers */
function gcd(a, b) {
	if (b==0) return a;
	return gcd(b, a%b);
}

/** Returns surface area and volume of a sphere of radius r */
function sphereCalc(r) {
	sph_area = 4.0 * PI() * (r*r);
	sph_vol = 4.0/3.0 * PI() * (r*r*r);
	return newArray{sph_area, sph_vol};
}
