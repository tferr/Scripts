;@Context context
;@UIService uiService

(let [runner (bar.Runner. context)]
	(.installIJ1Macro runner "/tools/SegmentProfile.ijm" true)
	(if-not (.scriptLoaded runner)
		(.showDialog uiService "File could not be installed. See Console for details" "Error"))

	;;; Uncomment next line to have the macro opened in the script editor ;;;
	;(.openLastLoadedResource runner)
)
