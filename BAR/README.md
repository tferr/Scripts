# BAR plugins

While macros and scripts form the bulk of [BAR][Home], some BAR commands are pre-compiled java plugins bundled in a single JAR file. This directory contains the source code (a [Maven project](http://fiji.sc/Maven)) for the `bar package` implementing such plugins. The project also includes [plugins.config](./src/main/resources/plugins.config) that organizes the BAR menu [hierarchy](#bar-menu).


### BAR-menu
The top-level _BAR_ Menu is organized in the following manner (version 1.0.2):

    BAR
    ├── Annotation
    │   └── ROI Color Coder
    ├── Data Analysis
    │   ├── Distribution Plotter
    │   ├── Find Peaks
    │   ├── Fit Polynomial
    │   └── Plot Results
    ├── Morphometry
    │   └── Strahler Analysis
    ├── Segmentation
    │   ├── Shen-Castan Edge Detector
    │   ├── Apply Threshold To ROI
    │   ├── Clear Thresholded Pixels
    │   ├── Threshold From Background
    │   └── Wipe Background
    ├── Tool Installers
    │   ├── Install Calibration Menu
    │   ├── Install Segment Profile
    │   ├── Install Shortcuts Menu
    │   ├── ROI Manager Tools
    │   └── Toolset Creator...
    └── About BAR...



The relevant files get stored in the following locations (version 1.0.2):

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
        ├── BAR_-1.0.2-SNAPSHOT.jar
        └── Scripts
            └── BAR
                ├── Annotation
                │   └── ROI_Color_Coder.ijm
                ├── Data Analysis
                │   ├── Distribution_Plotter.ijm
                │   ├── Find_Peaks.bsh
                │   ├── Fit_Polynomial.bsh
                │   └── Plot_Results.bsh
                ├── Morphometry
                │   └── Strahler_Analysis.bsh
                └── Segmentation
                    ├── Apply_Threshold_To_ROI.ijm
                    ├── Clear_Thresholded_Pixels.ijm
                    ├── Threshold_From_Background.ijm
                    └── Wipe_Background.ijm


###Notes
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
