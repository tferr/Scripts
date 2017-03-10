/* ROI_Color_Coder.ijm
 * IJ BAR: https://github.com/tferr/Scripts#scripts
 * http://imagejdocu.tudor.lu/doku.php?id=macro:roi_color_coder
 *
 * Colorizes ROIs by matching LUT indexes to measurements in the Results table. It is
 * complementary to the ParticleAnalyzer (Analyze>Analyze Particles...), generating
 * particle-size heat maps. Requires IJ 1.47r.
 *
 * Tiago Ferreira, v.5.3 2016.05.10
 */


// assess required conditions before proceeding
requires("1.47r");
if (roiManager("count")==0 || nResults==0) {
    showMessageWithCancel("No images open or the ROI Manager is empty...\n"
        + "Run demo? (Results Table and ROI Manager will be cleared)");
    run("Blobs (25K)");
    setThreshold(126, 255);
    run("Analyze Particles...", "display clear add");
    resetThreshold();
}

setBatchMode(true);
  nROIs= roiManager("count");
  nRES= nResults;

// if no measurements exist, measure existing ROIs
  if (nRES==0) {
      roiManager('Deselect');
      roiManager('Measure');
      nRES= nResults;
  }

// create the dialog prompt
  Dialog.create("ROI Color Coder: "+ getTitle);
    headings= split(String.getResultsHeadings);
    Dialog.addChoice("Measurement:", headings, "Area");
    luts = getList("LUTs");
    Dialog.addChoice("LUT:", luts, "mpl-viridis");
    Dialog.setInsets(12, 0, 0);
    Dialog.addMessage("Contours:");
    Dialog.addNumber("Width:", 0, 0, 4, "pixels  (0 to fill ROIs)");
    Dialog.addSlider("Opacity (%):", 0, 100, 80);
    Dialog.setInsets(12, 0, 0);
    Dialog.addMessage("Legend:");
    getPixelSize(unit, null, null);
    Dialog.addString("Label:", unit+"^2", 10);
    Dialog.addString("Range:", "Min-Max", 10);
    Dialog.setInsets(-35, 213, 0);
    Dialog.addMessage("(e.g., 10-100)");
    Dialog.addNumber("N. of labels:", 5, 0, 3, "(Defines major ticks interval)");
    Dialog.addNumber("Decimal places:", 0, 0, 3, "(Scientific notation if negative)");
    Dialog.addChoice("Ramp size:", newArray("128 pixels", "256 pixels", "512 pixels"), "256 pixels");
    Dialog.addChoice("Font:", newArray("Monospaced", "SansSerif", "Serif"), "SansSerif");
    Dialog.addNumber("Font_size:", 14, 0, 3, "pt");
    Dialog.setInsets(-25, 200, 0);
    Dialog.addCheckbox("Draw tick marks", true);
    if (nROIs!=nRES)
        Dialog.addMessage(nROIs +" ROI(s) in Manager, "+ nRES +" rows in Results table:\n"
                + abs(nROIs-nRES) +" item(s) will be ignored...");
    Dialog.addHelp("https://github.com/tferr/Scripts/blob/master/BAR/src/main/resources/scripts/BAR/Annotation/README.md#roi-color-coder");
  Dialog.show;
      parameter= Dialog.getChoice;
      lut= Dialog.getChoice;
      stroke= Dialog.getNumber;
      alpha= pad(toHex(255*Dialog.getNumber/100));
      label= Dialog.getString;
      range= split(Dialog.getString, "-");
      if (range.length==1) {
          min= NaN; max= parseFloat(range[0]);
      } else {
          min= parseFloat(range[0]); max= parseFloat(range[1]);
      }
      numLabels= Dialog.getNumber;
      decPlaces= Dialog.getNumber;
      rampH= parseFloat(Dialog.getChoice);
      font= Dialog.getChoice;
      fontS= Dialog.getNumber;
      ticks= Dialog.getCheckbox;

// get id of image and number of ROIs to colorize
   id= getImageID();
   items= minOf(nROIs, nRES);

