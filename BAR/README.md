# BAR plugins

While most of BAR commands are macros and scripts, some BAR commands are pre-compiled java plugins bundled in a single JAR file. This directory contains the source code (a [Maven project](http://fiji.sc/Maven)) for the `bar package` implementing such plugins. The project also includes [plugins.config](./src/main/resources/plugins.config) that organizes the BAR menu [hierarchy](#bar-menu).


### BAR-menu
The top-level _BAR_ Menu is organized in the following manner (version 1.0.2):

    BAR
    ├── About BAR...
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
    │   ├── Apply Threshold To ROI
    │   ├── Clear Thresholded Pixels
    │   ├── Threshold From Background
    │   ├── Wipe Background
    │   └── Shen-Castan Edge Detector
    └── Tool Installers
        ├── Install Calibration Menu
        ├── Install Segment Profile
        ├── Install Shortcuts Menu
        ├── Install ROI Manager Tools
        └── Toolset Creator...


The relevant files get stored in the following locations (version 1.0.2):

    Fiji.app
    ├── macros
    │   ├── tools
    │   │   ├── Calibration_Menu.ijm
    │   │   ├── Segment_Profile.ijm
    │   │   └── Shortcuts_Menu.ijm
    │   └── toolsets
    │       ├── ROI Manager Tools.ijm
    │       └── Toolset Creator.ijm
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
   - While all files could be bundled in a single jar file (arguably a tidier approach), spreading files across folders has the sole advantage of being compatible with the [Shift-trick](http://fiji.sc/BAR#OpeningBAR), while maintaining an organized menu hierarchy
   - Files are placed in the proper locations through a [script](../misc/symlink_bar.sh) (which is only useful when uploading files to the BAR [update site](http://sites.imagej.net/Tiago/)
   - The recursive lists above were created with [tree](http://mama.indstate.edu/users/ice/tree/)

[ [BAR's Home] ](../README.md#scripts)