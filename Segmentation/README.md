# Segmentation

Thresholding [BARs](../README.md#scripts).


###[Apply Threshold To ROI](./Apply_Threshold_To_ROI.ijm)
   Applies Threshold values to the active ROI
   ([Download .ijm](./Apply_Threshold_To_ROI.ijm?raw=true))

###[Clear Thresholded Pixels](./Clear_Thresholded_Pixels.ijm)
   Clears thresholded voxels within the active ROI
   ([Download .ijm](./Clear_Thresholded_Pixels.ijm?raw=true))

###[Threshold From Background](./Threshold_From_Background.ijm)
   Sets a threshold based on a background ROI
   ([Download .ijm](./Threshold_From_Background.ijm?raw=true))

###[ShenCastan Edge Detector](./ShenCastan_Edge_Detector.java)
   Edge-detection filter according to Shen and Castan, CVGIP, 1992, 54 (2) 112-133. Effective alternative to Canny-Deriche filtering.
   ([Download .java](./ShenCastan_Edge_Detector.java?raw=true))

   NB: Currently, it does not support RGB images directly: they need to be first  converted to _RGB-Stacks_: 1) Open RGB image; 2) Run _Image>Type>RGB-Stacks_; 3) Run the plugin, processing all 3 slices in the stack; 3) Run _Image>Color>Stack to RGB_ to obtain the initial data type.

###[Wipe Background](./Wipe_Background.ijm)
   Clears thresholded particles of defined circularity & size within the active ROI
   ([Download .ijm](./Wipe_Background.ijm?raw=true))


##See Also

* [Segment Profile Tool](../Tools/README.md#segment-profile-tool)


[ [BAR's Home] ](../README.md#scripts)
