
import ij.*;
import ij.gui.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import java.awt.*;
import java.lang.Math;

import common514.IOFile;


public class Shen_Castan_514 implements PlugInFilter
{
	private ImagePlus imp;
	private ImageProcessor ip;
	private boolean window=true;	//opens a new window to show the result or not
	private int width;
	private int height;
	private double f;					//smoothing factor
	private IOFile saveDefaults;
	double[] tmpresX;		//x coords of the gradient
	double[] tmpresY;		//y coords of the gradient
	
	
	public Shen_Castan_514()
		{
		}

	public int setup(String args, ImagePlus imp)
		{
		if(args.equals("about"))
			{
			showAbout();
			return DONE;
			}
		if(args.equals("no_window"))
		    window=false;
		if (imp==null)return IJ.setupDialog(imp, DOES_ALL);
		else 
			{
			this.imp=imp;
			this.ip=imp.getProcessor();
			width=imp.getWidth();
			height=imp.getHeight();
			saveDefaults=new IOFile("./shen.ini");
			return DOES_8G;
			}
 		}
		
	public void run(ImageProcessor ip)
		{
		if(!getProperties())return;
        
		ImagePlus nouv=NewImage.createByteImage("Shen", width, height, 1, NewImage.FILL_WHITE);
		ImageProcessor ip2=nouv.getProcessor();
		
		byte[] res=new byte[width*height];
		byte[] pixels=(byte[])ip.getPixels();
		double[] row1=new double[width];
		double[] row2=new double[width];
		tmpresX=new double[width*height];
		tmpresY=new double[width*height];
		double[] tmpres=new double[width*height];
		double a= ((1-Math.exp(-f)))/(1+Math.exp(-f)),
			min=1e15,
			max=-1e15,
			den;
		int offset;
        
		//Shen X
		for(int y=height;y-->0;)	//for all the rows
			{
			offset=y*width;
            
			row1[0]=a*pixels[offset];
			for (int x=width;x-->1;)
				row1[width-x]=a*((pixels[offset+width-x]&0xff)-row1[width-x-1])+row1[width-x-1];
            
			row2[width-1]=a*row1[width-1];
			for (int x=width;x-->1;)
				row2[x-1]=a*((pixels[offset+x-1]&0xff)-row2[x])+row2[x];
            
			for (int x=width;x-->0;)
				{
				tmpresX[offset+x]=f*(row2[x]-row1[x]);
				/*if(tmpres[offset+x]>max)max=tmpres[offset+x];
				if(tmpres[offset+x]<min)min=tmpres[offset+x];*/
				}
			}
		//Shen Y
		row1=new double[height];
		row2=new double[height];
		for(int x=width;x-->0;)	//for all the columns
			{
			row1[0]=a*(pixels[x]-a*pixels[x])+a*pixels[x];
			for (int y=height;y-->1;)
				row1[height-y]=a*((pixels[width*(height-y)+x]&0xff)-row1[height-y-1])+row1[height-y-1];
            
			row2[height-1]=a*row1[height-1];
			for (int y=height;y-->1;)
				row2[y-1]=a*((pixels[width*(y-1)+x]&0xff)-row2[y])+row2[y];
            
			for (int y=height;y-->0;)
				{
				tmpresY[width*y+x]=f*(row2[y]-row1[y]);
				/*if(tmpres[width*y+x]>max)max=tmpres[width*y+x];
				if(tmpres[width*y+x]<min)min=tmpres[width*y+x];*/
				}
			}
		//den=max-min;
		for(int y=height;y-->0;)	//for all the rows
			{
			offset=y*width;
			for (int x=width;x-->0;)
			if((tmpres[offset+x]=Math.sqrt(tmpresX[offset+x]*tmpresX[offset+x]+tmpresY[offset+x]*tmpresY[offset+x]))>max)max=tmpres[offset+x];//(255*(tmpres[offset+x]-min))/den));
			}
		for(int y=height;y-->0;)	//for all the rows
			{
			offset=y*width;
			for (int x=width;x-->0;)
				res[offset+x]=(byte)((255*tmpres[offset+x])/max);
			}
        
		if(window)
			{
			ip2.setPixels(res);
			nouv.show();
			}
		else ip.setPixels(res);
		}
    
	public boolean getProperties()
		{
		GenericDialog gd=new GenericDialog("Shen filter settings");
		gd.addMessage("You have to specify the Shen-Castan coefficient. \n"+
			"It must be between 0 and 1.It corresponds\n"+
                        "to the smoothing factor. O corresponds to high\n"+
                        "smoothing and 1 corresponds to no smoothing.");
		String def=saveDefaults.getContent(10,"0.5");
		gd.addNumericField("Shen-Castan coefficient (alpha):", (new Double(def)).doubleValue(), 1);
		gd.showDialog();
		if(gd.wasCanceled()) return false;
		f=gd.getNextNumber();
		saveDefaults.write(String.valueOf(f));
		return true;
		}

	public void showAbout()
		{
		IJ.showMessage("Shen filter...","This plugin process a Shen filter on the image.");
		}

	public double[] getX()
		{
		return tmpresX;
		}

	public double[] getY()
		{
		return tmpresY;
		}
}
