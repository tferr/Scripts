# Segmentation
  [Routines][Home] for partitioning images into analyzable parts.


## Edge Detection Routines
###[Shen-Castan Edge Detector](../BAR/README.md#bar-plugins)
   Java plugin implementing an Edge-detection filter according to Shen and Castan, CVGIP, 1992, 54 (2) 112-133 [[ref](../README.md#references)]. Effective alternative to Canny-Deriche filtering.
   ([Source code](../BAR/src/main/java/bar/ShenCastan.java))

   NB: Currently, it does not support RGB images directly: they need to be first  converted to _RGB-Stacks_: 1) Open RGB image; 2) Run _Image>Type>RGB-Stacks_; 3) Run the plugin, processing all 3 slices in the stack; 3) Run _Image>Color>Stack to RGB_ to obtain the initial data type.


## Threshold-based Routines
###[Apply Threshold To ROI](./Apply_Threshold_To_ROI.ijm)
   Applies Threshold values to the active ROI
   ([Download .ijm](./Apply_Threshold_To_ROI.ijm?raw=true))

###[Clear Thresholded Pixels](./Clear_Thresholded_Pixels.ijm)
   Clears thresholded voxels within the active ROI
   ([Download .ijm](./Clear_Thresholded_Pixels.ijm?raw=true))

###[Threshold From Background](./Threshold_From_Background.ijm)
   Sets a threshold based on a background ROI
   ([Download .ijm](./Threshold_From_Background.ijm?raw=true))

###[Wipe Background](./Wipe_Background.ijm)
   Clears thresholded particles of defined circularity & size within the active ROI
   ([Download .ijm](./Wipe_Background.ijm?raw=true))

###[Segment Profile Tool](../Tools/README.md#segment-profile)
   Described in [Tools](../Tools/README.md#segment-profile)




| [Home] | [Analysis] | [Segmentation] | [Morphometry] | [Tools] | [Plugins] | [Fiji documentation] |
|:------:|:----------:|:--------------:|:-------------:|:-------:|:---------:|:--------------------:|

[Home]: https://github.com/tferr/Scripts#ij-bar
[Analysis]: https://github.com/tferr/Scripts/tree/master/Data_Analysis#analysis
[Annotation]: https://github.com/tferr/Scripts/tree/master/Annotation#annotation
[Segmentation]: https://github.com/tferr/Scripts/tree/master/Segmentation#segmentation
[Morphometry]: https://github.com/tferr/Scripts/tree/master/Morphometry#morphometry
[Tools]: https://github.com/tferr/Scripts/tree/master/Tools#tools-and-toolsets
[Plugins]: https://github.com/tferr/Scripts/tree/master/BAR#bar-plugins
[Fiji documentation]: http://fiji.sc/BAR
