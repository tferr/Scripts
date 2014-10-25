# <a name="scripts"></a>IJ BAR

Welcome to the **IJ BAR**: A collection of <b>B</b>roadly <b>A</b>pplicable <b>R</b>outines for [ImageJ][ij1]/[Fiji][fiji], the de facto standard in scientific image processing in the life sciences.

The easiest way is to install these scripts is to use [Fiji][fiji] and subscribe to the [BAR update site](http://fiji.sc/BAR#Installation). Alternatively, you can navigate the collection using the sections below, and download individual routines as needed.
For more details refer to the [BAR documentation page][Fiji documentation].


## [Data Analysis][Analysis]
  Operations related to statistics, profiles, histograms and curve fitting.

  1. (ijm) [Distribution Plotter](./Data_Analysis/README.md#distribution-plotter)
  2. (bsh) [Find Peaks](./Data_Analysis/README.md#find-peaks)
  3. (bsh) [Fit Polynomial](./Data_Analysis/README.md#fit-polynomial)
  4. (bsh) [Plot Results](./Data_Analysis/README.md#plot-results)


## [Image Annotation][Annotation]
  Aiders for the annotation of scientific images.

  1. (ijm) [ROI Color Coder](./Annotation/README.md#roi-color-coder)


## [Image Segmentation][Segmentation]
  Routines for partitioning images into analyzable parts.

  1. (java) [Shen-Castan Edge Detector](./Segmentation/README.md#shen-castan-edge-detector)
  2. (ijm)[Apply Threshold To ROI](./Segmentation/README.md#apply-threshold-to-roi)
  3. (ijm) [Clear Thresholded Pixels](./Segmentation/README.md#clear-thresholded-pixels)
  4. (ijm) [Remove Isolated Pixels](./Segmentation/README.md#remove-isolated-pixels)
  5. (ijm) [Segment Profile Tool](./Tools/README.md#segment-profile-tool)
  6. (ijm) [Set Threshold From Background](./Segmentation/README.md#set-threshold-from-background)
  7. (ijm) [Wipe Background](./Segmentation/README.md#wipe-background)


## [Neuronal Morphometry][Morphometry]
  Scripts related to the quantification of neuronal arbors and other tree-like structures.

  1. (bsh) [Strahler Analysis](./Morphometry/README.md#strahler-analysis)


## [Tools and Toolsets][Tools]
  Additions to the ImageJ toolbar.

  1. (ijm) [Calibration Menu](./Tools/README.md#calibration-menu)
  2. (ijm) [ROI Manager Tools](./Tools/README.md#roi-manager-tools)
  3. (ijm) [Segment Profile Tool](./Tools/README.md#segment-profile-tool)
  4. (ijm) [Shortcuts Menu](./Tools/README.md#shortcuts-menu)
  5. (ijm) [Toolset Creator](./Tools/README.md#toolset-creator)


## [BAR plugins][Plugins]
  Maven project implementing the Java [plugins](./BAR/README.md#bar-plugins) bundled with BAR. The files organizing the [BAR menu](./BAR/README.md#bar-menu) are also described in this section.

## [Snippets][Snippets]
  A depository of generic and reusable scripts to be used as scripting templates, including:

  1. (py) [Median Filter](./Snippets/README.md#median-filter)
  2. (ijm) [Process Folder](./Morphometry/README.md#process-folder)


## Help?
 * Want to Contribute to BAR?
    * Thanks! Please, please do! See [here](https://guides.github.com/activities/contributing-to-open-source/) and [here](https://help.github.com/articles/fork-a-repo) for details on how to [fork](https://github.com/tferr/Scripts/fork) BAR or [here](https://help.github.com/articles/using-pull-requests) on how to initiate a [pull request](https://github.com/tferr/Scripts/pulls)
    * Documentation updates are also welcome, so go ahead and improve the [BAR documentation page][Fiji documentation]
 * Having problems? Found a bug? Need to ask a question?
    * See the BAR [FAQs](http://fiji.sc/BAR#FAQ), Fiji [FAQs](http://fiji.sc/Frequently_Asked_Questions) and [Bug reporting best practices](http://fiji.sc/Bug_reporting_best_practices). Then, you can either:
      * [Open an issue](https://github.com/tferr/Scripts/issues) on this repository
      * Report it on the [ImageJ mailing list](http://imagej.nih.gov/ij/list.html)


## Citations
BAR scripts have contributed to the following publications:

  - Medda et al. Investigation of early cell-surface interactions of human mesenchymal stem cells on nanopatterned β-type titanium-niobium alloy surfaces. Interface Focus (2014) vol. 4 (1) pp. 20130046 [PMID 24501674](http://www.ncbi.nlm.nih.gov/pubmed/24501674)
  - Dobens and Dobens. FijiWings: an open source toolkit for semiautomated morphometric analysis of insect wings. G3 (Bethesda) (2013) vol. 3 (8) pp. 1443-9 [PMID 23797110](http://www.ncbi.nlm.nih.gov/pubmed/23797110)
  - van der Meer et al. Three-dimensional co-cultures of human endothelial cells and embryonic stem cell-derived pericytes inside a microfluidic device. Lab Chip (2013) vol. 13 (18) pp. 3562-8 [PMID 23702711](http://www.ncbi.nlm.nih.gov/pubmed/23702711)
  - Soulet et al. Automated filtering of intrinsic movement artifacts during two-photon intravital microscopy. PLoS ONE (2013) vol. 8 (1) pp. e53942 [PMID 23326545](http://www.ncbi.nlm.nih.gov/pubmed/23326545)
  - Carnevalli et al. S6K1 plays a critical role in early adipocyte differentiation. Developmental Cell (2010) vol. 18 (5) pp. 763-74 [PMID 20493810](http://www.ncbi.nlm.nih.gov/pubmed/20493810)


## References
  - Shen and Castan. An optimal linear operator for step edge detection. CVGIP: Graphical Models and Image Processing (1992) vol. 54 (2) pp. 112-133 [DOI: 10.1016/1049-9652(92)90060-B](http://dx.doi.org/10.1016/1049-9652(92)90060-B)


License
-------
These scripts are free software: you can redistribute them and/or modify them under the terms of the [GNU General Public License](http://www.gnu.org/licenses/gpl.txt) as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.


Contributors
------------
BAR was created and is maintained by [Tiago Ferreira](mailto:tiagoalvespedrosa_at_gmail_dot_com) with contributions from Maxime Pinchon, [Johannes Schindelin](https://github.com/dscho), [Wayne Rasband][ij1] and [Kota Miura](https://github.com/cmci). This project would not have been possible without the support of the outstanding [ImageJ community](http://imagej.net/Mailing_Lists).


[ij1]: http://imagej.nih.gov/ij/
[fiji]: http://fiji.sc/





| [Analysis] | [Annotation] | [Segmentation] | [Morphometry] | [Tools] | [Plugins] | [Snippets] | [Fiji][Fiji documentation] |
|:----------:|:------------:|:--------------:|:-------------:|:-------:|:---------:|:----------:|:--------------------------:|


[Analysis]: https://github.com/tferr/Scripts/tree/master/Data_Analysis#analysis
[Annotation]: https://github.com/tferr/Scripts/tree/master/Annotation#annotation
[Segmentation]: https://github.com/tferr/Scripts/tree/master/Segmentation#segmentation
[Morphometry]: https://github.com/tferr/Scripts/tree/master/Morphometry#morphometry
[Tools]: https://github.com/tferr/Scripts/tree/master/Tools#tools-and-toolsets
[Plugins]: https://github.com/tferr/Scripts/tree/master/BAR#bar-plugins
[Snippets]: https://github.com/tferr/Scripts/tree/master/Snippets#snippets
[Fiji documentation]: http://fiji.sc/BAR

