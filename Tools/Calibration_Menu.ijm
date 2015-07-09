/* Calibration_Menu.ijm
 * IJ BAR: https://github.com/tferr/Scripts#scripts
 *
 * An IJ1 Menu Tool providing shortcuts for spatial calibration of images lacking metadata.
 *
 *  - Create "Calibration files" for each of your microscopes. These are simple 3-column
 *    .csv tables listing the objective, pixel size and unit. Choose "Demo File..." for
 *    more details
 *  - Settings are stored in the image header (Image>Show Info...) upon calibration
 */

// Editable variables
var prmpt= false;  // With stacks and hyperstacks, should you be prompted for voxel depth?
var cpath= getDirectory("imagej") +"Microscope Profiles Collection/"; // Path to calibration files
var tpath= getDirectory("macros") +"tools"+ File.separator +"Calibration_Menu.ijm"; // Path to this file

// Fixed variables
var cFile= call("ij.Prefs.get", "qcm.file", " ");
var fList= extractListfromFile(""+ cpath +""+ cFile);
var oList= getArrayfromList("Lns", true, cFile);
var cList= getArrayfromList("Cal", false, "-");
var uList= getArrayfromList("Unt", false, "-");
var fMenu= newMenu("Calibration Menu Tool", oList);


macro "Calibration Menu Tool - C037L101fL0020L0f2fL0323L0626L0929L0c2c D4fD5fD68D69D6aD6fD76D7aD7bD7cD7dD7fD8aD8fD90D91D92D93D94D95D96D97D98D9aD9dD9fDaaDafDbfD86D77DbaD5eD6eD7eD8eD9eDa3Da4Da5Da6Da7DaeDbeD85D67DcfD78D79D87D8bD8cD8dDa0Da1Da2Da8Dad" {
    cmd= getArgument();
     if (endsWith(cmd, ".csv") || endsWith(cmd, "not defined..."))
         getCSVchoice();
     else if (endsWith(cmd, "Scale Bar..."))
         run(cmd);
     else if (cmd=="Remove Scale")
         setVoxelSize(1, 1, 1, "pixels");
     else if (cmd!="-") {
         for (i=0; i<oList.length; i++) {
             if (cmd==oList[i]) {
                 setVoxelSize(cList[i], cList[i], cList[i], uList[i]);
                 setMetadata("Info", getMetadata("Info") + "\nLens: "+ oList[i] +" ["+ cFile +"]");
                 showStatus("Calibration applied: "+ oList[i]);
                 getDimensions(width, height, channels, slices, frames);
                 if (prmpt && (slices>1 || frames>1))
                     run("Properties...");
             }
         }
     }
}

function extractListfromFile(calFile) {
  if (File.exists(calFile)) {
      rawCal= File.openAsRawString(calFile);
      list= split(rawCal,"\n");
      lgth= list.length;
      for (i=1; i<lgth; i++) {
          line= split(list[i],",");
          if (line.length<2)
              return "";
          //list[i] = replace(list[i], "^\\s*", ""); // remove leading spaces from csv column
          List.set("Lns"+""+i-1, line[0]);
          List.set("Cal"+i-1, line[1]);
          List.set("Unt"+i-1, line[2])
      }
  }
  return List.getList;
}

function getArrayfromList(key, prefix, header) {
  qcm_indnt= "    "; // indentation for each lens on the list (aesthetic function only)
  lgth= ((List.size)/3)+1;
  if (lgth==1)
      return newArray("Calibration file not defined...");
  array= newArray(lgth+3);

  array[0]= header;
  array[lgth]= "-";
  array[lgth+1]= "Scale Bar...";
  array[lgth+2]= "Remove Scale";

  for (i=1; i<lgth; i++) {
      array[i]= List.get(key+""+i-1);
      if (prefix)
         array[i]= qcm_indnt+array[i];
  }
  return array;
}

function getCSVchoice() {
  if (!File.exists(cpath))
      File.makeDirectory(cpath);
  rawlist= getFileList(cpath);
  for (count=0, i=0; i< rawlist.length; i++)
      if (endsWith(rawlist[i], ".csv")) count++;
  list= newArray(count+1);
  for (index=0, i=0; i< rawlist.length; i++) {
      if (endsWith(rawlist[i], ".csv"))
          list[index++]= rawlist[i];
  }
  list[count]= "Demo file...";

  Dialog.create("Calibration Menu");
  Dialog.addChoice("Calibration file:", list, cFile);
  Dialog.addHelp("<html>Use this Menu Tool to spatially calibrate images lacking detailed metadata.<br><br>"
        +"<b>Select the csv file corresponding to the device used to acquire the image</b>.<br>"
        +"Select <i>Demo File...</i> to generate a template to be edited by an external spreadsheet<br>"
        +"application.<br><br>"
        +"<b>To apply global calibrations</b>, i.e., to propagate the chosen calibration to all open<br>"
        +"images, <b>activate the <i>Global</i> checkbox in Image>Properties... [P]</b> (titles of<br>"
        +"image windows will be labeled with a '<tt>(G)</tt>').<br><br>"
        +"Calibration settings are loaded from <tt>.csv</tt> files stored in:<br>"
        +"&emsp;&emsp;<tt>"+ cpath +"</tt>");
  Dialog.setInsets(-2, 105, 10);
  Dialog.addCheckbox("Open selected file", false);
  Dialog.addCheckbox("Prompt for voxel depth", prmpt);
  Dialog.show;
  file= Dialog.getChoice; opn= Dialog.getCheckbox; prmpt= Dialog.getCheckbox;

  if (file==list[count]) {
      if (!File.exists(cpath+"DemoMicroscope.csv")) {
          f= File.open(cpath+"DemoMicroscope.csv");
          print(f, "Lens, Pixel Size, Unit, Comments");
          print(f, "10x Ph2 NEOFL, 0.660, um, some comment here");
          print(f, "20x Plan 0.65, 0.331, um, some other comment here");
          File.close(f);
      }
      file= "DemoMicroscope.csv";
  }
  if (opn)
      open(cpath+file);
  if (file!=cFile) {
      call("ij.Prefs.set", "qcm.file", file);
      if (File.exists(tpath))
          run("Install...", "install=["+ tpath +"]");
      else
          showMessage("Tool could not be reloaded", tpath
                     +"\nwas not found. You must install the tool manually to reload the new settings.");
  }
}
