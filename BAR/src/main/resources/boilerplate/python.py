#@ImageJ ij
#@UIService ui
#@LogService log
#@ScriptService script
#@DisplayService display

#header
import bar
import sys

def main():

    # Load template BAR lib (BARlib.py) (see 'BAR> Utilities>
    # Install Multi-language libs...'). Exit if file is not
    # available
    sys.path.append(bar.Utils.getLibDir())
    try:
        import BARlib as lib
    except ImportError:
        ui.showDialog("File not found: BARlib.py", "Error")
        return

    # Your code here... e.g., confirm access to loaded file
    lib.confirmLoading()


if __name__ == '__main__':
    main()
