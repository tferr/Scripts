# Misc

This directories contain miscellaneous scripts that are too narrow in scope to be included in the main [BAR][Home] hierarchy.

###[Capture Window](./CaptureWindow.ijm)
A Mac-specific macro that calls the _Grab.app_ (part of the Mac OS) to capture
a snapshot of an ImageJ window.
([Download .ijm](./CaptureWindow.ijm?raw=true))

###[Clipboard to Results](./Clipboard_to_Results.py)
   Imports numeric values (delimiter-separated values), copied to the clipboard into the
   _Results_ table. It was of utility prior to BAR v1.1.7, when BARs that analyzed tabular
   data could only read values from the main IJ "Results" table.
   ([Download .py](./Clipboard_to_Results.py?raw=true))

###[Extract Bouts From Tracks](./Extract_Bouts_From_Tracks.py)
A Jython script that segregates videotracked paths (e.g., those obtained from
[TrackMate](http://imagej.net/TrackMate)) into "Moving" and "Motionless" bouts
according to predefined spatial and temporal constraints.
([Download .bsh](./Extract_Bouts_From_Tracks.py?raw=true))



| [Home] | [Analysis] | [Data Analysis] | [Annotation] | [Segmentation] | [Tools] | [Plugins] | [lib] | [Snippets] | [IJ] |
|:------:|:----------:|:---------------:|:------------:|:--------------:|:-------:|:---------:|:-----:|:----------:|:----:|

[Home]: https://github.com/tferr/Scripts#ij-bar
[Analysis]: https://github.com/tferr/Scripts/tree/master/Analysis#analysis
[Data Analysis]: https://github.com/tferr/Scripts/tree/master/Data_Analysis#data-analysis
[Annotation]: https://github.com/tferr/Scripts/tree/master/Annotation#annotation
[Segmentation]: https://github.com/tferr/Scripts/tree/master/Segmentation#segmentation
[Tools]: https://github.com/tferr/Scripts/tree/master/Tools#tools-and-toolsets
[Plugins]: https://github.com/tferr/Scripts/tree/master/BAR#bar-plugins
[lib]: https://github.com/tferr/Scripts/tree/master/lib#lib
[Snippets]: https://github.com/tferr/Scripts/tree/master/Snippets#snippets
[IJ]: http://imagej.net/BAR
