# Morphometry

[Routines](../README.md#scripts) related to the quantification of neuronal arbors


###[Strahler Analysis](./Strahler_Analysis.bsh)
   A BeanShell script that performs Strahler analysis on topographic skeletons (2D/3D)
   through progressive pruning of terminal branches.
   ([Documentation Page](http://fiji.sc/Strahler_Analysis))
   ([Download .bsh](./Strahler_Analysis.bsh?raw=true))


#### Requirements
   If you are running [Fiji](http://fiji.sc/) you already have all the required
   dependencies. If you are not running Fiji, the script will tell you which files to
   download, as long as you are running IJ 1.47m (or newer). The required plugins are:

   - [AnalyzeSkeleton](http://fiji.sc/AnalyzeSkeleton) ([Download.jar][AnalyzeSkeleton_ jar])
   - [Skeletonize 3D](http://fiji.sc/Skeletonize3D) (for 3D analysis) ([Download.jar][Skeletonize3D_ jar])

  If you are not using Fiji, make sure you are running the latest versions of the above
  plugins. A way to ensure it (without running Fiji), is to delete your local copies of
  _AnalyzeSkeleton_ and _Skeletonize3D_ .jars. The script will then provide you with the
  diret download links from [jenkins][jenkins plugins].


#### Installation:
   Save [Strahler_Analysis.bsh](./Strahler_Analysis.bsh?raw=true) in the plugins/ folder
   using the _Plugins>Install..._ command.


##See Also

* [Sholl Analysis](https://github.com/tferr/ASA#sholl-analysis)

[jenkins plugins]: http://jenkins.imagej.net/job/Stable-Fiji/ws/Fiji.app/plugins/
[AnalyzeSkeleton_ jar]: http://jenkins.imagej.net/job/Stable-Fiji/ws/Fiji.app/plugins/Skeletonize3D_-1.0.1-SNAPSHOT.jar
[Skeletonize3D_ jar]: http://jenkins.imagej.net/job/Stable-Fiji/ws/Fiji.app/plugins/AnalyzeSkeleton_-2.0.0-SNAPSHOT.jar

