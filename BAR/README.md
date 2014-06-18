# BAR Specific Files

This directory contains BAR-specific files (namely [installers](./installers/) for IJ1 macro files 
and [routines](./linkToFiji.sh) that generate the _BAR_ Menu hierarchy). They are only useful in the
context of the BAR Update Site.

The top-level _BAR_ Menu is organized in the following manner:

    BAR
    ├── Analysis
    │   ├── Distribution Plotter
    │   ├── Find Peaks
    │   ├── Fit Polynomial
    │   └── Plot Results
    ├── Annotation
    │   └── ROI Color Coder
    ├── Morphometry
    │   └── Strahler Analysis
    ├── Segmentation
    │   ├── Apply Threshold To ROI
    │   ├── Clear Thresholded Pixels
    │   ├── Threshold From Background
    │   └── Wipe Background
    └── Tool Installers
        ├── Install Calibration Menu
        ├── Install ROI Manager Tools
        ├── Install Segment Profile
        ├── Install Shortcuts Menu
        └── Install Toolset Creator


The relevant files get stored in:

    Fiji.app
    ├── macros
    │   ├── tools
    │   │   ├── Calibration_Menu.ijm
    │   │   ├── Segment_Profile.ijm
    │   │   └── Shortcuts_Menu.ijm
    │   └── toolsets
    │       ├── ROI_Manager_Tools.ijm
    │       └── Toolset_Creator.ijm
    └── plugins
        └── Scripts
            └── BAR
                ├── Analysis
                │   ├── Distribution_Plotter.ijm
                │   ├── Find_Peaks.bsh
                │   ├── Fit_Polynomial.bsh
                │   ├── Plot_Results.bsh
                │   └── README.md
                ├── Annotation
                │   ├── README.md
                │   └── ROI_Color_Coder.ijm
                ├── Morphometry
                │   ├── README.md
                │   └── Strahler_Analysis.bsh
                ├── README.md
                ├── Segmentation
                │   ├── Apply_Threshold_To_ROI.ijm
                │   ├── Clear_Thresholded_Pixels.ijm
                │   ├── README.md
                │   ├── Threshold_From_Background.ijm
                │   └── Wipe_Background.ijm
                └── Tool_Installers
                    ├── Install_Calibration_Menu.bsh
                    ├── Install_ROI_Manager_Tools.bsh
                    ├── Install_Segment_Profile.bsh
                    ├── Install_Shortcuts_Menu.bsh
                    └── Install_Toolset_Creator.bsh

(Recursive lists created with [tree](http://mama.indstate.edu/users/ice/tree/))

[ [Home] ](../README.md#scripts)