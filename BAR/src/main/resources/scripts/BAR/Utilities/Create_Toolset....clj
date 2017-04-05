;@Context context
;@UIService uiService

(let [runner (bar.Runner. context)]
	(.runIJ1Macro runner "tools/ToolsetCreator.ijm" "")
	(if-not (.scriptLoaded runner)
		(.showDialog uiService "File could not be installed. See Console for details" "Error"))

	;;; Uncomment next line to have the macro opened in the script editor ;;;
	;(.openLastLoadedResource runner)
)
