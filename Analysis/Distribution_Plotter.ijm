/* Distribution_Plotter.ijm
 * https://github.com/tferr/Scripts/#scripts
 *
 * Plots cumulative (left Y-axis) and relative frequencies (right Y-axis) using values
 * from the Results table. To increase readability, height of modal class is matched to
 * the 50th-percentile. A Gaussian curve (normal distribution) is fitted to the histogram.
 * Requires at least IJ 1.47.
 *
 * TF, 2009.11  Initial version
 * TF, 2014.01  Added median, methods for optimal number of bins and fit to Normal distribution
 */

// Uncomment the two lines below to obtain demo values
    //run("Blobs (25K)"); setAutoThreshold;
    //run("Analyze Particles...", "size=0-Infinity circularity=0.00-1.00 show=Nothing display");

countNaN = max2 = y2scale = dgs = 0; y2unit = "";
nRes = nResults; values = newArray(nRes); cumFreq = newArray(nRes);

if (nRes==0)
	exit("The Results table is empty.");

tabChoices = newArray(3);
tabChoices[0] = 'Number of values';
tabChoices[1] = 'Relative frequency (%)';
tabChoices[2] = 'Relative frequency (fractions)';
binChoices = newArray(5);
binChoices[0] = "Square-root";
binChoices[1] = "Sturges";
binChoices[2] = "Scott (IJ's default)";
binChoices[3] = "Freedman-Diaconis";
binChoices[4] = "Specify manually below:";

Dialog.create('Distribution Plotter');
  prmtrs = getMeasurements();
  Dialog.addChoice("Parameter:", prmtrs, "Area");
  Dialog.addChoice('Tabulate:', tabChoices, tabChoices[1]);
  Dialog.addRadioButtonGroup("Automatic binning:", binChoices, 3, 2, binChoices[3]);
  Dialog.addSlider("Bins:", 1, nRes, nRes/2);
  Dialog.addMessage(nRes +" data points in Results table");
Dialog.show;
  parameter = Dialog.getChoice;
  yAxis = Dialog.getChoice;
  autoBin = Dialog.getRadioButton;
  userBins = minOf(Dialog.getNumber, nRes);

if (yAxis==tabChoices[0]) {
	cumFreq[0] = 1; yLimit = nRes;
} else if (yAxis==tabChoices[1]) {
	cumFreq[0] = 100/nRes; yLimit = 100;
} else {
	cumFreq[0] = 1/nRes; yLimit = 1;
}

for (i=0; i<nRes; i++) {
	values[i] = getResult(parameter, i);
	if (isNaN(values[i])) countNaN++;
}
if (countNaN==nRes)
	exit("No data for \""+ parameter +"\" in the Results table");

for (i=1; i<nRes; i++)
	cumFreq[i] = cumFreq[i-1] + cumFreq[0];

Array.sort(values);
Array.getStatistics(values, min1, max1, mean1, stdDev1);

if (autoBin==binChoices[4]) { // User-defined
	nBins = userBins;
} else {
	if (autoBin==binChoices[0]) // Square-root
		criteria = sqrt(nRes);
	else if (autoBin==binChoices[1]) // Sturges
		criteria = -floor( -((log(nRes)/log(2) + 1)) );
	else if (autoBin==binChoices[2]) // Scott
		criteria = -floor( -((3.5*stdDev1)/pow(nRes, 1/3)) );
	else  // Freedman-Diaconis
		criteria = -floor( -((2*(values[(0.75*nRes)-1]-values[(0.25*nRes)-1]))/pow(nRes, 1/3)) );
	nBins = -floor(-((max1-min1)/criteria));
}
  if (isNaN(nBins)) nBins = 1; //e.g., when SD=0;
  binWidth = (max1-min1)/nBins;
  bins = getBinsArray(nBins, binWidth); // get bin center and draw bin limit
  freq = getHistoCounts(bins, nBins, parameter); // get counts and draw 2-Axis scale

  Array.getStatistics(freq, min2, max2, mean2, stdDev2);
  for (i=0; i<bins.length; i++){
	  if (yAxis==tabChoices[0]) {
		  freq[i] = ((nRes/2)/max2)*freq[i]; y2scale = 1;
		  asa = (nRes/2)/max2;
	  } else if (yAxis==tabChoices[1]) {
		  freq[i] = (50/max2)*freq[i]; y2scale = 100/nRes; y2unit = "%";
		  asa = (nRes/2)/max2;//(50/max2);
	  } else {
		  freq[i] = (0.5/max2)*freq[i]; y2scale = 1/nRes; dgs = 2;
		  asa = (0.5/max2);
	  }
	  bins[i] = bins[i]-(binWidth/2); // list bin centers in plot table
  }

