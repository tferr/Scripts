#lib

Centralized repository of user-defined libraries that can be shared across files. These libraries
serve as scripting additions to [Snippets] and other routines. The `BAR> Snippets> New Snippet...`
command exemplifies how to use them.


##[BSH lib](./BARlib.bsh)
Loading instructions:

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
Loading instructions:

```java
libPath = call('bar.Utils.getLibDir') + 'BARlib.ijm';
libFunctions = File.openAsString(libPath);
call('ij.macro.Interpreter.setAdditionalFunctions', libFunctions);

// Confirm availability of new additions
confirmLoading();
```

##[PY lib](./BARlib.py)
Loading instructions:

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

##See Also

* [Snippets]



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
