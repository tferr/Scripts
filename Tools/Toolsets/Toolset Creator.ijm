/* Toolset Creator
 * IJ BAR: https://github.com/tferr/Scripts#scripts
 *
 * Creates toolsets of Built-in Tools and Menu Tools listing installed plugins.
 * v.14 2015.07, see https://github.com/tferr/Scripts/releases for release notes
 */

var maxTools= 10;

macro "AutoRun"{
    wait(15); run("Toolset Creator");
}

macro "Toolset Creator" {
    Dialog.create("Toolset Creator");
    msg = "This sequence of prompts will create a toolset grouping two types of\n"
        + "tools: Built-in tools (those listed at the bottom of the '>>' menu) and\n"
        + "Menu Tools listing commands from subfolders of ImageJ/plugins/";
    Dialog.addMessage(msg);
    Dialog.addString("      Toolset name without extension:", getInfo("user.name") +"'s Toolset", 22);
    Dialog.show;
    name= replace(Dialog.getString, "^[.]+|[\\/:\"*?<>|]+", ""); // remove leading dots & illegal chars
    path= getDirectory("macros");
    if (!File.exists(path))
        exit("macros directory not found!\n \nToolset cannot be saved...");
    path= path +"toolsets"+ File.separator;
    if (!File.exists(path))
        File.makeDirectory(path);
    path= path + name + ".ijm";
    if (File.exists(path))
        showMessageWithCancel("Redesign previous toolset?","A file named \""+ name
                             +".ijm\" already exists\nin ImageJ/macros/toolsets/. Overwrite?");
    showStatus("Creating the toolset: An empty file will be created if you hit \"Cancel\"");

    for (i=0; i<nColors; i++) {
        separator= indexOf(colors[i], ":");
        sColors[i]= substring(colors[i], 0, separator);
        hColors[i]= substring(colors[i], separator+1);
    }
    for (i=0; i<nIcons; i++) {
        separator= indexOf(icons[i], ":");
        sIcons[i]= substring(icons[i], 0, separator);
        hIcons[i]= substring(icons[i], separator+1);
    }

    f= File.open(path);
    printHeader();
    while (add && slot<maxTools) {
        add= promptForSettings(); slot++;
    }
    if (menus) printFooter();
    File.close(f);
    open(path);
    showStatus("Toolset created. Use the '>>' menu to switch between toolsets");
}

function listPluginDirs() {
  plgn= getDirectory("plugins");
  if (!File.exists(plgn))
      return newArray(0);
  rawlist= getFileList(plgn);
  for (count=0, i=0; i< rawlist.length; i++)
      if (endsWith(rawlist[i], '/')) count++;
  if (count==0)
      return newArray(0);
  list= newArray(count);
  for (index=0, i=0; i< rawlist.length; i++) {
      if (endsWith(rawlist[i], '/'))
          list[index++]= substring(rawlist[i], 0, lengthOf(rawlist[i])-1);
  }
  return list;
}

