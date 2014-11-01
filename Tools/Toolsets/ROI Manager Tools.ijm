/* ROI Manager Tools.ijm
 * IJ BAR: https://github.com/tferr/Scripts#scripts
 *
 * ImageJ toolset that renames selections stored in the ROI Manager. Supersedes the
 * 'Rename and Save ROI Set.txt' macro. Save this file in ImageJ/macros/toolsets/,
 * then use the '>>' drop down menu to activate it. Requires IJ 1.45d or newer.
 *
 * TF, 04.2012: Initial version
 * TF, 12.2013: Fixed a bug in which labels were not being saved.
 */

var labels= getPrefList("labels");
var prefixs= getPrefList("prefixes");
var suffixs= getPrefList("suffixes");
var oldstr= "", newstr= "", usenames, label= true;

macro "AutoRun" { run("ROI Manager..."); }

macro "Unused Tool-" {}

var bCmds= newMenu("Settings Menu Tool", newArray("Define tags...", "Define prefixes...",
            "Define suffixes...", "-", "Rename by pattern...", "-", "Remove all prefixes",
            "Remove all suffixes", "-", "Toggle numeric labels", "Toggle labels in Results"));
macro "Settings Menu Tool - C037D3eD4eD5eD6bD6cD6dD7aD89D98Da7Db6Dc6Dd6De4De5D2aD5dDa2Dd5D59D68D69D77D78D86D87D96D1aD1bD1cD29D2bD39D49D4bD4cD4dD58D67D76D85D92D93D94Da1Db1Db2Db4Dc1Dc4Dd4De3D5aD6aD79D88D95D97Da5Da6D19D91D4aD5bDa4Db5D3aD5cDa3Dc5" {
    setBatchMode(true);
    cmd= getArgument(); i= roiManager("index"); n= roiManager("count");
    bools= newArray("true", "false");
    if(cmd=="Define tags...") {
        createNewList("labels", "Label");
    } else if(cmd=="Define prefixes...") {
        createNewList("prefixes", "Prefix");
    } else if(cmd=="Define suffixes...") {
        createNewList("suffixes", "Suffix");
    } else if(cmd=="Rename by pattern...") {
        help= "<html>Search is case sensitive. Regex patterns can also be used, e.g.,<br>"
             +"\"<tt>[0-9&&[^1]]\</tt>\" to find a digit that is not 1, or \"<tt>-.*-\</tt>\" to "
             +"find<br>any character sequence flanked by hyphens. You may need to<br>"
             +"escape metacharacters ('.', '[', ']', '^', '$', etc.) by a backslash.<br><br>"
             +"Note that replaced patterns should always give rise to unique<br>"
             +"names as the ROI Manager cannot cope with duplicated entries.";
        Dialog.create("Rename Pattern");
          Dialog.addString("Find:", oldstr, 18);
          Dialog.addString("Replace:", newstr, 18);
          Dialog.addString("Range:", maxOf(1,i+1) +"-"+ n, 9);
          Dialog.addHelp(help);
        Dialog.show();
        oldstr= Dialog.getString(); newstr= Dialog.getString();
        range= parseRange(Dialog.getString);
        renamePattern(range[0], range[1], oldstr, newstr);
    } else if(cmd=="Remove all prefixes") {
        renamePattern(0, n, ".*\\[", "");
    } else if(cmd=="Remove all suffixes") {
        renamePattern(0, n, "\\].*", "");
    } else if(cmd=="Toggle numeric labels") {
        roiManager("UseNames", bools[usenames]); usenames= !usenames;
        roiManager("Show All with labels");
    } else if(cmd=="Toggle labels in Results") {
        setOption("Display Label", label); label= !label;
    } else if (cmd!="-")
        run(cmd);
}

var cCmds= newMenu("Rename Menu Tool", labels);
macro "Rename Menu Tool - C037T0b11T T6b10a Tab10g Tfb10s" {
    setBatchMode(true);
    cmd= getArgument(); i= roiManager("index");
    renameROI(cmd, "Label", i);
    walkList(i, 0);
}

