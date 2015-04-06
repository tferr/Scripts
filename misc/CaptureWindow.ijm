/*
 * CaptureWindow.ijm
 * IJ BAR: https://github.com/tferr/Scripts#scripts
 *
 * Mac-specific macro that calls Grab.app to capture an ImageJ window
 */

// Capture window interactively
exec("screencapture -ciWo > /dev/null 2>&1 &");
run("System Clipboard");

// Add border to snapshot
fColor = getValue("color.foreground");
setForegroundColor(111, 121, 132);
drawRect(0, 0, getWidth, getHeight);
setForegroundColor(fColor);
