# BAR plugins

While macros and scripts form the bulk of [BAR][Home], some BAR commands are pre-compiled java plugins bundled in a single JAR file. This directory contains the source code (a [Maven project](http://fiji.sc/Maven)) for the `bar package` implementing such plugins. The project also includes [plugins.config](./src/main/resources/plugins.config) that organizes the BAR menu [hierarchy](#bar-menu).


### BAR-menu
The top-level _BAR_ Menu is organized in the following manner (version 1.0.3):

    BAR
    ├── Annotation
    │   ├── Move Menu (Context<>Main)
    │   └── ROI Color Coder
    ├── Data Analysis
    │   ├── Move Menu (Context<>Main)
    │   ├── Distribution Plotter
    │   ├── Find Peaks
    │   ├── Fit Polynomial
    │   └── Plot Results
    ├── Morphometry
    │   ├── Move Menu (Context<>Main)
    │   └── Strahler Analysis
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
    │   └── Reveal Snippets
    ├── Tool Installers
    │   ├── Install Calibration Menu
    │   ├── Install Segment Profile
    │   ├── Install Shortcuts Menu
    │   ├── ROI Manager Tools
    │   └── Toolset Creator...
    └── About BAR...



The relevant files get stored in the following locations (version 1.0.3):

    Fiji.app
    ├── macros
    │   ├── tools
    │   │   ├── Calibration_Menu.ijm
    │   │   ├── Segment_Profile.ijm
    │   │   └── Shortcuts_Menu.ijm
    │   └── toolsets
    │       ├── ROI Manager Tools.ijm
    │       └── Toolset Creator.ijm
    └── plugins
        ├── BAR_-1.0.3-SNAPSHOT.jar
        └── Scripts
            └── BAR
                ├── Annotation
                │   └── ROI_Color_Coder.ijm
                ├── Data_Analysis
                │   ├── Distribution_Plotter.ijm
                │   ├── Find_Peaks.bsh
                │   ├── Fit_Polynomial.bsh
                │   └── Plot_Results.bsh
                ├── Morphometry
                │   └── Strahler_Analysis.bsh
                ├── Segmentation
                │   ├── Apply_Threshold_To_ROI.ijm
                │   ├── Clear_Thresholded_Pixels.ijm
                │   ├── Remove_Isolated_Pixels.ijm
                │   ├── Threshold_From_Background.ijm
                │   └── Wipe_Background.ijm
                └── Snippets
                    ├── Median_Filter.py
                    └── Process_Folder.ijm


###Notes
   - You can place _BAR>_ submenus in the image's context menu (the menu that pops up when right-clicking on the image canvas) by using the _Move Menu (Context<>Main)_. This facilitates accessibility of commonly-used commands. The transfer is bi-directional: once in the context menu, chossing _Move Menu_ will replace the submenu in the main Menu bar.
   - While all files could be bundled in a single jar file (arguably a tidier approach), spreading files across folders has the main advantage of being compatible with the [Shift-trick](http://fiji.sc/BAR#OpeningBAR), while maintaining an organized menu hierarchy
   - Files are placed in the proper locations through a [shell script](../misc/symlink_bar.sh) (which is only useful for uploading files to the BAR [update site](http://sites.imagej.net/Tiago/))
   - The recursive lists above were created with [tree](http://mama.indstate.edu/users/ice/tree/)




| [Home] | [Analysis] | [Annotation] | [Segmentation] | [Morphometry] | [Tools] | [Plugins] | [Snippets] | [Fiji][Fiji documentation] |
|:------:|:----------:|:------------:|:--------------:|:-------------:|:-------:|:---------:|:----------:|:--------------------------:|

[Home]: https://github.com/tferr/Scripts#ij-bar
[Analysis]: https://github.com/tferr/Scripts/tree/master/Data_Analysis#analysis
[Annotation]: https://github.com/tferr/Scripts/tree/master/Annotation#annotation
[Segmentation]: https://github.com/tferr/Scripts/tree/master/Segmentation#segmentation
[Morphometry]: https://github.com/tferr/Scripts/tree/master/Morphometry#morphometry
[Tools]: https://github.com/tferr/Scripts/tree/master/Tools#tools-and-toolsets
[Plugins]: https://github.com/tferr/Scripts/tree/master/BAR#bar-plugins
[Snippets]: https://github.com/tferr/Scripts/tree/master/Snippets#snippets
[Fiji documentation]: http://fiji.sc/BAR
