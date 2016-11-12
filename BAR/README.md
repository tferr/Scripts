# BAR plugins

While macros and scripts form the bulk of [BAR][Home], some BAR commands are pre-compiled
java plugins bundled in a single JAR file. This directory contains the source code (a
[Maven project](http://imagej.net/Maven)) for the `bar package` implementing such plugins.
The project also includes [plugins.config](./src/main/resources/plugins.config) that
organizes the BAR menu [hierarchy](#bar-menu).

### List of Java plugins
  1. [BAR Commander](./src/main/java/bar/plugin/Commander.java), a keyboard-based file browser
  1. [BAR Utils](./src/main/java/bar/Utils.java), class providing convenience methods to script BAR
  1. [Shen-Castan Edge Detector](./src/main/java/bar/plugin/ShenCastan.java), described in
     [Segmentation](../Segmentation/README.md#shen-castan-edge-detector)
  1. [Snippet Creator](./src/main/java/bar/plugin/SnippetCreator.java), described in
     [Snippets](../Snippets/README.md#snippets) and [lib](../lib/README.md#lib)


### BAR-menu
The top-level _BAR_ Menu is organized in the following manner (version 1.x.x):

    BAR
    ├── Annotation
    │   ├── Move Menu (Context<>Main)
    │   ├── Combine Orthogonal Views
    │   └── ROI Color Coder
    ├── Data Analysis
    │   ├── Move Menu (Context<>Main)
    │   ├── Clipboard to Results
    │   ├── Distribution Plotter
    │   ├── Find Peaks
    │   ├── Fit Polynomial
    │   ├── Multichannel Plot Profile
    │   └── Plot Results
    ├── Segmentation
    │   ├── Move Menu (Context<>Main)
    │   ├── Shen-Castan Edge Detector
    │   ├── Apply Threshold To ROI
    │   ├── Clear Thresholded Pixels
    │   ├── Remove Isolated Pixels
    │   ├── Threshold From Background
    │   └── Wipe Background
    ├── Snippets
    │   ├── List Snippets
    │   ├── Reveal Snippets
    │   └── [...]
    ├── Tool Installers
    │   ├── Install Calibration Menu
    │   ├── Install List Folder Menu
    │   ├── Install Segment Profile
    │   ├── Install Shortcuts Menu
    │   ├── ROI Manager Tools
    │   └── Toolset Creator...
    └── About BAR...



The relevant files get stored in the following locations (version 1.x.x):

    Fiji.app
    ├── macros
    │   ├── tools
    │   │   ├── Calibration_Menu.ijm
    │   │   ├── List_Folder_Menu.ijm
    │   │   ├── Segment_Profile.ijm
    │   │   └── Shortcuts_Menu.ijm
    │   └── toolsets
    │       ├── ROI Manager Tools.ijm
    │       └── Toolset Creator.ijm
    └── plugins
        ├── BAR_-1.0.5.jar
        └── Scripts
            └── BAR
                ├── Annotation
                │   ├── Combine_Orthogonal_Views.ijm
                │   └── ROI_Color_Coder.ijm
                ├── Data_Analysis
                │   ├── Clipboard_to_Results.py
                │   ├── Distribution_Plotter.ijm
                │   ├── Find_Peaks.bsh
                │   ├── Fit_Polynomial.bsh
                │   ├── Multichannel_Plot_Profile.bsh
                │   └── Plot_Results.bsh
                ├── Segmentation
                │   ├── Apply_Threshold_To_ROI.ijm
                │   ├── Clear_Thresholded_Pixels.ijm
                │   ├── Remove_Isolated_Pixels.bsh
                │   ├── Threshold_From_Background.ijm
                │   └── Wipe_Background.ijm
                └── Snippets
                    ├── Median_Filter.py
                    ├── NN_Distances.py
                    ├── Process_Folder_IJM.ijm
                    ├── Process_Folder_PY.py
                    └── Search_Snippets.bsh


###Notes
   - You can place _BAR>_ submenus in the image's context menu (the menu that pops up when
   right-clicking on the image canvas) by using the _Move Menu (Context<>Main)_. This
   facilitates accessibility of commonly-used commands. The transfer is bi-directional:
   once in the context menu, choosing _Move Menu_ will replace the submenu in the main
   Menu bar.
   - While all files could be bundled in a single jar file (arguably a tidier approach),
   spreading files across folders has the main advantage of being compatible with the
   [Shift-trick](http://imagej.net/BAR#OpeningBAR), while maintaining an organized menu
   hierarchy
   - Files are placed in the proper locations through a [shell script](../misc/symlink_bar.sh)
   (which is only useful for uploading files to the BAR [update site](http://sites.imagej.net/Tiago/))
   - The recursive lists above were created with [tree](http://mama.indstate.edu/users/ice/tree/)




| [Home] | [Analysis] | [Data Analysis] | [Annotation] | [Segmentation] | [Tools] | [Plugins] | [lib] | [Snippets] | [IJ] |
|:------:|:----------:|:---------------:|:------------:|:--------------:|:-------:|:---------:|:-----:|:----------:|:----:|

[Home]: https://github.com/tferr/Scripts#ij-bar
[Analysis]: https://github.com/tferr/Scripts/tree/master/Analysis#analysis
[Data Analysis]: https://github.com/tferr/Scripts/tree/master/BAR/src/main/resources/scripts/BAR/Data_Analysis#data-analysis
[Annotation]: https://github.com/tferr/Scripts/tree/master/Annotation#annotation
[Segmentation]: https://github.com/tferr/Scripts/tree/master/Segmentation#segmentation
[Tools]: https://github.com/tferr/Scripts/tree/master/Tools#tools-and-toolsets
[Plugins]: https://github.com/tferr/Scripts/tree/master/BAR#bar-plugins
[lib]: https://github.com/tferr/Scripts/tree/master/lib#lib
[Snippets]: https://github.com/tferr/Scripts/tree/master/Snippets#snippets
[IJ]: http://imagej.net/BAR
