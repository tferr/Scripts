# IJ BAR

[![DOI](https://zenodo.org/badge/8709403.svg)](https://zenodo.org/badge/latestdoi/8709403)
[![Latest Release](https://img.shields.io/github/release/tferr/Scripts.svg?style=flat-square)](https://github.com/tferr/Scripts/releases)
[![Issues](https://img.shields.io/github/issues/tferr/Scripts.svg?style=flat-square)](https://github.com/tferr/Scripts/issues)
[![Jenkins](http://img.shields.io/jenkins/s/http/jenkins.imagej.net/BAR.svg?style=flat-square)](http://jenkins.imagej.net/job/BAR)
[![GPL License](http://img.shields.io/badge/license-GPL-blue.svg?style=flat-square)](http://opensource.org/licenses/GPL-3.0)

Welcome to the **IJ BAR**: A collection of <b>B</b>roadly <b>A</b>pplicable <b>R</b>outines
for [ImageJ](http://imagej.net/), the de facto standard in scientific image processing in
the life sciences.

To install BAR you just need to subscribe to the
[BAR update site](http://imagej.net/BAR#Installation). To know more about BAR, have a look
at its [Wiki Page][Wiki]. Below is a lis of some of the BAR routines:


## [Analysis]
Routines that complement built-in commands in the ImageJ `Analyze>` menu.

1. (py) [LoG-DoG Spot Counter](./BAR/src/main/resources/scripts/BAR/Analysis#log-dog-spot-counter)
1. (bsh) [Multichannel Plot Profile](./BAR/src/main/resources/scripts/BAR/Analysis#multichannel-plot-profile)
1. (bsh) [Multichannel ZT-axis Profile](./BAR/src/main/resources/scripts/BAR/Analysis#multichannel-zt-axis-profile)
1. (bsh) [Smoothed Plot Profile](./BAR/src/main/resources/scripts/BAR/Analysis#smoothed-plot-profile)
1. (groovy) [Multi ROI Profiler](./BAR/src/main/resources/scripts/BAR/Analysis/Time_Series#multi-roi-profiler)
1. (groovy) [Normalize Against F0](./BAR/src/main/resources/scripts/BAR/Analysis/Time_Series#normalize-against-f0)
1. (groovy) [Register Against Average](./BAR/src/main/resources/scripts/BAR/Analysis/Time_Series#register-against-average)


## [Data Analysis]
Operations related to statistics, profiles, histograms and curve fitting.

1. (bsh) [Create Boxplot](./BAR/src/main/resources/scripts/BAR/Data_Analysis#create-boxplot)
1. (bsh) [Create Polar Plot](./BAR/src/main/resources/scripts/BAR/Data_Analysis#create-polar-plot)
1. (ijm) [Distribution Plotter](./BAR/src/main/resources/scripts/BAR/Data_Analysis#distribution-plotter)
1. (bsh) [Find Peaks](./BAR/src/main/resources/scripts/BAR/Data_Analysis#find-peaks)
1. (bsh) [Fit Polynomial](./BAR/src/main/resources/scripts/BAR/Data_Analysis#fit-polynomial)
1. (java) [Interactive Plotting](./BAR/src/main/resources/scripts/BAR/Data_Analysis#interactive-plotting)
1. (py) [NN Distances](./BAR/src/main/resources/scripts/BAR/Data_Analysis#nn-distances)


## [Image Annotation][Annotation]
Aiders for the annotation of scientific images.

1. (ijm) [Combine Orthogonal Views](./BAR/src/main/resources/scripts/BAR/Annotation#combine-orthogonal-views)
1. (bsh) [Cumulative Z-Project](./BAR/src/main/resources/scripts/BAR/Annotation#cumulative-z-project)
1. (ijm) [ROI Color Coder](./BAR/src/main/resources/scripts/BAR/Annotation#roi-color-coder)


## [Image Segmentation][Segmentation]
Routines for partitioning images into analyzable parts.

1. (ijm) [Apply Threshold To ROI](./BAR/src/main/resources/scripts/BAR/Segmentation#apply-threshold-to-roi)
1. (ijm) [Clear Thresholded Pixels](./BAR/src/main/resources/scripts/BAR/Segmentation#clear-thresholded-pixels)
1. (bsh) [Remove Isolated Pixels](./BAR/src/main/resources/scripts/BAR/Segmentation#remove-isolated-pixels)
1. (ijm) [Segment Profile Tool](./BAR/src/main/resources/scripts/BAR/Segmentation#segment-profile-tool)
1. (java) [Shen-Castan Edge Detector](./BAR/src/main/resources/scripts/BAR/Segmentation#shen-castan-edge-detector)
1. (ijm) [Threshold From Background](./BAR/src/main/resources/scripts/BAR/Segmentation#threshold-from-background)
1. (ijm) [Wipe Background](./BAR/src/main/resources/scripts/BAR/Segmentation#wipe-background)


## [Utilities]
Productivity tools.

1. (java) [Commander](./BAR/src/main/resources/scripts/BAR/Utilities#commander)
1. (ijm) [Calibration Menu](./BAR/src/main/resources/scripts/BAR/Utilities#productivity-menus)
1. (ijm) [List Folder Menu](./BAR/src/main/resources/scripts/BAR/Utilities#productivity-menus)
1. (java) [New Snippet](./BAR/src/main/resources/scripts/BAR/Utilities#new-snippet)
1. (ijm) [Shortcuts Menu](./BAR/src/main/resources/scripts/BAR/Utilities#productivity-menus)
1. (ijm) [ROI Manager Tools](./BAR/src/main/resources/scripts/BAR/Utilities#roi-manager-tools)
1. (ijm) [Toolset Creator](./BAR/src/main/resources/scripts/BAR/Utilities#create-toolset)


## [My Routines]
An infrastructure to help users tinkering with ImageJ.

1. [Multi-language libs](./BAR/src/main/resources/scripts/BAR/lib#lib):
   User-defined libraries (BeanShell, Clojure, Groovy, IJ Macro, JavaScript, Python, Ruby)
1. [Boilerplate Scripts](./BAR/src/main/resources/boilerplate/), multi-language skeletons
   for new scripts
1. [Script Templates](./BAR/src/main/resources/script_templates), multi-language snippets


## [Java Classes]
Maven project implementing the backbone of BAR, including several [IJ1 plugins and IJ2
commands](./BAR#commands-and-plugins), [External Ops](./BAR#external-ops),
and the [BAR API](http://tferr.github.io/Scripts/apidocs/).


## [Tutorials]
1. [Introduction to Scripting](./BAR/src/main/resources/tutorials#tutorials):
   101 of (IJ1) scripting using BeanShell and Python (Jython)

1. [External Ops](./BAR#external-ops): Advanced tutorial exemplifying how to
   provide external [ops](http://imagej.net/ImageJ_Ops)


# Help?
 * Want to Contribute to BAR?
    * Thanks! Please, please do! See [here](https://guides.github.com/activities/contributing-to-open-source/)
    and [here](https://help.github.com/articles/fork-a-repo) for details on how to
    [fork](https://github.com/tferr/Scripts/fork) BAR or
    [here](https://help.github.com/articles/using-pull-requests) on how to initiate a
    [pull request](https://github.com/tferr/Scripts/pulls)
    * Documentation updates are also welcome, so go ahead and improve the [BAR documentation page][IJ]
 * Having problems? Found a bug? Need to ask a question?
    * See the BAR [FAQs](http://imagej.net/BAR#FAQ), Fiji [FAQs](http://imagej.net/Frequently_Asked_Questions)
    and [Bug reporting best practices](http://imagej.net/Bug_reporting_best_practices). Then, you can either:
      * [Open an issue](https://github.com/tferr/Scripts/issues) on this repository
      * Report it on the [ImageJ mailing list](http://imagej.nih.gov/ij/list.html)


# Citations

* To cite BAR:

[![DOI](https://zenodo.org/badge/8709403.svg)](https://zenodo.org/badge/latestdoi/8709403)

* BAR scripts have contributed to the following publications:

  1. Ferreira et al. Neuronal morphometry directly from bitmap images. Nature Methods (2014), 11(10):982–984. [PMID 25264773](http://www.ncbi.nlm.nih.gov/pubmed/25264773)
  1. Pope and Voigt. Peripheral glia have a pivotal role in the initial response to axon degeneration of peripheral sensory neurons in zebrafish. PLoS ONE (2014), 9(7):e103283. [PMID 25058656](http://www.ncbi.nlm.nih.gov/pubmed/25058656)
  1. Medda et al. Investigation of early cell-surface interactions of human mesenchymal stem cells on nanopatterned β-type titanium-niobium alloy surfaces. Interface Focus (2014), 4(1):20130046. [PMID 24501674](http://www.ncbi.nlm.nih.gov/pubmed/24501674)
  1. Ferreira et al. Dendrite architecture is organized by transcriptional control of F-actin nucleation. Development (2014), 141(3):650–60. [PMID 24449841](http://www.ncbi.nlm.nih.gov/pubmed/24449841)
  1. Dobens and Dobens. FijiWings: an open source toolkit for semiautomated morphometric analysis of insect wings. G3 (Bethesda) (2013), 3(8):1443-9. [PMID 23797110](http://www.ncbi.nlm.nih.gov/pubmed/23797110)
  1. van der Meer et al. Three-dimensional co-cultures of human endothelial cells and embryonic stem cell-derived pericytes inside a microfluidic device. Lab Chip (2013), 13(18):3562-8. [PMID 23702711](http://www.ncbi.nlm.nih.gov/pubmed/23702711)
  1. Soulet et al. Automated filtering of intrinsic movement artifacts during two-photon intravital microscopy. PLoS ONE (2013), 8(1):e53942. [PMID 23326545](http://www.ncbi.nlm.nih.gov/pubmed/23326545)
  1. Paolicelli et al. Synaptic pruning by microglia is necessary for normal brain development. Science (2011), 9;333(6048):1456-8. [PMID 21778362](http://www.ncbi.nlm.nih.gov/pubmed/21778362)
  1. Carnevalli et al. S6K1 plays a critical role in early adipocyte differentiation. Developmental Cell (2010), 18(5):763-74. [PMID 20493810](http://www.ncbi.nlm.nih.gov/pubmed/20493810)


# License
This program is free software: you can redistribute them and/or modify them under the terms of the
[GNU General Public License](http://www.gnu.org/licenses/gpl.txt) as published by the Free Software
Foundation, either version 3 of the License, or (at your option) any later version.


# Contributors
BAR was created and is maintained by [Tiago Ferreira](http://imagej.net/User:Tiago)
with contributions from Maxime Pinchon, [Johannes Schindelin](https://github.com/dscho),
[Wayne Rasband](http://imagej.nih.gov/ij/), [Mark Hiner](https://github.com/hinerm),
[Jerome Mutterer](https://github.com/mutterer), [Kota Miura](https://github.com/cmci),
Nicolas Vanderesse, Peter J. Lee, [Jan Eglinger](https://github.com/imagejan) and
[others](https://github.com/tferr/Scripts/graphs/contributors).
BAR uses public domain [code](./BAR/src/main/java/bar/FileDrop.java) from Robert Harder
and Nathan Blomquist. This project would not have been possible without the support of the
outstanding [ImageJ community](http://imagej.net/Mailing_Lists).


[Java Classes]: https://github.com/tferr/Scripts/tree/master/BAR
[Tutorials]: https://github.com/tferr/Scripts/tree/master/BAR/src/main/resources/tutorials



------
| [Home] | [Analysis] | [Annotation] | [Data Analysis] | [lib] | [My Routines] | [Segmentation] | [Tools] | [Utilities] | [Wiki] |

[Home]: https://github.com/tferr/Scripts
[Analysis]: https://github.com/tferr/Scripts/tree/master/BAR/src/main/resources/scripts/BAR/Analysis
[Annotation]: https://github.com/tferr/Scripts/tree/master/BAR/src/main/resources/scripts/BAR/Annotation
[Data Analysis]: https://github.com/tferr/Scripts/tree/master/BAR/src/main/resources/scripts/BAR/Data_Analysis
[lib]: https://github.com/tferr/Scripts/tree/master//BAR/src/main/resources/scripts/BAR/lib
[My Routines]: https://github.com/tferr/Scripts/tree/master/BAR/src/main/resources/scripts/BAR/My_Routines
[Segmentation]: https://github.com/tferr/Scripts/tree/master/BAR/src/main/resources/scripts/BAR/Segmentation
[Tools]: https://github.com/tferr/Scripts/tree/master//BAR/src/main/resources/scripts/BAR/tools
[Utilities]: https://github.com/tferr/Scripts/tree/master//BAR/src/main/resources/scripts/BAR/Utilities
[Wiki]: https://imagej.net/BAR
