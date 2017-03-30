//@UIService uiservice
//@ScriptService scriptservice
//@DisplayService displayservice
//@TextService textservice

def cleanIdentifier = {
    it.startsWith('script:') ? it - 'script:' : it
    it.replaceAll("_", " ")
}


scripts = scriptservice.getScripts()
if (!scripts) {
	uiservice.showDialog("Could not find any scripts", "Error")
	return
}
text = "##Installed BAR Scripts\n"
for (script in scripts) {
	identifier = script.getIdentifier()
	if (identifier.contains("BAR/"))
		text += " 1. ${cleanIdentifier(identifier)}\n"
}
text += "\n##This List Does Not Include:\n"
for (item in ["Scripts listed in the Script Editor _Templates_ Menu", 
		"Compiled Java files (Use the Command Finder to list those)"])
	text += " 1. ${item}\n"

temp = File.createTempFile('barlist', '.md')
temp.write(text)
displayservice.createDisplay("List of BAR scripts", textservice.asHTML(temp))
temp.delete()
