# Analysis

[BARs][Home] related to data analysis.

###[Clipboard to Results](./Clipboard_to_Results.py)
   Imports numeric values copied to the clipboard into the _Results_ table. Useful, since BARs that
   analyze tabular data can only read values from the main IJ _Results_ table.
   ([Download .py](./Clipboard_to_Results.py?raw=true))

   See also [Plot Results](#plot-results)


###[Distribution Plotter](./Distribution_Plotter.ijm)
   Plots relative and cumulative frequencies on a double Y-axis graph of a measured parameter.

  1. Retrives relative and cumulative frequencies
  2. Fits a Normal distribution to histogram of relative frequencies
  3. Offers several methods to determine the optimal number of histogram bins: Square root
   (used by e.g., M. Excel), Sturges', Scott's (used by _Analyze>Distribution..._) and
   Freedman–Diaconis'

   ([Download .ijm](./Distribution_Plotter.ijm?raw=true))
   ([Documentation page][DP page])

   [![][DP image]][DP page]


###[Find Peaks](./Find_Peaks.bsh)
   Retrieves local maxima and minima from an ImageJ plot, allowing several filtering
   options such as: 1) Peak amplitude; 2) Peak height and 3) Peak width.

   ([Download .bsh](./Find_Peaks.bsh?raw=true))
   ([Documentation page][FP page])

   [![][FP image]][FP page]


###[Fit Polynomial](./Fit_Polynomial.bsh)
   Fits a polynomial function (of arbitrary degree) to sampled data from an ImageJ plot.
   Features an heuristic algorithm for guessing a polynomial 'best fit'.

   Requires the apache commons math library, distributed with Fiji. Non-Fiji users that do
   not have it installed are provided with a direct download link that will install all
   required dependencies.
   ([Download .bsh](./Fit_Polynomial.bsh?raw=true))

   [![][Poly image]](http://fiji.sc/Sholl_Analysis#Complementary_Tools)


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
     set the flag [`pixelUnits`](./Multichannel_Plot_Profile.bsh#L27) to `true`.


###[Plot Results](./Plot_Results.bsh)
   Routine that creates an XY plot from data in the Results table. Useful for plotting
   data from imported spreadsheets.
   ([Download .bsh](./Plot_Results.bsh?raw=true))

   See also [Clipboard to Results](#clipboard-to-results)

[DP page]: http://imagejdocu.tudor.lu/doku.php?id=macro:distribution_plotter
[DP image]: http://imagejdocu.tudor.lu/lib/exe/fetch.php?cache=&media=macro:distributionplotterdemo.png
[FP page]: http://fiji.sc/Find_Peaks
[FP image]: http://fiji.sc/images/a/a1/FindPeaksSnapshot.png
[Poly image]: http://fiji.sc/images/f/f0/AnimatedPolyFit.gif




| [Home] | [Annotation] | [Segmentation] | [Morphometry] | [Tools] | [Plugins] | [Snippets] | [Fiji documentation] |
|:------:|:------------:|:--------------:|:-------------:|:-------:|:---------:|:----------:|:--------------------:|

[Home]: https://github.com/tferr/Scripts#ij-bar
[Analysis]: https://github.com/tferr/Scripts/tree/master/Data_Analysis#analysis
[Annotation]: https://github.com/tferr/Scripts/tree/master/Annotation#annotation
[Segmentation]: https://github.com/tferr/Scripts/tree/master/Segmentation#segmentation
[Morphometry]: https://github.com/tferr/Scripts/tree/master/Morphometry#morphometry
[Tools]: https://github.com/tferr/Scripts/tree/master/Tools#tools-and-toolsets
[Plugins]: https://github.com/tferr/Scripts/tree/master/BAR#bar-plugins
[Snippets]: https://github.com/tferr/Scripts/tree/master/Snippets#snippets
[Fiji documentation]: http://fiji.sc/BAR

