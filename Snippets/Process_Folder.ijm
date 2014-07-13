/* Process_Folder.ijm
 * IJ BAR snippet https://github.com/tferr/Scripts/tree/master/Snippets
 *
 * This macro[1] snippet implements a generic, reusable script to be used as a
 * more modular and flexible alternative to the Process>Batch>Macro built-in
 * command.
 *
 * It is composed of four self-contained functions:
 *   1. expectedImage(): Determines wich image types should be processed
 *   2. getOutputDirectory(): Sets a destination folder to save processed images
 *   3. myRoutines(): Container to hold the image processing routines
 *   4. processImage(): Applies myRoutines() to individual images
 *
 * [1] https://github.com/tferr/Scripts/tree/master/Snippets#imagej-macro-language
 */


inputDir = getDirectory("Select a source directory");
outputDir = getOutputDirectory(inputDir);

setBatchMode(true);
files = getFileList(inputDir);

for (i=0; i<files.length; i++) {

	if (expectedImage(files[i])) {
		print(i+1, "Analyzing "+ files[i] +"...");
		processImage(files[i], outputDir);
	} else {
		print(i+1, "Skipping "+ files[i] +"... not the right file type");
	}

}


function myRoutines() {

	// <Your code here>

}


/*
 * Retrieves the full path to a "Processed" folder placed at the same location
 * of <input_dir>. The function does nothing if such a directory already exists.
 * For safety, the macro is aborted if <input_dir> is not accessible or if the
 * directory cannot be created, e.g., due to lack of appropriate credentials.
 */
function getOutputDirectory(input_dir) {
	if (!File.isDirectory(input_dir)) {
		exit("Macro aborted: The directory\n'"+ input_dir +"'\nwas not found.");
	}
	if (endsWith(input_dir, File.separator)) {
		separatorPosition = lengthOf(input_dir);
		input_dir = substring(input_dir, 0, separatorPosition-1);
	}
	new_dir = input_dir + "_Processed" + File.separator;
	if (!File.isDirectory(new_dir)) {
		File.makeDirectory(new_dir);
		if (!File.isDirectory(new_dir))
			exit("Macro aborted:\n" + new_dir + "\ncould not be created.");
	}
	return new_dir;
}


/*
 * Returns true if the file extension of the argument <filename> is present in
 * the <extensions> array. Returns false otherwise. Note that certain image
 * types may be handled by Bio-Formats (or other plugins) and not by IJ directly.
 */
function expectedImage(filename) {
	extensions = newArray(".tif", ".stk", ".oib");
	expected = false;
	for (i=0; i<extensions.length; i++) {
		if (endsWith(toLowerCase(filename), extensions[i])) {
			expected = true;
			break;
		}
	}
	return expected;
}


/*
 * Applies <myRoutines()> to individual files. It takes 2 arguments: the full
 * path of the image to be processed <file_path> and the directory <save_dir>
 * where a copy of the processed image should be saved.
 */
function processImage(file_path, save_dir) {
	open(file_path);
	if (!endsWith(save_dir, File.separator))
		save_dir += File.separator;
	output_path = save_dir + "Treated_" + getTitle();
	myRoutines();
	saveAs("tiff", output_path);
	close();
}
