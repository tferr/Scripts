/* Distribution_Plotter.ijm
 * https://github.com/tferr/Scripts/#scripts
 *
 * Plots cumulative (left Y-axis) and relative frequencies (right Y-axis) using values
 * from the Results table. To increase readability, height of modal class is matched to
 * the 50th-percentile. Requires at least IJ 1.47.
 *
 * TF, 2014.01 Modernized code
 */

// Uncomment the two lines below to obtain demo values
    //run("Blobs (25K)"); setAutoThreshold;
    //run("Analyze Particles...", "size=0-Infinity circularity=0.00-1.00 show=Nothing display");

unit = "Image Units"; graphTtl = ""; xTitle = "";
sqSign= fromCharCode(178); countNaN = max2 = y2scale = y2unit = dgs = 0;
nRes = nResults; values = newArray(nRes); cumFreq = newArray(nRes);

if (nRes==0)
	exit("The Results table is empty.");

prmtrs = getMeasurements();
Dialog.create('Distribution Plotter');
  Dialog.addChoice("Parameter:", prmtrs, "Area");
  Dialog.addChoice('Frequencies as:', newArray('Percentage','Fraction','Count'));
  Dialog.addChoice('Trim curves at:', newArray("Max. (Do not trim)", 'Q1', 'Q2 (Median)', 'Q3', 'P90', 'P95'));
  Dialog.addSlider("Number of bins:", 1, nRes, nRes/2);
  Dialog.addCheckbox("Use information from frontmost image", true);
Dialog.show;
  parameter = Dialog.getChoice;
  yAxis = Dialog.getChoice;
  xAxis = split(Dialog.getChoice);
  nBins = minOf(Dialog.getNumber, nRes);
  info = Dialog.getCheckbox;

if (nImages!=0 && info) {
	graphTtl = graphTtl+" for "+getTitle;
	getPixelSize(unit, pW, pH);
}

if (parameter== "Other...")
	parameter = getString("Parameter:", "");
if (parameter=="Perim." || parameter=="Feret" || parameter=="Length")
	xTitle = unit;
if (parameter=="Area")
	xTitle = unit+sqSign;

if (yAxis=="Count") {
	cumFreq[0] = 1; yLimit = nRes; yTick = nRes/80;
} else if (yAxis=="Fraction") {
	cumFreq[0] = 1/nRes; yLimit = 1; yTick = 0.015;
} else if (yAxis=="Percentage") {
	cumFreq[0] = 100/nRes; yLimit = 100; yTick = 1.5;
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

if (xAxis[0]=="Q1")
	max1 = values[(nRes/4)-1];
else if (xAxis[0]=="Q2")
	max1 = values[(nRes/2)-1];
else if (xAxis[0]=="Q3")
	max1 = values[(0.75*nRes)-1];
else if (xAxis[0]=="P90")
	max1 = values[(0.90*nRes)-1];
else if (xAxis[0]=="P95")
	max1 = values[(0.95*nRes)-1];
else //if(xAxis=="Max")
	max1 = values[nRes-1];

Plot.create(parameter +" Distribution"+ graphTtl, parameter +" ["+ xTitle +"]", yAxis);
  Plot.setLimits(0, max1, 0, yLimit);
  legend = "Mean: "+ d2s(mean1,2) +"  SD: "+ d2s(stdDev1,2)
         + "  Min. "+ d2s(min1,2) +"  "+ xAxis[0] +" "+ d2s(max1,2);
  Plot.addText(legend, 0,0);

  int = max1/nBins;
  bins = getBinsArray(nBins, int); // get bin center and draw bin limit
  freq = getHistoCounts(bins, nBins, parameter); // get counts and draw 2-Axis scale

  Array.getStatistics(freq, min2, max2, mean2, stdDev2);
  for (i=0; i<nBins; i++){
	  if (yAxis=="Fraction") {
		  freq[i] = (0.5/max2)*freq[i]; y2scale = 1/nRes; dgs = 1;
	  } else if (yAxis=="Percentage") {
		  freq[i] = (50/max2)*freq[i]; y2scale = 100/nRes; y2unit = "%"; dgs = 0;
	  } else if (yAxis=="Count") {
		  freq[i] = ((nRes/2)/max2)*freq[i]; y2scale = 1; dgs = 0;
	  }
	  bins[i] = bins[i]-(int/2); // plot curve using centers of bins
  }

  drawScales();
  Plot.setLineWidth(2);
  Plot.setColor("red");
  Plot.add("line", bins, freq);
  Plot.setColor("blue");
  Plot.add("line", values, cumFreq);
  Plot.setLineWidth(1);
Plot.show;


function getMeasurements() {
    list = split(String.getResultsHeadings);
    nList = list.length;
    Array.sort(list);
    parameters = newArray(nList+1);
    for (i=0; i<nList; i++)
       parameters[i] = list[i];
    parameters[nList] = "Other...";
    return parameters;
}

function getBinsArray(nInt, intBin) {
    bins = newArray(nInt);
    for (i=0; i<nInt; i++) {
       bins[i] = (i*intBin)+intBin;
       if (i<nInt-1)
           Plot.drawLine(bins[i], 0, bins[i], yTick);
    }
    return bins;
}

function getHistoCounts(binArray, nB, colN) {
   counts = newArray(nB);
   Array.fill(counts, 0);
   for (i=0; i<nRes; i++) {
        value = getResult(colN,i);
        if (value<=binArray[0])
            counts[0] = counts[0]+1;
        if (value>binArray[nB-1])
            counts[nB-1] = counts[nB-1]+1;
        for (j=1; j<nB; j++)
             if (value<=binArray[j] && value>binArray[j-1])
                 counts[j]++;
    }
   return counts;
}

function drawScales() {
    // Draw left Y-axis labels
    Plot.setColor("#0000ff");
    Plot.addText(" "+ d2s(cumFreq[(nRes*3/4)-1],dgs) +""+ y2unit, 0, 0.25);
    Plot.addText(" "+ d2s(cumFreq[(nRes/2)-1],dgs) +""+ y2unit, 0, 0.50);
    Plot.addText(" "+ d2s(cumFreq[(nRes/4)-1],dgs) +""+ y2unit, 0, 0.75);

    // Draw right Y-axis labels
    Plot.setColor("#ff0000"); Plot.setJustification("right");
    Plot.addText(d2s(2*max2*y2scale,dgs)+y2unit, 1, 0.25);
    Plot.addText(d2s(max2*y2scale,dgs)+y2unit, 1, 0.50);
    Plot.addText(d2s(max2/2*y2scale,dgs)+y2unit, 1, 0.75);

    // Draw gridlines
    for (x=max1; x>=0; x--) {
        rgbR = x*255/max1;
        rgbB = 255 - rgbR;
        Plot.setColor("#"+ pad(toHex(rgbR)) +"00"+ pad(toHex(rgbB)));
        Plot.drawLine(x, cumFreq[(nRes*3/4)-1], x, cumFreq[(nRes*3/4)-1]);
        Plot.drawLine(x, cumFreq[(nRes/2)-1], x, cumFreq[(nRes/2)-1]);
        Plot.drawLine(x, cumFreq[(nRes/4)-1], x, cumFreq[(nRes/4)-1]);
    }
}

function pad(n) {
  n= toString(n); if (lengthOf(n)==1) n= "0"+n; return n;
}
