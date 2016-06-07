import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;

import ij.plugin.ContrastEnhancer;
import ij.plugin.filter.*;
import ij.plugin.Thresholder;

public class Projekt10_ implements PlugInFilter {
	ImagePlus imp;
	private ImagePlus impOriginal;
	private ImagePlus impNew;

	public int setup(String arg, ImagePlus imp) {
		impOriginal = imp;
		impNew = imp.duplicate();
		return DOES_ALL;
	}

	public void run(ImageProcessor ip) {
		
		optimizeImg();
		
		// show new image
        impNew.setTitle("image filtered");
        impNew.show();			
	}
	
	private void optimizeImg() {
		//ImagePlus impFiltered = impOriginal.duplicate();
		ImageProcessor ip = impNew.getProcessor();
        
        // enhance contrast
        ContrastEnhancer ce = new ContrastEnhancer();
        ce.stretchHistogram(ip, 50);
        
        // binarisize
     	ip.autoThreshold();
		
		// convert to gray
        ImageConverter ic = new ImageConverter(impNew);
        ic.convertToGray8();
        
        
	}
	
	private void findChess() {
		
	}
	
	private void calibrate() {
		
	}
}