function promptForSettings() {
  addMore= slot<maxTools-1;
  dirs= folders.length>0;
  Dialog.create("Toolset Creator: Designing slot "+ slot+1);
  msg = "<html>For each slot you can either append 1) a <i>Built-in Tool</i> or 2) a designed<br>"
      + "<i>Menu Tool</i>, a drop-down menu listing commands (i.e., executable files) in<br>"
      + "subfolders of the <tt>plugins/</tt> directory. This latter option is automatically<br>"
      + "disabled if no subdirectories are found.<br><br>"
      + "When building menu icons, the first two characters of the folder name are<br>"
      + "used if neither a symbol or a string is specified.<br><br>"
      + "Unfortunately, hitting <i>Cancel</i> or pressing <i>Esc</i> halfway through the design<br>"
      + "will lead to an empty toolset file.";
  Dialog.addHelp(msg);
  Dialog.setInsets(5, 0, 0);
  Dialog.addMessage("Option 1) Add a Built-in Tool to slot "+ slot+1 +":");
  Dialog.addChoice("Built-in tool:", bTools);
  if (dirs) {
      Dialog.setInsets(20, 0, 4);
      Dialog.addMessage("Option 2) Add a Menu Tool to slot "+ slot+1 + " instead:");
      Dialog.addChoice("       Listing executables in:", folders);
      Dialog.addChoice("Icon symbol:", sIcons, sIcons[prefersymbol]);
      Dialog.addString("Icon text:", "", 3);
      Dialog.setInsets(-30, 218, 0);
      Dialog.addMessage("(maximum 2 characters)");
      Dialog.addChoice("Icon color:", sColors, color);
  }
  if (addMore)
      Dialog.addCheckbox("Keep appending tools ("+ maxTools-1-slot +" slot(s) remain available)", true);
  Dialog.show();
  btool= Dialog.getChoice;
  if (dirs) {
      folder = Dialog.getChoice;
      icon = Dialog.getChoice;
      text = Dialog.getString;
      color = Dialog.getChoice;
  }
  if (addMore)
      add= Dialog.getCheckbox;

  if (btool!=bTools[0]) {
      print(f, " macro \""+ btool +" Built-in Tool\" {}\n ");
      bTools= trimChosenItem(bTools, btool);
  } else if (dirs) {
      menus= true;
      folders = trimChosenItem(folders, folder);
      idx= substring(alphaCounter, slot, slot+1);
      mName= replace(folder, "-", "");
      if (text=="" && icon==sIcons[0])
          text= mName;
      print(f, "   var "+ idx +"Dir= '"+ folder +"';");
      print(f, "   var "+ idx +"List= getPluginList("+ idx +"Dir);");
      print(f, "   var "+ idx +"Cmds= newMenu('"+ mName +" Menu Tool',"+ idx +"List);");
      print(f, " macro '"+ mName +" Menu Tool - "+ makeIcon(text, icon, color) +"' {");
      print(f, "   cmd= getArgument();");
      print(f, "   if (startsWith(cmd,'Reveal ')) {");
	  print(f, "       call('bar.Utils.revealFile', getDirectory('plugins')+"+ idx +"Dir);");
      print(f, "   } else if (startsWith(cmd,'No executable') || endsWith(cmd,' not found') || endsWith(cmd,'...')) {");
      print(f, "       showMessageWithCancel('Reveal '+ getDirectory('plugins')+ "+ idx +"Dir +'?');");
	  print(f, "       call('bar.Utils.revealFile', getDirectory('plugins')+"+ idx +"Dir);");
	  print(f, "   } else if (cmd!='-')");
      print(f, "       run(cmd);");
      print(f, " }\n ");
  }
  return add;
}

function trimChosenItem(array, item) {
  trimmedarray= newArray(array.length-1);
  for (j=0, i=0; i<array.length; i++)
      if (item!=array[i]) trimmedarray[j++]= array[i];
  return trimmedarray;
}

function makeIcon(iText, iDrawing, iColor) {
  while (lengthOf(iText)<2)
      iText+= " ";
  for (idx=0, i=0; i<sIcons.length; i++) {
      if (iDrawing==sIcons[i])
          {iDrawing= hIcons[i]; idx= i;}
  }
  for (i=0; i<sColors.length; i++) {
      if (iColor==sColors[i]) iColor= hColors[i];
  }
  icon= ""+ iColor +"T1d13"+ substring(iText, 0, 1) +"T9d13"+ substring(iText, 1, 2) + iDrawing;
  if (iDrawing!="") {
      prefersymbol=1;
      sIcons= trimChosenItem(sIcons, sIcons[idx]);
      hIcons= trimChosenItem(hIcons, hIcons[idx]);
  }
  return icon;
}

function printHeader() {
  getDateAndTime(y, m, null, d, h, min, sec, null);
  date= ""+y+"."+IJ.pad(m+1,2)+"."+IJ.pad(d,2)+" "+IJ.pad(h,2)+":"+IJ.pad(min,2)+":"+IJ.pad(sec,2);
  print(f,"/* File created by Toolset Creator (ImageJ "+ getVersion +") on "+ date);
  print(f, " *");
  print(f, " * Buil-in Tools:");
  print(f, " *   Those listed on the botoom of the '>>' list (e.g. drawing tools).");
  print(f, " *");
  print(f, " * Menu Tools:");
  print(f, " *   List executable files in subfolders of the ImageJ/plugins/ directory. Executables");
  print(f, " *   are 1) all macros ('.ijm'), 2) all script files ('.js', '.bsh', '.clj', etc), and");
  print(f, " *   3) plugins ('.class' and '.jar' files) with at least one underscore in the filename.");
  print(f, " *");
  print(f, " *   For any Menu Tool, hold down Shift while selecting a macro or script file in the");
  print(f, " *   dropdown list and ImageJ will open that macro or script.");
  print(f, " *");
  print(f, " *   Edit the three lines below '// Settings:' to change which files should be listed:");
  print(f, " *   E.g., you can exclude or rename the commands implemented by .jar files containing");
  print(f, " *   plugins.config instructions. N.B.: These three lines can be deleted if you have not");
  print(f, " *   created Menu Tools.");
  print(f," */\n ");
  print(f,"// Settings:");
  print(f," var AllowedFileExtensions= newArray('class','jar','ijm','bsh','js','py','clj','rb','groovy');");
  print(f," var IgnoreFilenamesContaining= newArray('$');");
  print(f," var ReplaceFilenames= newArray('3D Viewer:ImageJ_3D_Viewer.jar');\n \n ");
}

