/* Remove_Isolated_Pixels.ijm
 * IJ BAR: https://github.com/tferr/Scripts#scripts
 *
 * Calls Wipe_Background.ijm to eliminate isolated pixels in segmented images
 * TF, 2014.10
 */

run("Wipe Background", "size=1 circ.=0.00-1.00 scope=[Whole image] apply=[All slices]");