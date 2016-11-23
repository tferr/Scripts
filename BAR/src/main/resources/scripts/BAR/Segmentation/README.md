# Segmentation
  [Routines][Home] for partitioning images into analyzable parts.


## Edge Detection Routines
###[Shen-Castan Edge Detector](../BAR/README.md#bar-plugins)
   Java plugin implementing an Edge-detection filter according to Shen and Castan, CVGIP, 1992, 54
   (2) 112-133 ([DOI: 10.1016/1049-9652(92)90060-B](http://dx.doi.org/10.1016/1049-9652(92)90060-B)).
   ([Source code](../BAR/src/main/java/bar/plugin/ShenCastan.java))

It is a re-write of the [initial plugin]
(http://imagej.nih.gov/ij/plugins/inserm514/Documentation/Shen_Castan_514/Shen_Castan_514.html)
(that no longer works with IJ) written in 2004 by Maxime Pinchon under the supervision of Noël
Bonnet at the UMRS-INSERM 514.

Currently, it does not support RGB images directly: they need to be first  converted to
_RGB-Stacks_: 1) Open RGB image; 2) Run _Image>Type>RGB-Stacks_; 3) Run the plugin, processing all
three slices in the stack; 3) Run _Image>Color>Stack to RGB_ to obtain the initial data type.

## Morphological Operations
###[Remove Isolated Pixels](./Remove_Isolated_Pixels.bsh)
   Clears (erodes) isolated pixels in binary images. Operation is applied to the entire image or
   stack rather than the active ROI.


## Threshold-based Routines
###[Apply Threshold To ROI](./Apply_Threshold_To_ROI.ijm)
   Applies Threshold values to the active ROI.

###[Clear Thresholded Pixels](./Clear_Thresholded_Pixels.ijm)
   Clears thresholded voxels within the active ROI.

###[Threshold From Background](./Threshold_From_Background.ijm)
   Sets a threshold based on a background ROI.

###[Wipe Background](./Wipe_Background.ijm)
   Clears thresholded particles of defined circularity & size within the active ROI.

###[Segment Profile Tool](../../../../../../../Tools/README.md#segment-profile)
   Described in [Tools].




| [Home] | [Analysis] | [Data Analysis] | [Annotation] | [Segmentation] | [Tools] | [Plugins][Java Classes] | [lib] | [Snippets] | [IJ] |
|:------:|:----------:|:---------------:|:------------:|:--------------:|:-------:|:-----------------------:|:-----:|:----------:|:----:|

[Home]: https://github.com/tferr/Scripts#ij-bar
[Analysis]: https://github.com/tferr/Scripts/tree/master/BAR/src/main/resources/scripts/BAR/Analysis#analysis
[Annotation]: https://github.com/tferr/Scripts/tree/master/BAR/src/main/resources/scripts/BAR/Annotation#annotation
[Data Analysis]: https://github.com/tferr/Scripts/tree/master/BAR/src/main/resources/scripts/BAR/Data_Analysis#data-analysis
[Segmentation]: https://github.com/tferr/Scripts/tree/master/BAR/src/main/resources/scripts/BAR/Segmentation#segmentation
[Tools]: https://github.com/tferr/Scripts/tree/master/Tools#tools-and-toolsets
[Java Classes]: https://github.com/tferr/Scripts/tree/master/BAR#java-classes
[lib]: https://github.com/tferr/Scripts/tree/master/lib#lib
[Snippets]: https://github.com/tferr/Scripts/tree/master/Snippets#snippets
[IJ]: http://imagej.net/BAR