saveSettings();
removeGrids = eval("script", "PlotWindow.noGridLines=true;");
Plot.create(parameter +" Distribution", parameter, yAxis);
  Plot.setLimits(min1, max1, 0, yLimit);
  legend = "Mean: "+ d2s(mean1,2) +",  Median: "+ d2s(getMedian(),2) +",  SD: "+ d2s(stdDev1,2) +",  N: "+ nRes;
  Plot.addText(legend, 0,0);
  Plot.setColor("blue");
  Plot.add("dots", bins, freq);
  plotNormal(mean1, stdDev1);
  Plot.setLineWidth(2);
  drawHistogram(bins, freq);
  Plot.setLineWidth(2);
  Plot.setColor("red");
  Plot.add("line", values, cumFreq);
  Plot.setLineWidth(1);
  drawGridlines();
  restoreSettings();
Plot.show;

function plotNormal(mean, sd) {
	scale = binWidth * yLimit * (nRes/2)/max2;
	width = call("ij.Prefs.get", "pp.width", 350);
	drawingStep = (max1-min1)/width;
	for (i=0; i<width; i++) {
	    x = min1 + i*drawingStep;
		y = scale * ( (1/(sd*sqrt(2*PI))) * ( exp( -(((x-mean)*(x-mean))/((2*sd*sd))) ) ));
		Plot.drawLine(x, y, x, y);
	}
}

function getMeasurements() {
    list = split(String.getResultsHeadings);
    return Array.sort(list);
}

function getBinsArray(n, width) {
    bins = newArray(n);
    for (i=0; i<bins.length; i++)
       bins[i] = i * width + min1 + width;
    return bins;
}

function getMedian() { // values[] is already sorted
	if (nRes%2==0)
		median = (values[nRes/2] + values[nRes/2 -1])/2;
	else
		median = values[nRes/2];
	return median;
}

function getHistoCounts(binArray, nB, colN) {
   counts = newArray(nB);
   Array.fill(counts, 0);
   for (i=0; i<nRes; i++) {
        value = getResult(colN, i);
        if (value<=binArray[0])
            counts[0] = counts[0]+1;
        if (value>binArray[nB-1])
            counts[nB-1] = counts[nB-1]+1;
        for (j=1; j<counts.length; j++)
             if (value<=binArray[j] && value>binArray[j-1])
                 counts[j]++;
    }
   return counts;
}

function drawHistogram(binCenters, frequencies) {
    for (i=0; i<binCenters.length; i++) {
        x1 = binCenters[i]-(binWidth/2);
        x2 = binCenters[i]+(binWidth/2);
        Plot.drawLine(x1, frequencies[i], x2, frequencies[i]);
        Plot.drawLine(x1, 0, x1, frequencies[i]);
        Plot.drawLine(x2, 0, x2, frequencies[i]);
    }
}

function drawGridlines() {
    // Draw left Y-axis labels
    Plot.setColor("red");
    Plot.addText(" "+ d2s(yLimit*3/4,dgs) +""+ y2unit, 0, 0.25);
    Plot.addText(" "+ d2s(yLimit/2,dgs) +""+ y2unit, 0, 0.50);
    Plot.addText(" "+ d2s(yLimit/4,dgs) +""+ y2unit, 0, 0.75);

    // Draw right Y-axis labels
    Plot.setColor("blue"); Plot.setJustification("right");
    Plot.addText(d2s(2*max2*y2scale,dgs)+y2unit, 1, 0.25);
    Plot.addText(d2s(max2*y2scale,dgs)+y2unit, 1, 0.50);
    Plot.addText(d2s(max2/2*y2scale,dgs)+y2unit, 1, 0.75);

    width = call("ij.Prefs.get", "pp.width", 350);
	drawingStep = (max1-min1)/width;
	for (i=0; i<width; i++) {
	    x = min1 + i*drawingStep;
        b = i*255/width;
        r = 255 - b;
        Plot.setColor("#"+ pad(toHex(r)) +"00"+ pad(toHex(b)));
        Plot.drawLine(x, cumFreq[(nRes*3/4)-1], x, cumFreq[(nRes*3/4)-1]);
        Plot.drawLine(x, cumFreq[(nRes/2)-1], x, cumFreq[(nRes/2)-1]);
        Plot.drawLine(x, cumFreq[(nRes/4)-1], x, cumFreq[(nRes/4)-1]);
    }
}

function pad(n) {
  n= toString(n); if (lengthOf(n)==1) n= "0"+n; return n;
}
