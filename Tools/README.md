# Tools and Toolsets
[BARs][Home] that are installed in the ImageJ Toolbar and can be installed using the _BAR>Tool Installers_ menu (see [BAR Menu organization](../BAR/README.md#bar-menu)). 

## Tools
Single Tools are appended to the IJ toolbar and are typically saved in the `ImageJ/macro/tools/` directory. Note, however, that Tools placed in `ImageJ/plugins/Tools/` are automatically listed in the ">>" drop-menu in the main ImageJ window, which may be convenient when installing individual files manually.

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
Toolsets replace the user's toolbar and are listed in the toolbar's ">>" menu when saved in the `ImageJ/macros/toolsets` directory.

###[ROI Manager Tools](./Toolsets/ROI%20Manager%20Tools.ijm)
   Renames selections stored in the ROI Manager.
   ([Download .ijm](./Toolsets/ROI%20Manager%20Tools.ijm?raw=true))
   ([Documentation page](http://imagej.net/plugins/roi-manager-tools))

   See also [Image Annotation](../Annotation/README.md#annotation)


###[Toolset Creator](./Toolsets/Toolset%20Creator.ijm)
   Creates toolbar menus for running plugins, macros and scripts. It can also group
   built-in tools (such as drawing tools) in custom toolsets. It is distributed with ImageJ
   since IJ 1.41.
   ([Download .ijm](./Toolsets/Toolset%20Creator.ijm?raw=true))




| [Home] | [Analysis] | [Annotation] | [Segmentation] | [Morphometry] | [Plugins] | [Snippets] | [Fiji][Fiji documentation] |
|:------:|:----------:|:------------:|:--------------:|:-------------:|:---------:|:----------:|:--------------------------:|


[Home]: https://github.com/tferr/Scripts#ij-bar
[Analysis]: https://github.com/tferr/Scripts/tree/master/Data_Analysis#analysis
[Annotation]: https://github.com/tferr/Scripts/tree/master/Annotation#annotation
[Segmentation]: https://github.com/tferr/Scripts/tree/master/Segmentation#segmentation
[Morphometry]: https://github.com/tferr/Scripts/tree/master/Morphometry#morphometry
[Tools]: https://github.com/tferr/Scripts/tree/master/Tools#tools-and-toolsets
[Plugins]: https://github.com/tferr/Scripts/tree/master/BAR#bar-plugins
[Snippets]: https://github.com/tferr/Scripts/tree/master/Snippets#snippets
[Fiji documentation]: http://fiji.sc/BAR
