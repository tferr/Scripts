//@ImageJ ij
//@UIService uiservice
//@ScriptService scriptservice
//@CommandService commandservice
//@DisplayService displayservice
//@TextService textservice
//@UIService uiservice

import bar.Runner
import bar.Utils
import groovy.io.FileType
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
		url = Utils.getBARresource("$dir/")
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
	config = new Runner(ij.getContext()).readContents("plugins.config")
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

def appendLocalFiles(string) {
	def header = FileUtils.shortenPath(Utils.getBARDir())
	string += "\n## Local Files ($header)\n"
	def dir = new File(Utils.getBARDir())
	dir.eachFileRecurse(FileType.FILES) {
		string += " 1. ${it.path - dir.path}\n"
	}
	return string
}

// List resources in BAR jar file
text = appendScriptList("# BAR Files")
text = appendCommandList(text)
text = appendIJ1plugins(text)
text = appendResources(text, ["boilerplate", "lib", "script_templates",
		"shared", "tools", "tutorials"])

// List user files on local installation
text = appendLocalFiles(text)

// Render markdown list
temp = File.createTempFile('barlist', '.md')
temp.write(text)
displayservice.createDisplay("BAR Files", textservice.asHTML(temp))
temp.delete()

// Check for legacy installation files
if (new File(Utils.getPluginsBasedBARDir()).list()) {
	uiservice.showDialog("<html><div WIDTH=450>With BAR v1.5 and newer, local files are no "
		+ "longer rooted in the plugins directory. The new, much more convenient directory "
		+ "for your local files is <b>${Utils.getBARDir()}</b>. You should move any files "
		+ "in the old location to the new one (and safely delete the old directory).<br><br>"
		+ "Old location: <i>${Utils.getPluginsBasedBARDir()}</i><br>"
		+ "New location: <i>${Utils.getBARDir()}</i>", "Legacy Warning")
}
