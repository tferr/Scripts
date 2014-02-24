# Analysis

[Routines](../README.md#scripts) related to data analysis.

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
   options such as:

  1. Peak amplitude
  2. Peak height
  3. Peak width

   ([Download .bsh](./Find_Peaks.bsh?raw=true))
   ([Documentation page][FP page])


[ [Home] ](../README.md#scripts)

[DP page]: http://imagejdocu.tudor.lu/doku.php?id=macro:distribution_plotter
[DP image]: http://imagejdocu.tudor.lu/lib/exe/fetch.php?cache=&media=macro:distributionplotterdemo.png
[FP page]: http://fiji.sc/Find_Peaks

