/* Shortcuts_Menu.ijm
 * IJ BAR: https://github.com/tferr/Scripts#scripts
 *
 * An IJ1 Menu Tool listing the user's most used commands. List is remembered across restarts
 *
 * Non-BAR users: place this file in ImageJ/plugins/Tools/. Rename the variable "path" below
 * and re-start ImageJ. Load it using the ">>"
 * drop-down menu in the main ImageJ window
 */

var cmdList = getMenuPrefs();
var dCmds = newMenu("Shortcuts Menu Tool", cmdList);
var path= getDirectory("macros") +"tools"+ File.separator +"Shortcuts_Menu.ijm"; // Path to this file


macro "Shortcuts Menu Tool - C037 D1fD2fD3fD4fD9fDafDbfDcfD2eD3eD4eD9eDaeDbeDdfD3dD4dD9dDadD4aD9aDbdD3cD4cD9cDacD0fD19D59D5fD89D8fDc9D29D4bD9bDb9D18D28D58D5eD68D78D88D8eDb8Dc8D69D79D1eD27D2dD57D5dD67D77D87D8dDb7DceD26D36D46D56D5cD66D76D86D8cD96Da6Db6D49D99D25D35D3bD45D55D5bD65D75D85D8bD95Da5DabDb5D34D44D54D5aD64D74D84D8aD94Da4D33D43D53D63D73D83D93Da3D62D72D2cDbcD61D71D52D82D42D92D60D70D17D24D32Da2Db4Dc7" {
    cmd = getArgument();
    if (startsWith(cmd, "Save All")) {
        path = getDirectory("Choose directory to save all open images as .tif");
        while (nImages>0) {
            selectImage(nImages); saveAs("tiff", path+getTitle); close;
        }
    } else if (cmd=="Define Shortcuts...") {
        shortcutInstaller();
    } else if (endsWith(cmd, ".txt") || endsWith(cmd, ".ijm")) {
        if (isKeyDown("Shift"))
             open(getDirectory("macros")+cmd);
        else
            runMacro(cmd);
    } else if (endsWith(cmd, ".js") || endsWith(cmd, ".bsh") || endsWith(cmd, ".py") || endsWith(cmd, ".rb") || endsWith(cmd, ".clj")) {
        open(getDirectory("imagej")+ "scripts"+ File.separator + cmd);
    } else if (cmd!="-")
        run(cmd);
 }

function shortcutInstaller() {
  help = "<html>You can create shortcuts to ImageJ commands (including any installed command in<br>"
       +"the Plugins Menu). You can also list macro files in <tt>ImageJ/macros/</tt> or scripts in<br>"
       +"<tt>ImageJ/scripts/</tt> as long as you type their full filename, file extension included.<br><br>"
       +"Entries defined by a single hyphen are interpreted as menu separators. Empty entries<br>"
       +"are skipped (so that a particular item can be removed at any time). When all entries<br>"
       +"are filled, selecting the <i>Add more shortcuts</i> checkbox will expand the shortcut list.";

  prefs = call("ij.Prefs.get", "sMenu.list", "-");
  prefs = split(prefs, ",");
  newitems = "";
  k = maxOf(prefs.length, 6);

  Dialog.create('Define Shortcuts');
    for (i=0; i<prefs.length; i++)
        Dialog.addString("Shortcut "+ i+1 +":", prefs[i], 20);
    for (j=prefs.length; j<k; j++)
        Dialog.addString("Shortcut "+ j+1 +":", "", 20);
    Dialog.addCheckboxGroup(1,2,newArray("Add more shortcuts", "Clear all entries"), newArray(2));
    Dialog.addHelp(help);
  Dialog.show();
    add = Dialog.getCheckbox();
    clear = Dialog.getCheckbox();

  for (i =0; i<k; i++) {
      entry = Dialog.getString;
      if (!blankString(entry))
          newitems+= entry +",";
  }
  if (clear) {
      call("ij.Prefs.set", "sMenu.list", "-");
      shortcutInstaller();
  } else {
  	  if (!blankString(newitems))
          call("ij.Prefs.set", "sMenu.list", newitems);
      if (add) {
          call("ij.Prefs.set", "sMenu.list", newitems+", , , ,");
          shortcutInstaller();
      } else if (File.exists(path))
          run("Install...", "install=["+ path +"]");
      else
          showMessage("Tool could not be reloaded", path +" not found.\n"
                     +"You must install the tool manually to reload the new settings.");
  }
}

function getMenuPrefs() {
    prefs = call("ij.Prefs.get", "sMenu.list", "-");
    if (!endsWith(prefs, ",")) prefs += ",";
    prefs += "-,Save All Images,-,Define Shortcuts...";
    return split(prefs, ",");
}

function blankString(string) {
    strLength = lengthOf(string);
    if (strLength==0)
        return true;
    for (i=0; i<strLength; i++)
        if (charCodeAt(string, i)!=32) //white space
            return false;
    return true;
}
