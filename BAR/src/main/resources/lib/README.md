# lib
The lib directory is a local centralized repository of user-defined libraries that can be
shared across files. These libraries serve as scripting additions to user scripts
(namely for [My Routines]).

Use `BAR>Utilities>Install Multi-language libs...` to populate your local lib directory
with some template libs that exemplify the process and serve as a starting point for
building your own libraries. Note that any files in the local _/scripts/BAR/_ directory
are not synchronized with the BAR update site.

Below are some examples on how lib files can be access from other scripts (the default
template `BARlib` files are assumed (to load your own files, simply replace
_`BARlib.<lang>`_ when appropriate). Also, the `BAR> Utilities> New Snippet...` generates
some [boilerplate] functions and methods that simplify the access to lib files.

You can also have a look at the files in BAR's
[internal lib](https://github.com/tferr/Scripts/tree/master//BAR/src/main/resources/shared)
(these are shared by BAR built-in commands) for further examples.

## BeanShell (BSH lib)

```java
// Add BAR/lib to classpath and load BARlib.bsh
addClassPath(bar.Utils.getBARDir());
importCommands("lib/");
BARlib(); //name of lib class file

// Initiate BARlib and confirm its availability
lib = new BARlib();
lib.confirmLoading();
```

## Clojure (CLJ lib)

```clojure
;; Load BARlib.clj
(load-file (str (bar.Utils/getLibDir) "BARlib.clj"))

;; Confirm BARlib availability
(confirmLoading)
```

## Groovy (GVY lib)

```groovy
// Parse and load BARlib.groovy
import bar.Utils
file = new File(Utils.getLibDir() + "BARlib.groovy")
BARlib = new GroovyClassLoader().parseClass(file)

// Initiate BARlib and confirm its availability
lib = BARlib.newInstance()
lib.confirmLoading()
```

## ImageJ Macro Language (IJM lib)

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

## JavaScript (JS lib)

```javascript
// Load BARlib.js
importClass(Packages.bar.Utils);
load(Utils.getLibDir() +"BARlib.js");

// Initiate BARlib and confirm its availability
lib = new BARlib(); // name of JS function
lib.confirmLoading();
```

## Python (Jython) (PY lib)

```python
# Load BARlib.py
import sys
from bar import Utils as barUtils
sys.path.append(barUtils.getLibDir())
import BARlib as lib // name of file

# Confirm availability of BARlib
lib.confirmLoading()
```

## Ruby (JRuby) (RB lib)

```ruby
# Load BARlib.rb
java_import "bar.Utils"
require "#{Utils.getLibDir}" + "BARlib.rb"

# Initiate BARlib and confirm its availability
lib = BARlib.new()
lib.confirmLoading
```


## Notes
 * For more details on the methods provided `bar.Utils` have a look at the
   [BAR API](http://tferr.github.io/Scripts/apidocs/)
 * Library filenames have no underscore and so are not registered in the `BAR` menu
   hierarchy.
 * You can Browse the `lib` directory by typing <kbd>!lib</kbd> in Commander
 * Use the provided test files to debug your customizations


[boilerplate]: https://github.com/tferr/Scripts/tree/master/BAR/src/main/resources/boilerplate

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
