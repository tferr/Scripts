# Analysis

[BARs][Home] that complement built-in commands in the ImageJ `Analyze>` menu.

###[Multichannel Plot Profile](./Multichannel_Plot_Profile.bsh)
   Extends the _Analyze> Plot Profile_ to multichannel (composite) images. It features a _Live
   mode_, guesses displayed lookup tables and ignores disabled channels, i.e., those deselected
   in the "Channels" widget (which can be called by pressing `Z`, the shortcut for _Image> Color>
   Channels Tool_).
   ([Download .bsh](./Multichannel_Plot_Profile.bsh?raw=true))

   Tips:

   * _Live mode_ will stop as soon as a valid ROI is no longer present. It will resume as soon as a
     new one is reinstated.
   * By default, rectangular areas and lines wider than 1 pixel are plotted using column averages.
     To use row averages instead hold down `Alt`. This works in _Live mode_ and mimmics the behavior
     of the  built-in _Analyze> Plot Profile_ command. To use column averages at all times, activate
     the _Vertical Profile_ checkbox in _Edit>Options>Profile Plot Options..._
   * Y-axis upper limit is set by the active channel. If a profile from a brighter channel appears
     truncated, turn on _Live mode_, and activate the brighter channel using the _C slider_ of the
     image.
   * If one of the channels uses and unconvential LUT the script will plot its profile using black.
     To change the color of the plotted curves, turn on _Live mode_, activate the image, and apply a
     new LUT using the drop-down menu of the "Channels" widget.
   * By default, the X-axis will use spatially calibrated distances. To use pixel distances instead
     set the flag [`pixelUnits`](./Multichannel_Plot_Profile.bsh#L27-L29) to `true`.


   See also: [Data Analysis]



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