function printFooter() {
  print(f, "\nfunction getPluginList(subfolder) {");
  print(f, "  dir= getDirectory('plugins')+ subfolder +File.separator;");
  print(f, "  list= newArray(''+ subfolder +' not found','in the plugins directory...');");
  print(f, "  if (!File.exists(dir))");
  print(f, "      return list;");
  print(f, "  rawlist= getFileList(dir);");
  print(f, "  for (count=0, i=0; i< rawlist.length; i++) {");
  print(f, "      isPlugin= endsWith(rawlist[i],'.class') || endsWith(rawlist[i],'.jar');");
  print(f, "      if (indexOf(rawlist[i], '_')==-1 && isPlugin)");
  print(f, "          rawlist[i]='-';");
  print(f, "      for (h=0; h<IgnoreFilenamesContaining.length; h++)");
  print(f, "          if (indexOf(rawlist[i], IgnoreFilenamesContaining[h])!=-1) rawlist[i]='-';");
  print(f, "      for (j=0; j<AllowedFileExtensions.length; j++)");
  print(f, "          if (endsWith(rawlist[i], AllowedFileExtensions[j])) count++;");
  print(f, "  }");
  print(f, "  if (count==0) {");
  print(f, "      list= newArray('No executable files found','on '+''+subfolder+'...');");
  print(f, "      return list;");
  print(f, "  }");
  print(f, "  list= newArray(count+2);");
  print(f, "  list[0]= 'Reveal '+ subfolder;");
  print(f, "  list[1]= '-';");
  print(f, "  for (index=2, i=0; i< rawlist.length; i++) {");
  print(f, "      for (h=0; h< AllowedFileExtensions.length; h++) {");
  print(f, "          cmdlength= lengthOf(rawlist[i])-lengthOf(AllowedFileExtensions[h])-1;");
  print(f, "          if (endsWith(rawlist[i], AllowedFileExtensions[h])) {");
  print(f, "              for (m=0; m<ReplaceFilenames.length; m++) {");
  print(f, "                  if (endsWith(ReplaceFilenames[m], rawlist[i])) {");
  print(f, "                      rawlist[i]= substring(ReplaceFilenames[m], 0, indexOf(ReplaceFilenames[m], ':'));");
  print(f, "                      cmdlength= lengthOf(rawlist[i]);");
  print(f, "                  }");
  print(f, "              }");
  print(f, "              list[index]= substring(rawlist[i], 0, cmdlength);");
  print(f, "              list[index]= replace(list[index++],'_',' ');");
  print(f, "          }");
  print(f, "      }");
  print(f, "  }");
  print(f, "  return list;");
  print(f, "}");
}

