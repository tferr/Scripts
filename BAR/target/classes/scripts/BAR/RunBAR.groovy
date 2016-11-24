//@LogService logService
//@ScriptService scriptService
//@UIService uiService

import bar.Utils
import java.io.*
import javax.script.ScriptException


boolean runBARScript(directory, filename) {
	try {
		path = "/scripts/BAR/$directory/$filename"
		success = false
		inStream = Utils.class.getResourceAsStream(path)
		if (inStream == null) {
			logService.error("Could not find script $path")
			return success
		}
		reader = new InputStreamReader(inStream)
		scriptService.run(filename, reader, true, null)
		success = true
	} catch (IOException e) {
		logService.error("There was an error reading $path")
	} catch (ScriptException e) {
		logService.error("There was an error running $path")
	} finally {
		return success
	}
}

if (!runBARScript("Data_Analysis", "Distribution_Plotter.ijm")) {
	uiService.showDialog("Script did not execute. Check console for details", "Error")
}
return
