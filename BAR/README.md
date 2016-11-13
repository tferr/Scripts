# Java Classes

While macros and scripts form the bulk of [BAR][Home], some BAR commands are pre-compiled
java plugins This directory contains the source code (a
[Maven project](http://imagej.net/Maven)) for the `bar package`, including:

### Plugins
  1. [BAR Commander](./src/main/java/bar/plugin/Commander.java), a keyboard-based file browser
  1. [Interactive Plotter](./src/main/java/bar/plugin/InteractivePlotter.java), described in
     [Data_Analysis](./src/main/resources/scripts/BAR/Data_Analysis/README.md#interactive-plotting)
  1. [Shen-Castan Edge Detector](./src/main/java/bar/plugin/ShenCastan.java), described in
     [Segmentation](../Segmentation/README.md#shen-castan-edge-detector)
  1. [Snippet Creator](./src/main/java/bar/plugin/SnippetCreator.java), described in
     [Snippets](../Snippets/README.md#snippets) and [lib](../lib/README.md#lib)

### Utilities
  1. [Utils](./src/main/java/bar/Utils.java), class providing convenience methods to script ImageJ
  1. [PlotUtils](./src/main/java/bar/Utils.java), class providing utility methods that improve ImageJ's plotting capabilities

### External Ops
  1. Classes exemplifying how to [add new ops](http://imagej.net/Adding_new_ops) outside the core imagej-ops project.

###Notes
   - Files are uploaded to the BAR [update site](http://sites.imagej.net/Tiago/) manually,
   through a [shell script](./start-upload.sh).



| [Home] | [Analysis] | [Data Analysis] | [Annotation] | [Segmentation] | [Tools] | [Plugins][Java Classes] | [lib] | [Snippets] | [IJ] |
|:------:|:----------:|:---------------:|:------------:|:--------------:|:-------:|:-----------------------:|:-----:|:----------:|:----:|

[Home]: https://github.com/tferr/Scripts#ij-bar
[Analysis]: https://github.com/tferr/Scripts/tree/master/Analysis#analysis
[Data Analysis]: https://github.com/tferr/Scripts/tree/master/BAR/src/main/resources/scripts/BAR/Data_Analysis#data-analysis
[Annotation]: https://github.com/tferr/Scripts/tree/master/Annotation#annotation
[Segmentation]: https://github.com/tferr/Scripts/tree/master/Segmentation#segmentation
[Tools]: https://github.com/tferr/Scripts/tree/master/Tools#tools-and-toolsets
[Java Classes]: https://github.com/tferr/Scripts/tree/master/BAR#java-classes
[lib]: https://github.com/tferr/Scripts/tree/master/lib#lib
[Snippets]: https://github.com/tferr/Scripts/tree/master/Snippets#snippets
[IJ]: http://imagej.net/BAR
