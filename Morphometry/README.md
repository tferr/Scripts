# Morphometry

[BARs][Home] related to the quantification of neuronal arbors


###[Strahler Analysis](./Strahler_Analysis.bsh)
   A BeanShell script that performs Strahler analysis on topographic skeletons (2D/3D)
   through progressive pruning of terminal branches.
   ([Documentation Page][SA page])
   ([Download .bsh](./Strahler_Analysis.bsh?raw=true))

   [![][SA image]][SA page]

#### Requirements
   If you are running [Fiji](http://fiji.sc/) you already have all the required
   dependencies. If you are not using Fiji, make sure you are running the latest versions of
   [AnalyzeSkeleton](http://fiji.sc/AnalyzeSkeleton) and [Skeletonize 3D](http://fiji.sc/Skeletonize3D)
   (for 3D analysis).
   Without running Fiji, a way to ensure this, is to delete your local copies of _AnalyzeSkeleton_
   and _Skeletonize3D_ .jar files. The script will then provide you with the diret download links
   from [jenkins.imagej.net][jenkins plugins].


#### Installation:
   Subscribe to the BAR [update site](http://fiji.sc/BAR#Installation) in Fiji. Alternatively, save [Strahler_Analysis.bsh](./Strahler_Analysis.bsh?raw=true) in the plugins/ folder
   using the _Plugins>Install..._ command.


##See Also

* Sholl Analysis: [Repository](https://github.com/tferr/ASA#sholl-analysis) /
  [Home page](http://fiji.sc/Sholl_Analysis)


[SA page]: http://fiji.sc/Strahler_Analysis
[SA image]: http://fiji.sc/images/9/97/Strahler_RootProtection.png
[jenkins plugins]: http://jenkins.imagej.net/job/Stable-Fiji/ws/Fiji.app/plugins/




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
