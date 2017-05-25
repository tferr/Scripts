#@ImageJ ij
#@UIService ui
#@LogService log
#@ScriptService script
#@DisplayService display
#@AppService appService
require_relative "#{$appService.getApp.getBaseDirectory}/plugins/JRuby/imagej.rb"

#header
java_import "bar.Utils"

def run

	# Load template BAR lib (BARlib.rb) (see 'BAR> Utilities>
	# Install Multi-language libs...'). Exit if file is not
	# available
	begin
		require "#{Utils.getLibDir}BARlib.rb"
	rescue LoadError => e
		$ui.showDialog("File not found: BARlib.rb", "Error")
		$log.error(e)
		return
	end

	# Your code here... e.g., confirm access to loaded file
	lib = BARlib.new()
	lib.confirmLoading()

end


run
