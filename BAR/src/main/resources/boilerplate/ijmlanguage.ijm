//@UIService ui
//@ScriptService script
//@DisplayService display

//header

// Load BARlib.ijm. NB: functions may only be available once
// a new instance of the macro interpreter is initiated. See
// http://imagej.net/BAR#FAQ for details
libPath = call('bar.Utils.getLibDir') + 'BARlib.ijm';
libContents = File.openAsString(libPath);
call('ij.macro.Interpreter.setAdditionalFunctions', libContents);

// Confirm availability of new additions
confirmLoading();
