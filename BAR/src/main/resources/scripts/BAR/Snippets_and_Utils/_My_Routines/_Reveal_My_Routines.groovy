//@UIService uiservice

import bar.Utils

path = Utils.getSnippetsDir()
dir = new File(path)
if (dir.mkdirs)
	Utils.revealFile(dir)
else
	uiservice.showDialog("Sorry, Could not open $path", "Error")
