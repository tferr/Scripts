# Morphometry

[Routines](../README.md#scripts) related to the quantification of neuronal arbors


###[Strahler Analysis](./Strahler_Analysis.bsh)
   A BeanShell script that performs Strahler analysis on topographic skeletons (2D/3D)
   through progressive pruning of terminal branches.
   ([Documentation Page](http://fiji.sc/Strahler_Analysis))
   ([Download .bsh](./Strahler_Analysis.bsh?raw=true))


#### Requirements
   If you are running [Fiji](http://fiji.sc/) you already have all the required
   dependencies. If you are not using Fiji, make sure you are running the latest versions of
   [AnalyzeSkeleton](http://fiji.sc/AnalyzeSkeleton) and [Skeletonize 3D](http://fiji.sc/Skeletonize3D)
   (for 3D analysis).
   Without running Fiji, a way to ensure this, is to delete your local copies of _AnalyzeSkeleton_
   and _Skeletonize3D_ .jar files. The script will then provide you with the diret download links
   from [jenkins][jenkins plugins].


#### Installation:
   Save [Strahler_Analysis.bsh](./Strahler_Analysis.bsh?raw=true) in the plugins/ folder
   using the _Plugins>Install..._ command.


##See Also

* [Sholl Analysis](https://github.com/tferr/ASA#sholl-analysis)


[ [Home] ](../README.md#scripts)

[jenkins plugins]: http://jenkins.imagej.net/job/Stable-Fiji/ws/Fiji.app/plugins/
