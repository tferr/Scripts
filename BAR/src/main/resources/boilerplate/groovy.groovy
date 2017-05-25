//@ImageJ ij
//@UIService ui
//@LogService log
//@ScriptService script
//@DisplayService display

//header
import bar.Utils


/**
 * Parses the given file into a class and initializes it
 *
 * @param filename the file to be parsed (in the BAR lib directory)
 * @return a reference to the initialized lib class
 */
def loadBARLib(filename) {
	file = new File(Utils.getLibDir() + filename)
	lib = null
	if (file.exists()) {
		loader = new GroovyClassLoader()
		lib = loader.parseClass(file).newInstance()
	}
	lib
}


def main() {

	// Load template BAR lib. Exit if file is not available
	// (see 'BAR>Utilities>Install Multi-language libs...')
	lib = loadBARLib("BARlib.groovy")
	if (!lib) {
		ui.showDialog("File not found: BARlib.groovy", "Error")
		return
	}

	// Your code here... e.g., confirm access to loaded file
	//lib.metaClass.respondsTo(lib, "confirmLoading")
	lib.confirmLoading()

}

main()