var dCmds= newMenu("Add prefix Menu Tool", prefixs);
macro "Add prefix Menu Tool - C037T0b10PT6b10rTab10fTeb10x" {
    setBatchMode(true);
    cmd= getArgument(); i= roiManager("index");
    renameROI(cmd, "Prefix", i);
    walkList(i, 0);
}

var eCmds= newMenu("Add suffix Menu Tool", suffixs);
macro "Add suffix Menu Tool - C037 T0b10ST6b10fTab10fTeb10x" {
    setBatchMode(true);
    cmd= getArgument(); i= roiManager("index");
    renameROI(cmd, "Suffix", i);
    walkList(i, 0);
}

macro "Selection cycler [Shift click: Previous] [Alt click: First] Action Tool - C037L020dL0268L0d68Lf2fdLf298Lfd98" {
    if (roiManager("count")==0) {showStatus("ROI Manager is empty."); exit;}
    i= roiManager("index");
    if (isKeyDown("alt") || i<0)
        roiManager('select', 0);
    else if (isKeyDown("shift"))
        walkList(i, 1);
    else
        walkList(i, 0);
}

var fCmds= newMenu("Save all regions Menu Tool", newArray("In image directory", "Elsewhere..."));
macro "Save all regions Menu Tool - C037D11D12D13D14D15D16D17D18D19D1aD1bD1cD21D27D28D29D2aD2bD2cD2dD31D33D35D37D38D39D3aD3bD3cD3dD3eD41D43D45D47D48D4eD51D53D55D57D58D5aD5bD5cD5eD61D63D65D67D68D6aD6bD6cD6eD71D73D75D77D78D7eD81D83D85D87D88D8eD91D93D95D97D98D9eDa1Da3Da5Da7Da8DaeDb1Db7Db8DbeDc1Dc2Dc3Dc4Dc5Dc6Dc7Dc8Dc9DcaDcbDccDcdDce" {
    if (roiManager("count")==0)
        exit("The selection list is empty.");
    if (nImages==0)
        exit(" No images open. Alternatively, use the\nROI Manager \"More>>Save...\" command.");
    cmd= getArgument();
    if (cmd=="In image directory") {
        path= getDirectory("image");
        if (!File.exists(path)) exit("Could not retrieve path of image.");
    } else if (cmd=="Elsewhere...")
        path= getDirectory("Select a Directory");
    roiManager('save', path + getTitle +'-ROIset.zip');
    showStatus("All ROIs in Manager have been saved...");
}

macro "Help Action Tool - C037T3e16?" {
  help= "<html>The ROI Manager <i>Rename...</i> command cannot rename multiple ROIs, requiring user<br>"
       +"input each time it is used. This set of tools addresses this issue by using 3 types of<br>"
       +"predefined labels: \"Tags\", \"Prefixes\" and \"Suffixes\", specified in the <i>Settings</i> Menu.<br>"
       +"Tags rename ROIs, prefixes and suffixes are appended to the listed name.<br><br>"
       +"To rename multiple ROIs, press <i>Deselect</i> in the ROI Manager, and choose a label<br>"
       +"from one of menus. Alternatively, choose <i>Rename by pattern...</i> from the Settings<br>"
       +" menu. In both cases, you can specify the Range of ROIs to be renamed, as when<br>"
       +"using the ROI Manager <i>Properties...</i> command.<br><br>"
       +"To rename single ROIs, use the <i>Selection cycler</i> to walk through the listed ROIs one<<br>"
       +"item at a time.<br><br>"
       +"Use the <i>Save</i> menu to store all ROIs in a .zip file named after the active image.<br><br>"
       +"You may need to stretch the ROI Manager window to accommodate long ROI names.";
    showMessage("ROI Manager Tools", help);
}

function getPrefList(type) {
    prefs= call("ij.Prefs.get",  "rmtools."+type, "no"+type);
    if (startsWith(prefs,"no")) {
        if (type=="labels") items= "Cortex|DG|CA3|CA1|Striatum|-|Custom...";
        if (type=="prefixes") items= "WT|KO|Custom...";
        if (type=="suffixes") items= "Anterior|Posterior|Ventral|Medial|Dorsal|Custom...";
        call("ij.Prefs.set", "rmtools."+type, items);
        return split(items, "|");
    } else
        return split(prefs, "|");
}

