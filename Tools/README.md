# Tools and Toolsets
[BARs](../README.md#scripts) that are installed in the ImageJ Toolbar.
Single _Tools_ should be be placed in `ImageJ/plugins/Tools/` and are appended to the IJ
toolbar. _Toolsets_ replace the user's toolbar and should be placed in `ImageJ/macros/toolsets`.
Both can be loaded using the ">>" drop-down menu in the main ImageJ window.

## Tools
Tools are listed in the toolbar's ">>" menu when saved in `ImageJ/plugins/Tools/`

###[Calibration Menu](./Calibration_Menu.ijm)
   Provides shortcuts for spatial calibration of images lacking metadata.
   ([Download .ijm](./Calibration_Menu.ijm?raw=true))


###[Segment Profile](./Segment_Profile.ijm)<a name="segment-profile-tool"></a>
   Extracts the segments within a straight line that are above a cutoff threshold.
   ([Download .ijm](./Segment_Profile.ijm?raw=true))

   (See also [Image Segmentation](../Segmentation/README.md#segmentation))


###[Shortcuts Menu](./Shortcuts_Menu.ijm)
   Lists the user's most used commands. List is remembered across restarts.
   ([Download .ijm](./Shortcuts_Menu.ijm?raw=true))


## Toolsets
Toolsets are listed in the toolbar's ">>" menu when saved in `ImageJ/macros/toolsets`

###[ROI Manager Tools](./toolsets/ROI%20Manager%20Tools.ijm)
   Renames selections stored in the ROI Manager.
   ([Download .ijm](./Toolsets/ROI%20Manager%20Tools.ijm?raw=true))
   ([Documentation page](http://imagej.net/plugins/roi-manager-tools))

   See also [Image Annotation](../Annotation/README.md#annotation)


###[Toolset Creator](./toolsets/Toolset%20Creator.txt)
   Creates toolbar menus for running plugins, macros and scripts. It can also group
   built-in tools (such as drawing tools) in custom toolsets. It is distributed with ImageJ
   since IJ 1.41.
   ([Download .ijm](./Toolsets/Toolset%20Creator.ijm?raw=true))


[ [BAR's Home] ](../README.md#scripts)
[ [BAR documentation page] ](http://fiji.sc/BAR)