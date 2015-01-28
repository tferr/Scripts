#lib
The lib directory is a centralized repository of user-defined libraries that can be shared across
files. These libraries serve as scripting additions to [Snippets] and other routines. The
`BAR> Snippets> New Snippet...` command exemplifies how to use them.

**Tip:** Type in `BARlib` in _BAR> Snippets> Search BAR_ to get the list of all the files using
scripting additions.

**See Also:** [Snippets], [BAR documentation page](http://fiji.sc/BAR#lib)


##[BSH lib](./BARlib.bsh)
Loading instructions, taken from [`Snippets> New Snippet...`](../Snippets/README.md#snippets):

```java
import bar.Utils;

/* Add BAR/lib to classpath. Load BARlib.bsh */
addClassPath(bar.Utils.getBARDir());
importCommands("lib/");
BARlib();
lib = new BARlib();

// Confirm availability of BARlib
lib.confirmLoading();
```

##[IJM lib](./BARlib.ijm)
Loading instructions, taken from [`Snippets> New Snippet...`](../Snippets/README.md#snippets):

```java
// Load BARlib.ijm. NB: functions may only be available once
// a new instance of the macro interpreter is initiated. See
// http://fiji.sc/BAR#FAQ for details
libPath = call('bar.Utils.getLibDir') + 'BARlib.ijm';
libContents = File.openAsString(libPath);
call('ij.macro.Interpreter.setAdditionalFunctions', libContents);

// Confirm availability of new additions
confirmLoading();
```

##[PY lib](./BARlib.py)
Loading instructions, taken from [`Snippets> New Snippet...`](../Snippets/README.md#snippets):

```python
import sys, bar
sys.path.append(bar.Utils.getLibDir())
import BARlib as lib

# Confirm availability of BARlib.py
lib.confirmLoading()
```


##Notes
* Library filenames have no underscore and so are not registered in the `BAR>lib>` menu. You will
  notice that, `BAR>lib>` lists some "Opening" commands instead. These are just one line-scrips that
  call [bar.Utils](../BAR/src/main/java/bar/Utils.java) to open unregistered files.
  This approach is intentional: If the calls were included directly in the library file, they would
  be executed every time the library is loaded.



| [Home] | [Analysis] | [Data Analysis] | [Annotation] | [Segmentation] | [Tools] | [Plugins] | [lib] | [Snippets] | [Fiji] |
|:------:|:----------:|:---------------:|:------------:|:--------------:|:-------:|:---------:|:-----:|:----------:|:------:|

[Home]: https://github.com/tferr/Scripts#ij-bar
[Analysis]: https://github.com/tferr/Scripts/tree/master/Analysis#analysis
[Data Analysis]: https://github.com/tferr/Scripts/tree/master/Data_Analysis#data-analysis
[Annotation]: https://github.com/tferr/Scripts/tree/master/Annotation#annotation
[Segmentation]: https://github.com/tferr/Scripts/tree/master/Segmentation#segmentation
[Morphometry]: https://github.com/tferr/Scripts/tree/master/Morphometry#morphometry
[Tools]: https://github.com/tferr/Scripts/tree/master/Tools#tools-and-toolsets
[Plugins]: https://github.com/tferr/Scripts/tree/master/BAR#bar-plugins
[lib]: https://github.com/tferr/Scripts/tree/master/lib#lib
[Snippets]: https://github.com/tferr/Scripts/tree/master/Snippets#snippets
[Fiji]: http://fiji.sc/BAR
