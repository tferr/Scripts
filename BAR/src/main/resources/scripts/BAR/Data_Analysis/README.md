# Data Analysis

[BARs][Home] related to analysis of non-image numerical data.

###[Create Boxplot](./Create_Boxplot.bsh)
Displays a [box-and-whisker](https://en.wikipedia.org/wiki/Box_plot) plot from data in an ImageJ
table using the [JFreeChart](http://www.jfree.org/jfreechart/) library, bundled with Fiji.
Data can be split into groups and plot can be exported as vector graphics. It exemplifies how to
use the BAR [API](http://tferr.github.io/Scripts/apidocs/) (namely
[PlotUtils](../BAR/src/main/java/bar/PlotUtils.java)) to script JFreeChart.

![boxplot](../images/box-plot-demo.png)

See also [Clipboard to Results](#clipboard-to-results), [Plot XY Data](#plot-xy-data),
 [JFreeChart API](http://javadoc.imagej.net/JFreeChart/)

###[Create Polar Plot](./Create_Polar_Plot.bsh)
Generates a polar plot from data in an ImageJ table using the [JFreeChart](http://www.jfree.org/jfreechart/)
library, bundled with Fiji. Plot can be customized and exported as vector graphics. It
exemplifies how to use the BAR [API](http://tferr.github.io/Scripts/apidocs/) (namely
[PlotUtils](../BAR/src/main/java/bar/PlotUtils.java)) to script JFreeChart.

![polar plot](../images/polar-plot-demo.png)

See also [Clipboard to Results](#clipboard-to-results), [Plot XY Data](#plot-xy-data),
 [JFreeChart API](http://javadoc.imagej.net/JFreeChart/)

###[Distribution Plotter](./Distribution_Plotter.ijm)
Plots relative and cumulative frequencies of a measured parameter. Detailed functionality:
1) Retrieves relative and cumulative frequencies; 2) Fits a Normal distribution to histogram
of relative frequencies; 3) Offers several methods to determine the optimal number of
histogram bins: Square root (used by e.g., M. Excel), Sturges', Scott's (used by
_Analyze>Distribution..._) and  Freedman–Diaconis'.
([Documentation page][DP page])

[![][DP image]][DP page]


###[Find Peaks](./Find_Peaks.bsh)
   Retrieves local maxima and minima from an ImageJ plot, allowing several filtering
   options such as: 1) Peak amplitude; 2) Peak height and 3) Peak width.
   ([Documentation page][FP page])

   [![][FP image]][FP page]


###[Fit Polynomial](./Fit_Polynomial.bsh)
   Fits a polynomial function (of arbitrary degree) to sampled data from an ImageJ plot.
   Features an heuristic algorithm for guessing a polynomial 'best fit'.

   Requires the apache commons math library, distributed with Fiji. Non-Fiji users that do
   not have it installed are provided with a direct download link that will install all
   required dependencies.

   [![][Poly image]](http://imagej.net/Sholl_Analysis#Complementary_Tools)


###[Plot XY Data](./Plot_XY_Data.bsh)
_The_ interactive plotting GUI for ImageJ. Interactively creates a multi-series XY plot (with or
without error bars), from ImageJ measurements, plugin tables or imported spreadsheet data.
Multi-series vector field plots are also supported.

![plot builder](../images/plotbuilder-demo.png)


See also [Create Boxplot](#create-boxplot), [Create Polar Plot](#create-polar-plot)


##See Also

* [Analysis], BARs that complement built-in commands in the ImageJ `Analyze>` menu.

[DP page]: http://imagejdocu.tudor.lu/doku.php?id=macro:distribution_plotter
[DP image]: http://imagejdocu.tudor.lu/lib/exe/fetch.php?cache=&media=macro:distributionplotterdemo.png
[FP page]: http://imagej.net/Find_Peaks
[FP image]: http://imagej.net/_images/a/a1/FindPeaksSnapshot.png
[Poly image]: http://imagej.net/_images/f/f0/AnimatedPolyFit.gif



| [Home] | [Analysis] | [Data Analysis] | [Annotation] | [Segmentation] | [Tools] | [Plugins] | [lib] | [Snippets] | [IJ] |
|:------:|:----------:|:---------------:|:------------:|:--------------:|:-------:|:---------:|:-----:|:----------:|:----:|

[Home]: https://github.com/tferr/Scripts#ij-bar
[Analysis]: https://github.com/tferr/Scripts/tree/master/Analysis#analysis
[Data Analysis]: https://github.com/tferr/Scripts/tree/master/Data_Analysis#data-analysis
[Annotation]: https://github.com/tferr/Scripts/tree/master/Annotation#annotation
[Segmentation]: https://github.com/tferr/Scripts/tree/master/Segmentation#segmentation
[Tools]: https://github.com/tferr/Scripts/tree/master/Tools#tools-and-toolsets
[Plugins]: https://github.com/tferr/Scripts/tree/master/BAR#bar-plugins
[lib]: https://github.com/tferr/Scripts/tree/master/lib#lib
[Snippets]: https://github.com/tferr/Scripts/tree/master/Snippets#snippets
[IJ]: http://imagej.net/BAR