var bTools= newArray("[Select from list]","Arrow", "Brush", "Developer Menu", "Flood Filler", "LUT Menu", "Overlay Brush", "Pencil", "Pixel Inspector", "Spray Can", "Stacks Menu");
var colors= newArray("Default:C037","Black:C000","Gray:C555", "Red:Cd00","Green:C0d0","Blue:C00d");
var icons= newArray("[None. Create icon text]:",
    "Bricks:D00D03D06D09D0cD0fD10D13D16D19D1cD1fD20D21D22D23D26D27D28D29D2cD2dD2eD2fD30D33D36D39D3cD3fD40D43D46D49D4cD4fD50D53D54D55D56D59D5aD5bD5cD5fD60D63D66D69D6cD6fD70D73D76D79D7cD7fD80D83D86D89D8cD8fD90D91D92D93D96D97D98D99D9cD9dD9eD9fDa0Da3Da6Da9Db0Db3Db6Db9Dc0Dc3Dc4Dc5Dc6Dc9DcaDcbDd0Dd3Dd6Dd9De0De3De6De9Df0Df3Df6Df9",
    "Down Arrow:D2aD3aD3bD4aD4bD4cD50D51D52D53D54D55D56D57D58D59D5aD5bD5cD5dD60D61D62D63D64D65D66D67D68D69D6aD6bD6cD6dD6eD70D71D72D73D74D75D76D77D78D79D7aD7bD7cD7dD7eD7fD80D81D82D83D84D85D86D87D88D89D8aD8bD8cD8dD8eD90D91D92D93D94D95D96D97D98D99D9aD9bD9cD9dDaaDabDacDbaDbbDca",
    "Film:D00D01D02D03D04D05D06D07D08D09D0aD0bD0cD0dD0eD0fD12D17D1cD22D27D2cD30D31D32D33D34D35D36D37D38D39D3aD3bD3cD3dD3eD3fD40D4bD50D5bD60D6bD70D7bD80D8bD90D9bDa0DabDb0Db1Db2Db3Db4Db5Db6Db7Db8Db9DbaDbbDbcDbdDbeDbfDc2Dc7DccDd2Dd7DdcDe0De1De2De3De4De5De6De7De8De9DeaDebDecDedDeeDef",
    "Floppy Disk:D11D12D13D14D15D16D17D18D19D1aD1bD1cD21D27D28D29D2aD2bD2cD2dD31D33D35D37D38D39D3aD3bD3cD3dD3eD41D43D45D47D48D4eD51D53D55D57D58D5aD5bD5cD5eD61D63D65D67D68D6aD6bD6cD6eD71D73D75D77D78D7eD81D83D85D87D88D8eD91D93D95D97D98D9eDa1Da3Da5Da7Da8DaeDb1Db7Db8DbeDc1Dc2Dc3Dc4Dc5Dc6Dc7Dc8Dc9DcaDcbDccDcdDce",
    "Folder:D13D14D16D17D18D19D1aD1bD1cD22D23D24D26D27D28D29D2aD2bD2cD31D32D33D34D36D37D38D39D3aD3bD3cD41D42D43D44D46D47D48D49D4aD4bD4cD51D52D53D54D56D57D58D59D5aD5bD5cD61D62D63D64D66D67D68D69D6aD6bD6cD72D73D74D76D77D78D79D7aD7bD7cD83D84D86D87D88D89D8aD8bD8cD93D94D96D97D98D99D9aD9bD9cDa3Da4Da6Da7Da8Da9DaaDabDacDb3Db4Db6Db7Db8Db9DbaDbbDbcDc3Dc4Dc6Dc7Dc8Dc9DcaDcbDccDd3Dd4Dd6Dd7Dd8Dd9DdaDdbDdcDe3De4De6De7De8De9DeaDebDec",
    "Gear:D16D17D23D26D27D2aD32D33D34D35D36D37D38D39D3aD3bD43D44D45D46D47D48D49D4aD53D54D55D58D59D5aD61D62D63D64D69D6aD6bD6cD71D72D73D74D79D7aD7bD7cD83D84D85D88D89D8aD93D94D95D96D97D98D99D9aDa2Da3Da4Da5Da6Da7Da8Da9DaaDabDb3Db6Db7DbaDc6Dc7CcccD22D24D29D2bD42D4bD56D57D65D68D75D78D86D87D92D9bDb2Db4Db9Dbb",
    "Histogram:D1eD2dD1fD2eD3cD3dD4bD4cD5aD5bD6aD14D15D16D17D18D19D1aD1bD1cD1dD20D21D22D23D24D25D26D27D28D29D2aD2bD2cD2fD33D34D35D36D37D38D39D3aD3bD3eD3fD47D48D49D4aD4dD4eD4fD5cD5dD5eD5fD6bD6cD6dD6eD6fD7dD7eD7fD8dD8eD8fD9dD9eD9fDaeDafDbeDbfDcfDdf",
    "Hourglass:D10D1fD20D21D22D2dD2eD2fD30D33D3cD3fD40D44D4bD4fD50D55D5aD5fD60D66D67D68D69D6fD70D75D7aD7fD80D84D8bD8fD90D93D9cD9fDa0Da1Da2DadDaeDafDb0Dbf",
    "Image:R01fbR2397",
    "List:L00f0L03f3L06f6L09f9L0cfcL0fbf",
    "Pen:D1dD2aD2bD2cD37D38D39D3aD3bD3eD43D44D45D46D47D48D49D4aD4dD4eD53D54D55D56D57D58D59D5cD5dD63D64D65D66D67D68D6bD6cD6dD73D74D75D76D77D7aD7bD7cD7dD84D85D86D87D89D8aD8bD8cD92D93D95D96D97D98D99D9aD9bD9cDa1Da2Da3Da4Da6Da7Da8Da9DaaDabDacDb2Db3Db4Db5Db7Db8Db9DbaDbbDbcDc3Dc4Dc5Dc6Dc8Dc9DcaDcbDccDd4Dd5Dd6De5De6",
    "Reduce:D17D23D27D2bD33D34D37D3aD3bD40D41D42D43D44D45D47D49D4aD4bD4cD4dD4eD53D54D57D5aD5bD63D67D6bD77D83D87D8bD93D94D97D9aD9bDa0Da1Da2Da3Da4Da5Da7Da9DaaDabDacDadDaeDb3Db4Db7DbaDbbDc3Dc7DcbDd7",
    "Robot:D0fD18D19D1aD1fD26D27D28D29D2aD2dD2eD2fD33D34D35D36D3bD3cD3dD3eD3fD42D43D44D45D46D49D4aD4bD4cD4dD4eD4fD52D53D54D55D56D57D58D59D5aD5bD5cD5dD5eD5fD60D61D62D63D64D65D66D67D68D69D70D71D72D73D74D75D76D77D78D79D82D83D84D85D86D87D88D89D8aD8bD8cD8dD8eD8fD92D93D94D95D96D99D9aD9bD9cD9dD9eD9fDa3Da4Da5Da6DabDacDadDaeDafDb6Db7Db8Db9DbaDbdDbeDbfDc8Dc9DcaDceDcfDdf",
    "Ruler:L15f5L1519Lf5f9L8885L5855Lb8b5",
    "Shuffle:D05D06D07D16D17D25D27D2aD34D3bD43D4cD52D5dD5fD6eD6fD7dD7eD7fD80D81D82D90D91Da0Da2DadDb3DbcDc4DcbDd5Dd8DdaDe8De9Df8Df9Dfa",
    "Sigma:D00D01D02D03D0dD0eD0fD10D11D12D13D14D1cD1dD1eD1fD20D21D24D25D2bD2cD2eD2fD30D31D35D36D3aD3bD3eD3fD40D41D46D47D49D4aD4eD4fD50D51D57D58D59D5eD5fD60D61D68D6eD6fD70D71D7eD7fD80D81D82D83D8cD8dD8eD8fD90D91D92D93D9cD9dD9eD9f",
    "Stacks:L00f0L01f1L04f4L05f5L08f8L09f9L0cbcL0dbd",
    "Tag:D22D23D24D25D26D27D32D33D34D35D36D37D38D42D43D46D47D48D49D52D53D56D57D58D59D5aD62D63D64D65D66D67D68D69D6aD6bD72D73D74D75D76D77D78D79D7aD7bD7cD83D84D85D86D87D88D89D8aD8bD8cD8dD94D95D96D97D98D99D9aD9bD9cD9dD9eDa5Da6Da7Da8Da9DaaDabDacDadDaeDb6Db7Db8Db9DbaDbbDbcDbdDc7Dc8Dc9DcaDcbDccDd8Dd9DdaDdbDe9Dea",
    "Tiles:R0066R9066R0966L99f9L999fL9fdfLf9fc",
    "Wrench:D3eD4eD5eD6bD6cD6dD7aD89D98Da7Db6Dc6Dd6De4De5D2aD5dDa2Dd5D59D68D69D77D78D86D87D96D1aD1bD1cD29D2bD39D49D4bD4cD4dD58D67D76D85D92D93D94Da1Db1Db2Db4Dc1Dc4Dd4De3D5aD6aD79D88D95D97Da5Da6D19D91D4aD5bDa4Db5D3aD5cDa3Dc5");

var nColors= colors.length;           var nIcons= icons.length;
var sColors= newArray(nColors);       var sIcons= newArray(nIcons);
var hColors= newArray(nColors);       var hIcons= newArray(nIcons);
var alphaCounter= "abcdefghijklmno";  var add= true;
var folders= listPluginDirs();        var color, prefersymbol, menus, slot;
