//@Context context
//@UIService uiservice


importClass(Packages.bar.Runner)

var runner = new Runner(context)
runner.installIJ1Macro("tools/InsertMagnifiedROI.ijm", true)
if (!runner.scriptLoaded()) {
	uiservice.showDialog("File could not be installed. See Console for details.")
}

//Uncomment next line to have the macro opened in the script editor
//runner.openLastLoadedResource()
