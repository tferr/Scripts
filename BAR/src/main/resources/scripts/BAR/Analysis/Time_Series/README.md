# Time Series Scripts

[Analysis] scripts that deal with timelapse images. For scripts dealing with video-tracked
data have a look at the scripts installed in the Script Editor [templates] menu.


## Multi ROI Profiler
Groovy script that plots ROI intensities over time. Extends ROI Manager's _Multi Plot_
command to multichannel images and ROIs of any type.


## Normalize Against F0
Normalizes the time-course of a fluorescent signal against a resting state (_F0_),
as required in, e.g., Calcium imaging experiments. Available methods: `F/F0`, `(F-F0)/F0`,
`Delta-F`, and variants thereof.


## Register Against Average
Registers twice a single-channel time sequence against its average using
[turboreg](http://imagej.net/TurboReg): _Rigid body_ first, followed by _Affine_
transformation.

[templates]: https://github.com/tferr/Scripts/tree/master/BAR/src/main/resources/script_templates



------
| [Home] | [Analysis] | [Annotation] | [Data Analysis] | [lib] | [My Routines] | [Segmentation] | [Tools] | [Utilities] | [Wiki] |

[Home]: https://github.com/tferr/Scripts
[Analysis]: https://github.com/tferr/Scripts/tree/master/BAR/src/main/resources/scripts/BAR/Analysis
[Annotation]: https://github.com/tferr/Scripts/tree/master/BAR/src/main/resources/scripts/BAR/Annotation
[Data Analysis]: https://github.com/tferr/Scripts/tree/master/BAR/src/main/resources/scripts/BAR/Data_Analysis
[lib]: https://github.com/tferr/Scripts/tree/master/BAR/src/main/resources/lib
[My Routines]: https://github.com/tferr/Scripts/tree/master/BAR/src/main/resources/scripts/BAR/My_Routines
[Segmentation]: https://github.com/tferr/Scripts/tree/master/BAR/src/main/resources/scripts/BAR/Segmentation
[Tools]: https://github.com/tferr/Scripts/tree/master/BAR/src/main/resources/tools
[Utilities]: https://github.com/tferr/Scripts/tree/master/BAR/src/main/resources/scripts/BAR/Utilities
[Wiki]: https://imagej.net/BAR
