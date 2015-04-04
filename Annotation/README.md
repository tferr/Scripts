# Annotation

[BARs][Home] related to annotation of images.

###[Combine Orthogonal Views](./Combine_Orthogonal_Views.ijm)
   Appends top, bottom and side views to a stack in a similar way to _Image>Stacks>Ortogonal Views_
   but extending projected views to the entire volume rather than displaying single slices. Empty
   spaces in the image canvas will be painted with background color (frame around the Color Picker
   Tool in the ImageJ toolbar).
   ([Download .ijm](./Combine_Orthogonal_Views.ijm?raw=true))

###[Cumulative Z-Project](./Cumulative_Z-Project.bsh)
   Produces cumulative projections using ImageJ's built-in projector (_Image>Stacks>Z Project..._).
   An immediate application of these progressive projections is the display of trailing paths of
   moving particles in timelapse experiments.
   ([Download .bsh](./Cumulative_Z-Project.bsh?raw=true))

###[ROI Color Coder](./ROI_Color_Coder.ijm)
   Colorizes ROI Manager selections by matching measurements to a lookup table (LUT),
   generating particle-size heat maps.
   ([Download .ijm](./ROI_Color_Coder.ijm?raw=true))
   ([Documentation page][RCC page])

   [![][RCC image]][RCC page]

   See Also [Calibration Menu](../Tools/README.md#calibration-menu)

[RCC page]: http://imagejdocu.tudor.lu/doku.php?id=macro:roi_color_coder
[RCC image]: http://imagejdocu.tudor.lu/lib/exe/fetch.php?cache=&media=macro:roicolorcoderoutput.png




| [Home] | [Analysis] | [Data Analysis] | [Annotation] | [Segmentation] | [Tools] | [Plugins] | [lib] | [Snippets] | [Fiji] |
|:------:|:----------:|:---------------:|:------------:|:--------------:|:-------:|:---------:|:-----:|:----------:|:------:|

[Home]: https://github.com/tferr/Scripts#ij-bar
[Analysis]: https://github.com/tferr/Scripts/tree/master/Analysis#analysis
[Data Analysis]: https://github.com/tferr/Scripts/tree/master/Data_Analysis#data-analysis
[Annotation]: https://github.com/tferr/Scripts/tree/master/Annotation#annotation
[Segmentation]: https://github.com/tferr/Scripts/tree/master/Segmentation#segmentation
[Morphometry]: https://github.com/tferr/Scripts/tree/master/Morphometry#morphometry
[Tools]: https://github.com/tferr/Scripts/tree/master/Tools#tools-and-toolsets
[Plugins]: https://github.com/tferr/Scripts/tree/master/BAR#bar-plugins
[lib]: https://github.com/tferr/Scripts/tree/master/lib#lib
[Snippets]: https://github.com/tferr/Scripts/tree/master/Snippets#snippets
[Fiji]: http://fiji.sc/BAR
