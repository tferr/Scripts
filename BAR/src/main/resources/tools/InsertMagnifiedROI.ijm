// Magnifies the contents of a ROI and renders it inside the image canvas.
// The Stack>Tools>Insert command requires a destination image to be a stack. The
// "Zoom_in_Images_and_Stacks" tool by G Carpentier only allows insertions within
// boundaries of the source image. This macro overcomes both these limitations
// and works with hyperstacks.

macro "Insert Magnified ROI Action Tool - C037 R01fb R2397" {

    if (selectionType!=0)
        exit("This tool requires a rectangular selection");

 setBatchMode(true);
    // get image and selection properties
    imgID = getImageID;
    getDimensions(width, height, channels, slices, frames);
    getSelectionBounds(x, y, wROI, hROI);

    // define default values before prompting user
    ch= newArray(1,channels); sl= newArray(1,slices); fr= newArray(1,frames); stroke= 0;
    maxZoom= round(minOf(width/wROI, height/hROI));

    // prompt user to input the zoom factor & ranges in the case of satcks & hyperstacks
      Dialog.create("Insert Magnified ROI Options");
        msg= "Canvas will be enlarged and filled with background color.\n"+
             "Frame around source and magnified ROI will be drawn in\n"+
             "foreground color using the specified frame width. If a\n"+
             "width of zero is chosen, frames are instead displayed\n"+
             "as non-destructive overlays.\n \n"+
             "The maximum zoom factor that can be used to display\n"+
             "the active ROI within current canvas is approximately "+ 
             maxZoom + fromCharCode(0x000D7) +".";
        Dialog.addMessage(msg);
        Dialog.addSlider("Zoom factor:", 1, 10, maxZoom);
        Dialog.addSlider("       Width of frame:", 0, 20, stroke);
        if (channels>1) Dialog.addString("range: Channels (c):", "1-"+channels);
        if (slices>1) Dialog.addString("Slices (z):", "1-"+slices);
        if (frames>1) Dialog.addString("Frames (t):", "1-"+frames);
      Dialog.show();
    zoom= Dialog.getNumber();
    stroke= Dialog.getNumber();
    if (channels>1) ch= split(Dialog.getString,"-");
    if (slices>1) sl= split(Dialog.getString,"-");
    if (frames>1) fr= split(Dialog.getString,"-");

    // get the dimensions of the magnified ROI. Both must be an integrer.
    wMagROI= floor(wROI*zoom); hMagROI= floor(hROI*zoom);

    // duplicate the magnified image. Only the initial ROI will be processed by the "Scale..." command
    run("Scale...", "x=&zoom y=&zoom z=1.0 width=&wMagROI height=&hMagROI interpolation=Bicubic process create title=temp");
    magID= getImageID();

    // define a new canvas size that can fit magROI and get the offsets caused by the enlargement  
    newW= width+(2*wROI*zoom)+50; newH= height+(2*hROI*zoom)+50;
    xOffset= (newW-width)/2; yOffset= (newH-height)/2;

    // resize the canvas
    selectImage(imgID);
    run("Canvas Size...", "width=&newW height=&newH position=Center");   

    // highlight initial selection
    Overlay.drawRect(x+xOffset, y+yOffset, wROI, hROI);
    Overlay.show;

    // this block is from Gilles Carpentier Zoom in Images and Stacks macro tool:
    // http://imagej.nih.gov/ij/macros/tools/Zoom_in_Images_and_Stacks.txt
    x2=-1; y2=-1; z2=-1; flags2=-1; xzo=0; yzo=0; click=0; leftButton=16;
    Overlay.drawRect(xzo, yzo, wMagROI, hMagROI);
    lastItemInOverlay = Overlay.size - 1;
    while (click!=1) {          
        getCursorLoc(xzo, yzo, z, flags);
        if (xzo!=x2 || yzo!=y2 || z!=z2 || flags!=flags2) {
            Overlay.removeSelection(lastItemInOverlay);
            if (flags&leftButton!=0)
            	click = 1;
            Overlay.drawRect(xzo, yzo, wMagROI, hMagROI);
        }
        x2= xzo; y2= yzo; z2= z; flags2= flags;
        wait(5);
    }
    Overlay.removeSelection(lastItemInOverlay);

    // copy the insert to the specified ranges
    saveSettings();
    setPasteMode("Copy");
    for (i=ch[0]; i<=ch[ch.length-1]; i++) {
        for (j=sl[0]; j<=sl[sl.length-1]; j++) {
            for (k=fr[0]; k<=fr[sl.length-1]; k++) {
            	selectImage(magID);
                Stack.setPosition(i, j, k);
                run("Copy");
                selectImage(imgID);
                Stack.setPosition(i, j, k);
                makeRectangle(xzo, yzo, wMagROI, hMagROI);
                run("Paste");
                if (stroke>0) { // foreground color will be used since setColor() was not called
                    setLineWidth(stroke); // for some reason it is not respected if called outside the loop??
                    drawRect(xzo, yzo, wMagROI, hMagROI);
                    drawRect(x+xOffset, y+yOffset, wROI, hROI);
                }
            }
        }
    }
    restoreSettings();

    // crop the canvas, trimming all the extra space. Since stroke width is not being considered
    // frame may be partially cropped on edges. On certain machines a hyperstack is cropped 
    // into a stack unless there is a small delay period (at least in IJ 1.44k)
    x1= minOf(xOffset, xzo); x2= maxOf(xOffset+width, xzo+wMagROI);
    y1= minOf(yOffset, yzo); y2= maxOf(yOffset+height, yzo+hMagROI);
    makeRectangle(x1, y1, x2-x1, y2-y1);
    wait(10); run("Crop");
 
    // if frames were not drawn, we'll add them to the overlay, re-correcting offsets after the crop
    if (stroke==0) {
        setLineWidth(stroke);
        Overlay.drawRect(x+xOffset-x1, y+yOffset-y1, wROI, hROI);
        Overlay.drawRect(xzo-x1, yzo-y1, wMagROI, hMagROI);
        Overlay.show;
    }
 setBatchMode(false);
}
