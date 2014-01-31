/* Distribution_Plotter.ijm
 * https://github.com/tferr/Scripts/#scripts
 *
 * Plots cumulative (left Y-axis) and relative frequencies (right Y-axis) using values
 * from the Results table. To increase readability, height of modal class is matched to
 * the 50th-percentile.
 *
 * TF, 2010.13
 */

// Uncomment the two lines below to obtain demo values
    //run("Blobs (25K)"); setAutoThreshold;
    //run("Analyze Particles...", "size=0-Infinity circularity=0.00-1.00 show=Nothing display");

unit = "Image Units"; graphTtl = ""; xTitle = "";
pmSign = fromCharCode(177); sqSign= fromCharCode(178);
countNaN = max2 = y2scale = y2unit = dgs = 0;
nRes = nResults; values = newArray(nRes); cumFreq = newArray(nRes);

if (nRes==0)
	exit("The \"Results\" table is empty");

prmtrs = getMeasurements();
Dialog.create('Distribution Plotter');
  Dialog.addChoice("Parameter:", prmtrs, "Area");
  Dialog.addChoice('Frequencies as:', newArray('Count','Fraction','Percentage'));
  Dialog.addChoice('Trim curves at:', newArray('Q1','Median','Q3', 'P95', "Don't Trim "));
  Dialog.addNumber("Number of bins:", 10);
  Dialog.addCheckbox("Use info from frontmost image", true);
Dialog.show;
  parameter = Dialog.getChoice;
  yAxis = Dialog.getChoice;
  xAxis = Dialog.getChoice;
  nBins = Dialog.getNumber;
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

run("Profile Plot Options...", "width=350 height=350 "); // remove default gridlines

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

if (xAxis=="Q1")
	max1 = values[(nRes/4)-1];
else if (xAxis=="Median")
	max1 = values[(nRes/2)-1];
else if (xAxis=="Q3")
	max1 = values[(0.75*nRes)-1];
else if (xAxis=="P95")
	max1 = values[(0.95*nRes)-1];
else //if(xAxis=="Max")
	max1 = values[nRes-1];

Plot.create(parameter +" Distribution"+ graphTtl, parameter +" ["+ xTitle +"]", yAxis);
  Plot.setLimits(0, max1, 0, yLimit);
  Plot.addText("Mean "+ pmSign +" StDev: "+ d2s(mean1,2) +" "+ pmSign +" "+ d2s(stdDev1,2), 0.02, 0.05);
  Plot.addText("Min: "+ d2s(min1,2) +"    "+ xAxis +": "+ d2s(max1,2), 0.02, 0.09);

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
  Plot.setLineWidth(2); Plot.setJustification("left");
  Plot.setColor("red");
  Plot.addText("---  Relative Freq.", 0.02, 0.18);
  Plot.add("line", bins, freq);
  Plot.setColor("blue");
  Plot.addText("---  Cumulative Freq.", 0.02, 0.14);
  Plot.add("line", values, cumFreq);
Plot.show;


function getMeasurements(){
    List.setMeasurements;
    list = split(List.getList, "\n");
    nList=list.length;
    Array.sort(list);
    parameters = newArray(nList+1);
    for (i=0; i<nList; i++)
       parameters[i] = substring(list[i], 0, indexOf(list[i], "="));
    parameters[nList] = "Other...";
    return parameters;
}

function getBinsArray(nInt, intBin) {
    Plot.setLineWidth(2); bins=newArray(nInt);
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
    Plot.setColor("blue"); Plot.setJustification("lef"); // Left Y axis
    Plot.addText(" "+d2s(cumFreq[(nRes*3/4)-1],dgs)+""+y2unit, 0, 0.25);
    Plot.addText(" "+d2s(cumFreq[(nRes/2)-1],dgs)+""+y2unit, 0, 0.50);
    Plot.addText(" "+d2s(cumFreq[(nRes/4)-1],dgs)+""+y2unit, 0, 0.75);

    Plot.setColor("red"); Plot.setJustification("right"); // Right Y axis
    Plot.addText(d2s(2*max2*y2scale,dgs)+y2unit, 1, 0.25);
    Plot.addText(d2s(max2*y2scale,dgs)+y2unit, 1, 0.50);
    Plot.addText(d2s(max2/2*y2scale,dgs)+y2unit, 1, 0.75);

    Plot.setColor("lightGray"); Plot.setLineWidth(1); // Grid lines
    Plot.drawLine(0, cumFreq[(nRes*3/4)-1], values[(nRes-1)], cumFreq[(nRes*3/4)-1]);
    Plot.drawLine(0, cumFreq[(nRes/2)-1], values[(nRes-1)], cumFreq[(nRes/2)-1]);
    Plot.drawLine(0, cumFreq[(nRes/4)-1], values[(nRes-1)], cumFreq[(nRes/4)-1]);
}