// get values for chosen parameter
  values= newArray(items);
  for (i=0; i<items; i++)
      values[i]= getResult(parameter,i);
  Array.getStatistics(values, minC, maxC);
  if (isNaN(min)) min= minC;
  if (isNaN(max)) max= maxC;

// make heat-map legend
  rampW= rampH/8; canvasH= 3*fontS+rampH; canvasW= rampH/2; tickL= rampW/4;
  getLocationAndSize(imgx, imgy, imgwidth, imgheight);
  call("ij.gui.ImageWindow.setNextLocation", imgx+imgwidth, imgy);
  newImage(parameter+" Ramp", "8-bit ramp", rampH, rampW, 1);

// load the LUT as a hexColor array
  roiColors= loadLutColors(lut);

// continue the legend design
  saveSettings;
  setColor(0, 0, 0);
  setBackgroundColor(255, 255, 255);
  setLineWidth(1);
  setFont(font, fontS, "antialiased");
  run("RGB Color");
  if (ticks) { // left & right borders
      drawLine(0, 0, rampH, 0);
      drawLine(0, rampW-1, rampH, rampW-1);
  } else
      drawRect(0, 0, rampH, rampW);
  run("Rotate 90 Degrees Left");
  run("Canvas Size...", "width="+ canvasW +" height="+ canvasH +" position=Center-Left");

// draw ticks and values
  step= rampH;
  if (numLabels>2)
      step /= (numLabels-1);
  for (i=0; i<numLabels; i++) {
      yPos= round(fontS/2 + (rampH - i*step - 1))+fontS;
      rampLabel= min + (max-min)/(numLabels-1) * i;
      drawString( d2s(rampLabel,decPlaces), rampW+2, yPos+fontS/2);
      if (ticks) {
          drawLine(0, yPos, tickL, yPos);               // left tick
          drawLine(rampW-1-tickL, yPos, rampW-1, yPos); // right tick
      }
  }

// parse symbols in unit and draw final label below ramp
  label= cleanLabel(label);
  uW= minOf(getStringWidth(label), rampW);
  drawString(label, (rampW-uW)/2, canvasH);
  restoreSettings;

// iterate through the ROI Manager list and colorize ROIs
  selectImage(id);
  for (countNaN=0, i=0; i<items; i++) {
      if (isNaN(values[i])) countNaN++;
      if (values[i]<=min)
          lutIndex= 0;
      else if (values[i]>max)
          lutIndex= 255;
      else
          lutIndex= round(255 * (values[i] - min) / (max - min));
      roiManager("select", i);
      if (stroke>0) {
          roiManager("Set Line Width", stroke);
          roiManager("Set Color", alpha+roiColors[lutIndex]);
      } else
          roiManager("Set Fill Color", alpha+roiColors[lutIndex]);
  }

// display result
  roiManager("Show all");
  if (countNaN!=0)
      print("\n>>>> ROI Color Coder:\n"
          + "Some values from the \""+ parameter +"\" column could not be retrieved.\n"
          + countNaN +" ROI(s) were labeled with a default color.");

setBatchMode("exit & display");


function loadLutColors(lut) {
  run(lut);
  getLut(reds, greens, blues);
  hexColors= newArray(256);
  for (i=0; i<256; i++) {
      r= toHex(reds[i]); g= toHex(greens[i]); b= toHex(blues[i]);
      hexColors[i]= ""+ pad(r) +""+ pad(g) +""+ pad(b);
  }
  return hexColors;
}

function pad(n) {
  n= toString(n); if (lengthOf(n)==1) n= "0"+n; return n;
}

function cleanLabel(string) {
  string= replace(string, "\\^2", fromCharCode(178)); // superscript 2
  string= replace(string, "\\^3", fromCharCode(179)); // superscript 3
  string= replace(string, "(?<![A-Za-z0-9])u(?=m)", fromCharCode(181)); // micrometer units
  string= replace(string, "\\b[aA]ngstrom\\b", fromCharCode(197)); // angstrom symbol
  return string;
}
