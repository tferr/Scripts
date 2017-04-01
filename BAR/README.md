# Java Classes

While scripts form the bulk of [BAR][Home], some BAR commands are pre-compiled
java plugins This directory contains the source code (a
[Maven project](http://imagej.net/Maven)) for the `bar package`, including:


## Utilities
1. [Utils](./src/main/java/bar/Utils.java), class providing convenience methods to script
   ImageJ
1. [PlotUtils](./src/main/java/bar/Utils.java), class providing utility methods that
   improve ImageJ's plotting capabilities
1. [Runner](./src/main/java/bar/Runner.java), providing convenience methods to run scripts
   loaded from JAR files


## External Ops
1. Classes exemplifying how to provide [external ops](./EXTERNAL_OPS.md) outside the core imagej-ops project.
   This work is explained in more detail on the [ImageJ website](http://imagej.net/Adding_new_ops).


## Commands and Plugins
1. [BAR Commander](./src/main/java/bar/plugin/Commander.java), described in [Utilities]
1. [Interactive Plotter](./src/main/java/bar/plugin/InteractivePlotter.java), described in
   [Data Analysis]
1. [Shen-Castan Edge Detector](./src/main/java/bar/plugin/ShenCastan.java), described in
   [Segmentation]
1. [Snippet Creator](./src/main/java/bar/plugin/SnippetCreator.java), described in
   [Utilities]



------
| [Home] | [Analysis] | [Annotation] | [Data Analysis] | [lib] | [My Routines] | [Segmentation] | [Tools] | [Utilities] | [Wiki] |

[Home]: https://github.com/tferr/Scripts
[Analysis]: https://github.com/tferr/Scripts/tree/master/BAR/src/main/resources/scripts/BAR/Analysis
[Annotation]: https://github.com/tferr/Scripts/tree/master/BAR/src/main/resources/scripts/BAR/Annotation
[Data Analysis]: https://github.com/tferr/Scripts/tree/master/BAR/src/main/resources/scripts/BAR/Data_Analysis
[lib]: https://github.com/tferr/Scripts/tree/master//BAR/src/main/resources/scripts/BAR/lib
[My Routines]: https://github.com/tferr/Scripts/tree/master/BAR/src/main/resources/scripts/BAR/My_Routines
[Segmentation]: https://github.com/tferr/Scripts/tree/master/BAR/src/main/resources/scripts/BAR/Segmentation
[Tools]: https://github.com/tferr/Scripts/tree/master//BAR/src/main/resources/scripts/BAR/tools
[Utilities]: https://github.com/tferr/Scripts/tree/master//BAR/src/main/resources/scripts/BAR/Utilities
[Wiki]: https://imagej.net/BAR
