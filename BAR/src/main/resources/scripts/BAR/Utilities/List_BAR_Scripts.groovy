//@ImageJ ij
//@UIService uiservice
//@ScriptService scriptservice
//@CommandService commandservice
//@DisplayService displayservice
//@TextService textservice


import bar.Runner
import bar.Utils
import org.scijava.util.FileUtils;

def cleanIdentifier(identifier, prefix) {
	if (identifier.startsWith(prefix))
		identifier -= prefix
    identifier.replaceAll("_", " ")
    identifier
}

def appendScriptList(string) {
	def scripts = scriptservice.getScripts()
	string += "\n## Scripts\n"
	if (!scripts) {
		string += " - Could not find any scripts\n"
		return string
	}
	for (script in scripts) {
		identifier = script.getIdentifier()
		if (identifier.contains("BAR/"))
			string += " 1. ${cleanIdentifier(identifier, "script:")}\n"
	}
	return string
}

def appendCommandList(string) {
	def commands = commandservice.getCommands()
	string += "\n## SciJava Commands\n"
	if (!commands) {
		string += " - Could not find any commands\n"
		return string
	}
	for (command in commands) {
		identifier = command.getIdentifier()
		if (identifier.contains("bar."))
			string += " 1. ${cleanIdentifier(identifier, "command:")}\n"
	}
	return string
}

def appendResources(string, directories) {
	string += "\n## Resources\n"
	for (dir in directories) {	
		url = Utils.class.getResource("/$dir/")
		list = FileUtils.listContents(url)
		string += "\n 1. **$dir**\n"
		if (!list) {
			string += "  - Could not find any files\n"
			continue
		}
		for (item in list) {
			item = item.toString()
			string += "  1. ${item.drop(item.lastIndexOf("/"))}\n"
		}
	}
	return string
}

def appendIJ1plugins(string) {
	string += "\n## IJ1 Plugins\n"
	config = new Runner(ij.getContext()).readContents("/plugins.config")
	if (!config) {
		string += " - Could not find any plugins\n"
		return string
	}
	for (line in config.split("\n")) {
		if (line.startsWith("#"))
			continue
		nameLine = line.split("\"")
		if (nameLine && nameLine.size() > 1)
			string += " 1. ${nameLine[1]}\n"
	}
	return string
}

// Append all lists
text = appendScriptList("# List of BAR Files")
text = appendCommandList(text)
text = appendIJ1plugins(text)
text = appendResources(text, ["boilerplate", "lib", "script_templates",
		"tools", "tutorials"])

// Include remaining sources
text += "\n## This List Does Not Include:\n"
text += " * Unregistered scripts in your local directories, including:\n"
for (item in [Utils.getBARDir(), Utils.getLibDir(), Utils.getMyRoutinesDir()])
	text += "  * ${item}\n"


// Render markdown list
temp = File.createTempFile('barlist', '.md')
temp.write(text)
displayservice.createDisplay("List of BAR Files", textservice.asHTML(temp))
temp.delete()
