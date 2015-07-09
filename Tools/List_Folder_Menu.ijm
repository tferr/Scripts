/* List_Folder_Menu.ijm
 * IJ bar: https://github.com/tferr/Scripts#scripts
 *
 * An IJ1 Menu Tool providing a drop-down-list of the contents of a specified directory. The path of
 * the chosen directory is stored in the IJ preferences file (IJ_Prefs.txt) so that it is remembered
 * across restarts. Requires BAR_-1.X.X.jar to be installed in the plugins/ directory.
 *
 * This Menu tool improves file browsing within ImageJ. It offers commands to reveal directories in
 * the native file browser (including the directory from which the active image was loaded) and
 * commands to list directory contents to dedicated windows.
 *
 * TF, 2014.10
 */

var lflist = listFiles();
var lfMenu = newMenu("List Folder Menu Tool", lflist);
macro "List Folder Menu Tool - C037F06f7L04e4L03e3L1262L2151" {

	cmd = getArgument();

	if (startsWith(cmd, "Change") || endsWith(cmd, "not found...")) {
		chosen = getDirectory("Choose new directory to be listed in Menu");
		call("ij.Prefs.set", "bar.lfmt.", chosen);
		autoReload();
	}

	dir = call("ij.Prefs.get", "bar.lfmt.", "-");
	if (endsWith(cmd, "active image"))
		dir = getDirectory("image");
	if (dir=="") {
		showStatus("Error: Image path not available!"); //return;
	} else if (indexOf(cmd, "List files")!=-1) {
		call("bar.Utils.listDirectory", dir);
	} else if (startsWith(cmd, "Reveal ") || endsWith(cmd, " empty")) {
		call("bar.Utils.revealFile", dir);
	} else if (endsWith(cmd, "/")) {
		call("bar.Utils.revealFile", ""+ dir + cmd +"");
	} else
		open(dir + cmd);

}

function autoReload() {
	pathToThisFile = getDirectory("macros") + "tools"+ File.separator +"List_Folder_Menu.ijm";
	if (File.exists(pathToThisFile))
		run("Install...", "install=["+ pathToThisFile +"]");
	else
		showMessage("List Folder Menu Tool", "<html><div WIDTH=400>"
			+ "File not found:<br><i>"+ pathToThisFile +"</i><br><br>"
			+ "You must reinstall the tool manually to reload the new settings.</div>");
	exit();
}

function listFiles() {
	dir = call("ij.Prefs.get", "bar.lfmt.", "-");
	dirlength = lengthOf(dir);
	dirstring = dir;
	if (dirlength>30)
		dirstring = "..."+ substring(dir, dirlength-30, dirlength);
	items = newArray("Reveal path of active image","List files in path of active image","-","Change path...");
	if (!File.exists(dir))
		items = Array.concat(items, dirstring +" not found...");
	list = getFileList(dir);
	if (list.length==0)
		items = Array.concat(items, dirstring+" is empty");
	else
		items = Array.concat(items, "Reveal "+ dirstring, "List files in "+ dirstring, "-", list);
	return items;
}
