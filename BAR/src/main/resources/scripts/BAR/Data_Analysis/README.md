# Data Analysis

[BARs][Home] related to analysis of non-image numerical data.

###[Create Boxplot](./Create_Boxplot.bsh)
Displays a [box-and-whisker](https://en.wikipedia.org/wiki/Box_plot) plot from data in an ImageJ
table using the [JFreeChart](http://www.jfree.org/jfreechart/) library, bundled with Fiji.
Data can be split into groups and plot can be exported as vector graphics. It exemplifies how to
use the BAR [API](http://tferr.github.io/Scripts/apidocs/) (namely
[PlotUtils](../../../../java/bar/PlotUtils.java)) to script JFreeChart.


![boxplot](../../../../../../../images/box-plot-demo.png)

See also [Interactive Plotting](#interactive-plotting),
 [JFreeChart API](http://javadoc.imagej.net/JFreeChart/)

###[Create Polar Plot](./Create_Polar_Plot.bsh)
Generates a polar plot from data in an ImageJ table using the [JFreeChart](http://www.jfree.org/jfreechart/)
library, bundled with Fiji. Plot can be customized and exported as vector graphics. It
exemplifies how to use the BAR [API](http://tferr.github.io/Scripts/apidocs/) (namely
[PlotUtils](../BAR/src/main/java/bar/PlotUtils.java)) to script JFreeChart.

![polar plot](../../../../../../../images/polar-plot-demo.png)

See also [Interactive Plotting](#interactive-plotting),
 [JFreeChart API](http://javadoc.imagej.net/JFreeChart/)

###[Distribution Plotter](./Distribution_Plotter.ijm)
Plots relative and cumulative frequencies of a measured parameter. Detailed functionality:
1) Retrieves relative and cumulative frequencies; 2) Fits a Normal distribution to histogram
of relative frequencies; 3) Offers several methods to determine the optimal number of
histogram bins: Square root (used by e.g., M. Excel), Sturges', Scott's (used by
_Analyze>Distribution..._) and  Freedman–Diaconis'.
([Documentation page][DP page])

![distribution plotter](../../../../../../../images/distribution-plotter-demo.png)


###[Find Peaks](./Find_Peaks.bsh)
   Retrieves local maxima and minima from an ImageJ plot, allowing several filtering
   options such as: 1) Peak amplitude; 2) Peak height and 3) Peak width.
   ([Documentation page][FP page])

   ![find peaks](../../../../../../../images/find-peaks-demo.png)


###[Fit Polynomial](./Fit_Polynomial.bsh)
   Fits a polynomial function (of arbitrary degree) to sampled data from an ImageJ plot.
   Features an heuristic algorithm for guessing a polynomial 'best fit'.

   Requires the apache commons math library, distributed with Fiji. Non-Fiji users that do
   not have it installed are provided with a direct download link that will install all
   required dependencies.

   ![polynomial fitter](../../../../../../../images/animated-poly-fit.gif)


###[Interactive Plotting](../../../../java/bar/plugin/InteractivePlotter.java)
_The_ interactive plotting GUI for ImageJ. Interactively creates a multi-series XY plot (with or
without error bars), from ImageJ measurements, plugin tables or imported spreadsheet data.
Multi-series vector field plots are also supported.

![plot builder](../../../../../../../images/plotbuilder-demo.png)


See also [Create Boxplot](#create-boxplot), [Create Polar Plot](#create-polar-plot)


##See Also

* [Analysis], BARs that complement built-in commands in the ImageJ `Analyze>` menu.




| [Home] | [Analysis] | [Data Analysis] | [Annotation] | [Segmentation] | [Tools] | [Plugins] | [lib] | [Snippets] | [IJ] |
|:------:|:----------:|:---------------:|:------------:|:--------------:|:-------:|:---------:|:-----:|:----------:|:----:|

[Home]: https://github.com/tferr/Scripts#ij-bar
[Analysis]: https://github.com/tferr/Scripts/tree/master/Analysis#analysis
[Data Analysis]: https://github.com/tferr/Scripts/tree/master/BAR/src/main/resources/scripts/BAR/Data_Analysis#data-analysis
[Annotation]: https://github.com/tferr/Scripts/tree/master/Annotation#annotation
[Segmentation]: https://github.com/tferr/Scripts/tree/master/Segmentation#segmentation
[Tools]: https://github.com/tferr/Scripts/tree/master/Tools#tools-and-toolsets
[Plugins]: https://github.com/tferr/Scripts/tree/master/BAR#bar-plugins
[lib]: https://github.com/tferr/Scripts/tree/master/lib#lib
[Snippets]: https://github.com/tferr/Scripts/tree/master/Snippets#snippets
[IJ]: http://imagej.net/BAR
