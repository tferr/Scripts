# Segmentation

Routines for partitioning images into analyzable parts.


## Edge Detection

### Shen-Castan Edge Detector
Java plugin implementing an Edge-detection filter according to Shen and Castan, CVGIP,
1992, 54 (2)
112-133([DOI: 10.1016/1049-9652(92)90060-B](http://dx.doi.org/10.1016/1049-9652(92)90060-B)).

It is a re-write of the
[initial plugin](http://imagej.nih.gov/ij/plugins/inserm514/Documentation/Shen_Castan_514/Shen_Castan_514.html)
(that no longer works with IJ) written in 2004 by Maxime Pinchon under the supervision of
Noël Bonnet at the UMRS-INSERM 514. Currently, it does not support RGB images directly:
they need to be first  converted to _RGB-Stacks_:

1. Open RGB image
2. Run `Image>Type>RGB-Stacks`
3. Run the plugin, processing all three slices in the stack
4. Run `Image>Color>Stack to RGB` to obtain the initial data type


## Morphological Operations

### Remove Isolated Pixels
Clears (erodes) isolated pixels in binary images. Operation is applied to the entire image
or stack rather than the active ROI.


## Threshold-based Routines

### Apply Threshold To ROI
Applies Threshold values to the active ROI.

### Clear Thresholded Pixels
Clears thresholded voxels within the active ROI.

### Threshold From Background
Sets a threshold based on a background ROI.

### Wipe Background
Clears thresholded particles of defined circularity & size within the active ROI.

### Segment Profile Tool
Extracts the segments within a straight line that are above a cutoff threshold. Installs
in the IJ toolbar.



------
| [Home] | [Analysis] | [Annotation] | [Data Analysis] | [lib] | [My Routines] | [Segmentation] | [Tools] | [Utilities] | [Wiki] |

[Home]: https://github.com/tferr/Scripts
[Analysis]: https://github.com/tferr/Scripts/tree/master/BAR/src/main/resources/scripts/BAR/Analysis
[Annotation]: https://github.com/tferr/Scripts/tree/master/BAR/src/main/resources/scripts/BAR/Annotation
[Data Analysis]: https://github.com/tferr/Scripts/tree/master/BAR/src/main/resources/scripts/BAR/Data_Analysis
[lib]: https://github.com/tferr/Scripts/tree/master//BAR/src/main/resources/scripts/BAR/lib
[My Routines]: https://github.com/tferr/Scripts/tree/master/BAR/src/main/resources/scripts/BAR/My_Routines
[Segmentation]: https://github.com/tferr/Scripts/tree/master/BAR/src/main/resources/scripts/BAR/Segmentation
[Tools]: https://github.com/tferr/Scripts/tree/master//BAR/src/main/resources/scripts/BAR/tools
[Utilities]: https://github.com/tferr/Scripts/tree/master//BAR/src/main/resources/scripts/BAR/Utilities
[Wiki]: https://imagej.net/BAR
