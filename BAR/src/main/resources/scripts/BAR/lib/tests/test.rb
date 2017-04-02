# test.rb
# IJ BAR: https://github.com/tferr/Scripts#scripts
#
# Tests correct loading of BARlib


java_import "bar.Utils"
require "#{Utils.getLibDir}" + "BARlib.rb"

def test(function)
    print("Test #{$counter}: #{function}\n")
    $counter += 1
end

$counter = 1;
lib = BARlib.new()

test(defined? lib.confirmLoading)
test(defined? lib.getClipboardText)
test(lib.randomString)
test(lib.getSimpleMovingAverage([1.0,2.0,3.0,4.0],2))
test(lib.gcd(4,2))
test(lib.gcdCommons(4,2))
test(lib.sphereCalc(10))
