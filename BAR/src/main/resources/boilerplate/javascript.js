//@ImageJ ij
//@UIService ui
//@LogService log
//@ScriptService script
//@DisplayService display

//header
importClass(Packages.bar.Utils);


function run() {

	// Load template BAR lib. Exit if file is not available
	// (see 'BAR>Utilities>Install Multi-language libs...')
	try {
		load(Utils.getLibDir() + "BARlib.js");
	} catch (e if e instanceof TypeError) {
		ui.showDialog("File not found: BARlib.js", "Error")
		log.error(e)
		return;
	}

	// Your code here... e.g., confirm access to loaded file
	lib = new BARlib();  // main function in BARlib.js
	lib.confirmLoading();

}

run()
