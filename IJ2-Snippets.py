# @UIService uiService
# @LogServive ls

def getRoiManager():
    """ Returns an empty IJ1 ROI Manager """
    from org.scijava.ui.DialogPrompt import MessageType, OptionType, Result
    from ij.plugin.frame import RoiManager
    rm = RoiManager.getInstance()
    if rm is None:
        rm = RoiManager()
    elif rm.getCount() > 0:
        mt = MessageType.WARNING_MESSAGE
        ot = OptionType.YES_NO_OPTION
        result = uiService.showDialog("Clear All ROIs on the list?", "ROI Manager", mt, ot)
        if result is Result.YES_OPTION:
            rm.reset()
        else:
            rm = None
    return rm


def log(*arg):
    """ Convenience log function """
    ls.info("%s" % ''.join(map(str, arg)))
