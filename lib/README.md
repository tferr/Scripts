#lib
The lib directory is a centralized repository of user-defined libraries that can be shared across
files. These libraries serve as scripting additions to [Snippets] and other routines. The
`BAR> Snippets> New Snippet...` command exemplifies how to use them.

**Tip:** Type in `BARlib` in _BAR> Snippets> Search BAR_ to get the list of all the files using
scripting additions.

**See Also:** [Snippets], [BAR API](http://tferr.github.io/Scripts/apidocs/),
[User documentation](http://imagej.net/BAR#lib)


##[BSH lib](./BARlib.bsh)
BeanShell library.
Loading instructions (from _Snippets> [New Snippet...](../Snippets/README.md#snippets)_):

```java
// Add BAR/lib to classpath and load BARlib.bsh
addClassPath(bar.Utils.getBARDir());
importCommands("lib/");
BARlib();

// Initiate BARlib and confirm its availability
lib = new BARlib();
lib.confirmLoading();
```

##[CLJ lib](./BARlib.clj)
Clojure library.
Loading instructions (from _Snippets> [New Snippet...](../Snippets/README.md#snippets)_):

```clojure
;; Load BARlib.clj
(load-file (str (bar.Utils/getLibDir) "BARlib.clj"))

;; Confirm BARlib availability
(confirmLoading)
```

##[GVY lib](./BARlib.groovy)
Groovy library.
Loading instructions (from _Snippets> [New Snippet...](../Snippets/README.md#snippets)_):

```groovy
// Parse and load BARlib.groovy
import bar.Utils
file = new File(Utils.getLibDir() + "BARlib.groovy")
BARlib = new GroovyClassLoader(getClass().getClassLoader()).parseClass(file)

// Initiate BARlib and confirm its availability
lib = BARlib.newInstance()
lib.confirmLoading()
```

##[IJM lib](./BARlib.ijm)
ImageJ Macro library.
Loading instructions (from _Snippets> [New Snippet...](../Snippets/README.md#snippets)_):

```java
// Load BARlib.ijm. NB: functions may only be available once
// a new instance of the macro interpreter is initiated. See
// http://imagej.net/BAR#FAQ for details
libPath = call('bar.Utils.getLibDir') + 'BARlib.ijm';
libContents = File.openAsString(libPath);
call('ij.macro.Interpreter.setAdditionalFunctions', libContents);

// Confirm availability of new additions
confirmLoading();
```

##[JS lib](./BARlib.js)
JavaScript library.
Loading instructions (from _Snippets> [New Snippet...](../Snippets/README.md#snippets)_):

```javascript
// Load BARlib.js
importClass(Packages.bar.Utils);
load(Utils.getLibDir() +"BARlib.js");

// Initiate BARlib and confirm its availability
lib = new BARlib();
lib.confirmLoading();
```

##[PY lib](./BARlib.py)
Python (Jython) library.
Loading instructions (from _Snippets> [New Snippet...](../Snippets/README.md#snippets)_):

```python
# Load BARlib.py
import sys
from bar import Utils as barUtils
sys.path.append(barUtils.getLibDir())
import BARlib as lib

# Confirm availability of BARlib
lib.confirmLoading()
```

##[RB lib](./BARlib.rb)
Ruby (JRuby) library.
Loading instructions (from _Snippets> [New Snippet...](../Snippets/README.md#snippets)_):

```ruby
# Load BARlib.rb
java_import "bar.Utils"
require "#{Utils.getLibDir}" + "BARlib.rb"

# Initiate BARlib and confirm its availability
lib = BARlib.new()
lib.confirmLoading
```


##Notes
* For more details on the methods provided by the
  <tt>[bar.Utils](http://tferr.github.io/Scripts/apidocs/index.html?bar/Utils.html)</tt> class, have a look
  at the [BAR API](http://tferr.github.io/Scripts/apidocs/)
* Library filenames have no underscore and so are not registered in the `BAR>lib>` menu. To open lib
  files from within ImageJ, use [Commander](../BAR/src/main/java/bar/plugin/Commander.java) via
  _BAR> lib> Browse /lib_ (or type `!lib` in Commander's search prompt).
* [lib tests](./tests/) can be used to debug libraries



| [Home] | [Analysis] | [Data Analysis] | [Annotation] | [Segmentation] | [Tools] | [Plugins] | [lib] | [Snippets] | [IJ] |
|:------:|:----------:|:---------------:|:------------:|:--------------:|:-------:|:---------:|:-----:|:----------:|:----:|

[Home]: https://github.com/tferr/Scripts#ij-bar
[Analysis]: https://github.com/tferr/Scripts/tree/master/Analysis#analysis
[Data Analysis]: https://github.com/tferr/Scripts/tree/master/BAR/src/main/resources/scripts/BAR/Data_Analysis#data-analysis
[Annotation]: https://github.com/tferr/Scripts/tree/master/Annotation#annotation
[Segmentation]: https://github.com/tferr/Scripts/tree/master/Segmentation#segmentation
[Tools]: https://github.com/tferr/Scripts/tree/master/Tools#tools-and-toolsets
[Java Classes]: https://github.com/tferr/Scripts/tree/master/BAR#java-classes
[lib]: https://github.com/tferr/Scripts/tree/master/lib#lib
[Snippets]: https://github.com/tferr/Scripts/tree/master/Snippets#snippets
[IJ]: http://imagej.net/BAR