function createNewList(type, prompt) {
  help= "<html>None of the "+ type +" should contain \"|\", \"[\" and \"]\" as these are<br>"
       +"used to define word boundaries. Entries defined by a single<br>"
       +"hyphen are interpreted as menu separators.<br><br>If you prefer, you can define "
       +"the last "+ prompt +" as \"Custom...\" to<br>be prompted for ad-hoc strings.";
  prefs= ""; items= getPrefList(type);
  Dialog.create('Define '+ type);
    for (i=1; i<=items.length; i++)
        Dialog.addString(prompt +" "+ i +":", items[i-1], 15);
    Dialog.addCheckbox("Add more "+type, false);
    Dialog.addCheckbox("Reset all entries", false);
    Dialog.addHelp(help);
  Dialog.show();
  add= Dialog.getCheckbox(); res= Dialog.getCheckbox();
  for (i=1; i<=items.length; i++)
      prefs+= Dialog.getString() +"|";
  if (res) {
      call("ij.Prefs.set", "rmtools."+type, "no"+type);
      createNewList(type, prompt);
  }
  call("ij.Prefs.set", "rmtools."+type, prefs); // store new list
  if (add) { // keep prompting for new items
      prefs+= "-";
      call("ij.Prefs.set", "rmtools."+type, prefs);
      createNewList(type, prompt);
  }
  // The Fiji editor appends ".ijm" to .txt files, we'll try to find the file w/o extension
  path= getDirectory("macros")+"toolsets/";
  list= getFileList(path);
  for (i=0; i<list.length; i++)
      if (startsWith(list[i], "ROI Manager Tools")) path+= list[i];
  if (File.exists(path))
      run("Install...", "install=["+ path +"]");
  else
      showMessage("ROI Manager Tools could not be reloaded as it was not\n"
                 +"found in the ImageJ/macros/toolset directory. You must\n"
                 +"re-install the file manually to reload the new settings.");
}

function renameROI(newname, placement, item) {
    custom= (newname=="Custom...");
    if (item<0) { // multiple ROIs selected
        n= roiManager("count");
        if (custom) {
            Dialog.create("Rename Multiple ROIs");
              Dialog.addString(placement +":", "", 18);
              Dialog.addString("Range:",  "1-"+ n, 9);
            Dialog.show;
            newname= Dialog.getString(); rg= parseRange(Dialog.getString);
        } else
            rg= parseRange(getString("\""+ newname +"\"; ROI range:", "1-"+ n));
        for (i=rg[0]; i<rg[1]; i++)
            renameROI(newname, placement, i);
        roiManager("Deselect"); return;
    }
    oldname= call("ij.plugin.frame.RoiManager.getName", item);
    if (custom) newname= getString(placement +":", "");
    if (placement=="Prefix") {
       sep= indexOf(oldname, "[");
       if (sep!=-1) oldname= substring(oldname, sep+1);
       newname+= "["+oldname;
    } else if (placement=="Suffix") {
       sep= lastIndexOf(oldname, "]");
       if (sep!=-1) oldname= substring(oldname, 0, sep);
       newname= oldname +"]"+ newname;
    } else // rename
        newname= IJ.pad(i+1, 2) +":"+ newname;
    roiManager("select", item);
    roiManager("rename", newname);
}

function walkList(idx, backwards) {
    lngth= roiManager("count");
    if (idx<0) return;
    if (backwards) {
        idx-= 1; if (idx<0) idx= lngth-1;
    } else {
        idx+= 1; if (idx>=lngth) idx= 0;
    }
    roiManager("select", idx);
}

function renamePattern(min, max, old, new) {
    for (i=min; i<max; i++) {
        name= call("ij.plugin.frame.RoiManager.getName", i);
        name= replace(name, old, new);
        roiManager("select", i);
        roiManager("rename", name);
    }
    roiManager("Deselect");
}

function parseRange(string) {
    range= split(string, "-");
    if (range.length==1) {
        min= 0; max= parseFloat(range[0]);
    } else {
        min= parseFloat(range[0]); max= parseFloat(range[1]);
    }
    min= maxOf(0, min-1); max= minOf(max, roiManager("count"));
    if (min>max || max==0) exit("Invalid ROI range.");
    return newArray(min, max);
}
