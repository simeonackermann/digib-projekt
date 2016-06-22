import ij.*;
import ij.process.*;
import interpolators.BilinearInterpolator;
import interpolators.PixelInterpolator;
import mappings.ProjectiveMapping;
import ij.gui.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import ij.plugin.ContrastEnhancer;
import ij.plugin.PlugIn;
import ij.plugin.filter.*;
//import ij.plugin.Thresholder;
import interpolators.BilinearInterpolator;
import interpolators.PixelInterpolator;
import mappings.ProjectiveMapping;


//public class Projekt10_ implements PlugInFilter {	
public class Projekt10_ implements PlugIn {
	
	ImagePlus imp;
	ImagePlus resultImage;
	private ImagePlus impOriginal;
	private int thresholdValue = 80;
	private int minFieldHeight = 1;
	private int minFieldWidth = 5;
	private int saturation = 25;
	
	int xFields = 4;
    int yFields = 6;
    File[] listOfFiles;
    String imagesFolder = "/Source/";
    String resultFolder = "/Result/";

	/*public int setup(String arg, ImagePlus imp) {

		File folder = new File(imagesFolder);
		listOfFiles = folder.listFiles();
		
		//impOriginal = imp;
		return DOES_ALL;
	}*/
    
	public void run(String arg) {
		// arg is empty... ;(
		//IJ.log("arg:"+arg);
		
		String args = Macro.getOptions();
		
		if ( args == null ) { // direclty call from imagej menu
			imp = WindowManager.getCurrentImage();
			run();
			resultImage.show();
			return; 
		}			
		
		for (String argsp : args.split(" ")) {
			String[] argument = argsp.split("=");
			
			if ( argument[0].equals("xs") ) {
				IJ.log("Its xfileds...");
				this.xFields = Integer.parseInt(argument[1]);
			} 
			if (argument[0].equals("ys") ) {
				yFields = Integer.parseInt(argument[1]);
			} 
			if (argument[0].equals("source") ) {
				imagesFolder = argument[1];
			} 
			if (argument[0].equals("result") ) {
				resultFolder = argument[1];
			}
		}
		
		File folder = new File(imagesFolder);
		
		if ( ! folder.isDirectory() || !(new File(resultFolder)).isDirectory() ) {
			IJ.log("Source or Results folder not given! Call: Projekt10 xs=4 ys=6 source=/path/to/sourceFolder result=/path/to/resultFolder");
			return;
		}
		
		listOfFiles = folder.listFiles();
		
		IJ.log("# Options:\n- x-Fields: "+xFields+"\n- y-Fields:"+yFields+"\n- Images Folder: "+imagesFolder+"\n- Results Folder: "+resultFolder);
		IJ.log("\n# Task: Transform "+listOfFiles.length+" files:");
		
		for (File file : listOfFiles) {
		    if (file.isFile()) {
		        String[] filename = file.getName().split("\\.");
		    	
		    	imp = IJ.openImage(file.getAbsolutePath());
		    	
		    	IJ.log("transforming "+file.getName()+"...");
		    	run();
		    	
		    	
		    	IJ.save(resultImage, resultFolder+filename[0]+"-transformed."+filename[1]);
		    	
		    }
		}
		IJ.log("done.");
		
	}
	
	private void run() {
		// optimize image
		ImagePlus tmpImp = optimizeImg(imp.duplicate() );
		
		// set as current image
		//optImp.setActivated();
		//WindowManager.setTempCurrentImage(optImp);
		
		// get vertical matches as lines
		ArrayList<Line> vHits;
		vHits = getVMatches(tmpImp);
		
		// get horizontal matches - mh lecker Matjes...
		ArrayList<Line> hHits;
		hHits = getHMatches(tmpImp);
           
        // filter horizontal matches, if they dont cross any vertical match
        ArrayList<Line> hHitsFiltered = new ArrayList<Line>();
        for (int i = 0; i < hHits.size(); i++) {
        	Line hLine = hHits.get(i);
        	boolean crossed = false;
        	
        	for (int j = 0; j < vHits.size(); j++) {
        		Line vLine = vHits.get(j);
				if ( 	hLine.x1 <= vLine.x1 && hLine.x2 >= vLine.x2 &&
						hLine.y1 >= vLine.y1 && hLine.y2 <= vLine.y2 ) {
					crossed = true;
					break;
				}
			}        	
        	if ( crossed ) {
        		hHitsFiltered.add(hHits.get(i));
        	}
		}
        hHits = hHitsFiltered;
        
        // filter vertical matches, if they dont cross any horizontal match
        ArrayList<Line> vHitsFiltered = new ArrayList<Line>();
        for (int i = 0; i < vHits.size(); i++) {
        	Line vLine = vHits.get(i);
        	boolean crossed = false;
        	
        	for (int j = 0; j < hHits.size(); j++) {
        		Line hLine = hHits.get(j);
        		
        		if (	vLine.y1 <= hLine.y1 && vLine.y2 >= hLine.y2 &&
        				vLine.x1 >= hLine.x1 && vLine.x2 <= hLine.x2 ) {
        			        		
					crossed = true;
					break;
				}
			}        	
        	if ( crossed ) {
        		vHitsFiltered.add(vHits.get(i));
        	}
		}
        vHits = vHitsFiltered;        
        
        if ( vHits.size() == 0 || hHits.size() == 0 ) {
        	// -> not found
        	IJ.showMessage("Projekt 10", "Schessboard not found...");
        	return;
        }
        
        
        ImageConverter ic = new ImageConverter(tmpImp);
        ic.convertToRGB();
        
        ImageProcessor ipN = tmpImp.getProcessor();
        
        ipN.setColor(Color.RED);
        for (Line line : vHits) {
        	ipN.drawLine(line.x1, line.y1, line.x2, line.y2);
		}
        
        ipN.setColor(Color.GREEN);
        for ( Line line : hHits) {
        	ipN.drawLine(line.x1, line.y1, line.x2, line.y2);
        }
		
		
		
		// create result image
		//ImagePlus targetImp = optimizeImg(impOriginal.duplicate() );
		//ImageConverter icT = new ImageConverter(targetImp);
        //icT.convertToGray8();
		
		//get edges
        Point2D[] ps = new Point2D[4];
        ps[0]  = new Point( hHits.get(0).x1, vHits.get(0).y1 );
        ps[1] = new Point( hHits.get(0).x2, vHits.get(vHits.size()-1).y1 );
        ps[2] = new Point( hHits.get(hHits.size()-1).x2, vHits.get(vHits.size()-1).y2 );
        ps[3] = new Point( hHits.get(0).x1, vHits.get(0).y2 );
        
        Point2D[] qs = new Point2D[4];
        int yStretch= (int) ( ((float)yFields/(float)xFields) * (ps[2].getX()-ps[3].getX()));
		//IJ.log(((float)yFields/(float)xFields)+" - "+(ps[2].getX()-ps[3].getX())+" = "+yStretch);
		
		qs[0] = new Point((int)ps[3].getX(), (int)ps[3].getY()-yStretch);
		qs[1] = new Point((int)ps[2].getX(), (int)ps[2].getY()-yStretch);
		qs[2] = new Point((int)ps[2].getX(), (int)ps[2].getY());
		qs[3] = new Point((int)ps[3].getX(), (int)ps[3].getY());
		
		//ImagePlus resultImp = projectiveTransforming(impOriginal, ps, qs);
		projectiveTransforming(ps, qs);
		//resultImage.show();
		
		// show tmp image
		//tmpImp.setTitle("image filtered");
		//tmpImp.show();
	}
	
	private ImagePlus optimizeImg(ImagePlus imp) {
		// convert to gray
        ImageConverter ic = new ImageConverter(imp);
        ic.convertToGray8();
        
        ImageProcessor ip = imp.getProcessor();
        
        // enhance contrast
        ContrastEnhancer ce = new ContrastEnhancer();
        ce.stretchHistogram(ip, saturation);
        
        // binarisize
     	//ip.autoThreshold();
     	//ip.threshold(thresholdValue); // TODO: calc opt threshold
     	//ip.setBinaryThreshold();
        
        return imp;
	}
	
	private ArrayList<Line> getHMatches(ImagePlus imp) {
		ImageProcessor ip = imp.getProcessor();
		ArrayList<Line> allHits = new ArrayList<Line>();
		int prevVal, count, prevCount;		
		
		for (int i=2*(ip.getHeight()/3); i < ip.getHeight(); i++) {
			List<ColorCounter> colors = new ArrayList<ColorCounter>();
			prevVal = -1;
			count = 0;
			prevCount = 0;
			for (int j=ip.getWidth()/3; j < 2*(ip.getWidth()/3); j++) {
				int gw = ip.getPixel(j,i);
				
				gw = (gw >= thresholdValue)? 1 : 0; // 0 - schwarz, 1 - weiss
				
				if ( gw == prevVal ) {
					count++;
				} else {
					prevCount=count;					
					count=1;
					colors.add( new ColorCounter(prevVal, j, prevCount) );
				}
				
				prevVal = gw;
			}
			
			colorLoop : for (int u = 4; u < colors.size(); u++) {				
				ColorCounter curColor = colors.get(u);
				
				// we only need white chess board and fields bigger min
				if ( curColor.color != 1 || curColor.count < minFieldWidth ) {
					continue;
				}
				
				// cur color fits 6 colors before
				if ( curColor.count < colors.get(u-4).count/4 || curColor.count > 4*colors.get(u-4).count ) {
					continue;
				}
				
				float lastBoxCount = colors.get(u-1).count; // last chess field box
				float boxesCount = lastBoxCount;
				
				// last box should bigger then min 
				if ( lastBoxCount < minFieldWidth ) {
					continue;
				}
				
				// board is bigger then chess box -> depends on angle!
				/*if ( curColor.count < lastBoxCount || curColor.count > 4*lastBoxCount ) {
					continue;
				}*/				
				 
				// loop the chess fields, check if they are same sizes
				for (int v = u-2; v >= u-3; v--) {
					float curBoxCount = colors.get(v).count;
					boxesCount += curBoxCount;
					
					if ( curBoxCount < minFieldWidth ) {
						continue colorLoop;
					}
					
					if ( lastBoxCount < curBoxCount/2|| lastBoxCount > 2*curBoxCount ) {
						continue colorLoop;
					}
					
				}
				
				// all chessfields / 3 should fit all chess fields size
				for (int v = u-1; v >= u-3; v--) {
					float curBoxCount = colors.get(v).count;
					
					if ( curBoxCount < boxesCount/3/2 || curBoxCount > 2*(boxesCount/3) ) {
						continue colorLoop;
					}
				}				
				
				//IJ.log("MATCH, x="+i+", y="+colCounts.get(u).start);
				allHits.add(new Line( colors.get(u-4).start-colors.get(u-4).count, i, colors.get(u).start-1, i));								
			}
			
		}
		
		
		return allHits;
	}
	
	private ArrayList<Line> getVMatches(ImagePlus imp) {
		ImageProcessor ip = imp.getProcessor();
		ArrayList<Line> allHits = new ArrayList<Line>();
		int prevVal;
		int count = 0;
		int prevCount = 0;
		/*int hits = 0;
		float chessFieldVarianz;
		int startLine = -1;*/		
		
		for (int i=0; i < ip.getWidth(); i++) {
			List<ColorCounter> colors = new ArrayList<ColorCounter>();
			prevVal = -1;
			//hits = 0;
			count = 0;
			prevCount = 0;
			//startLine = -1;
			for (int j=2*(ip.getHeight()/3); j < ip.getHeight(); j++) {
				int gw = ip.getPixel(i,j);
				gw = (gw >= thresholdValue)? 1 : 0; // 0 - schwarz, 1 - weiss
				//ip.set(i, j, (gw == 0)? 0 : 255 );
				
				if ( gw == prevVal ) {
					count++;
				} else {
					
					/*chessFieldVarianz = count/4;
					if ( count > minFieldHeight && ( count >= prevCount-chessFieldVarianz && count <= prevCount+chessFieldVarianz ) ) {
						
						if ( hits == 0 ) {
							startLine = j-(count+prevCount);
						}
						hits++;
					} else {
						hits=0;
					}
					if ( hits == 4 ) {
						IJ.log("x="+i+", y="+j+" ,hits:"+hits);
						//IJ.log("Startline:"+startLine);
						//allHits.add(new Line(i, startLine, i, j));
					}*/									
					
					prevCount=count;					
					count=1;
					colors.add( new ColorCounter(prevVal, j, prevCount) );
				}
				
				//prevCount++;
				prevVal = gw;
			}
			
			colorLoop : for (int u = 6; u < colors.size(); u++) {				
				ColorCounter curColor = colors.get(u);
				
				// we only need white chess board and fields bigger min
				if ( curColor.color != 1 || curColor.count < minFieldHeight ) {
					continue;
				}
				
				// cur color fits 6 colors before
				if ( curColor.count < colors.get(u-6).count/3 || curColor.count > 3*colors.get(u-6).count ) {
					continue;
				}
				
				float lastBoxCount = colors.get(u-1).count; // last chess field box
				float boxesCount = lastBoxCount;
				
				// last box should bigger thin min 
				if ( lastBoxCount < minFieldHeight ) {
					continue;
				}
				
				// board is bigger then chess box
				if ( curColor.count < lastBoxCount || curColor.count > 4*lastBoxCount ) {
					continue;
				}				
				 
				// loop the chess fields, check if they are same sizes
				for (int v = u-2; v >= u-5; v--) {
					float curBoxCount = colors.get(v).count;
					boxesCount += curBoxCount;
					
					if ( curBoxCount < minFieldHeight ) {
						continue colorLoop;
					}
					
					if ( lastBoxCount < curBoxCount/2|| lastBoxCount > 2*curBoxCount ) {
						continue colorLoop;
					}
					
				}
				
				// all chessfields / 5 should fit all chess fields size
				for (int v = u-1; v >= u-5; v--) {
					float curBoxCount = colors.get(v).count;
					
					if ( curBoxCount < boxesCount/5/2 || curBoxCount > 2*(boxesCount/5) ) {
						continue colorLoop;
					}
				}				
				
				//IJ.log("MATCH, x="+i+", y="+colCounts.get(u).start);
				allHits.add(new Line(i, colors.get(u-6).start-colors.get(u-6).count, i, colors.get(u).start-1));								
			}
			/*
			int curRowI = 0;
			int colI = 0;
			int curColorVal = -1;
			int prevCololorVal = -1;
			for (ColorCounter col : colors) {
				curRowI += col.count;
				
				if ( colI >= 4 ) {
					int sum5Fields = 0;
					boolean isPattern = true;
					for (int k = colI; k >= colI-4; k--) {
						//sum5Fields += 
						chessFieldVarianz = col.count/4;
						if ( 	col.count < minFieldHeight ||
								col.count < colors.get(k).count - chessFieldVarianz || col.count > colors.get(k).count + chessFieldVarianz ) {
							
								isPattern = false;
							
						} else {
							sum5Fields += colors.get(k).count;
						}
					}
					
					if ( isPattern ) {
						IJ.log("Hit on "+i);
						//allHits.add(new Line(i, curRowI-sum5Fields, i, curRowI));
						//allHits.add(new Line(i, col.start, i, col.start-sum5Fields));
					}
				}
				prevCololorVal = col.color;
				colI++;
			}
			*/
			
		}
		
		
		return allHits;
		
	}
	
	public void projectiveTransforming(Point2D[] ps, Point2D[] qs) {
		
		ProjectiveMapping map = ProjectiveMapping.makeMapping(ps[0],ps[1],ps[2],ps[3],qs[0],qs[1],qs[2],qs[3]);

        ImageProcessor actualIP = imp.getProcessor();
        ImageProcessor transformedIP = null;

        int targetWidth = imp.getWidth();
        int targetHeight = imp.getHeight();        
                 
        if (actualIP instanceof ByteProcessor)
           transformedIP = new ByteProcessor(targetWidth,targetHeight);
        else if (actualIP instanceof ShortProcessor)  
           transformedIP = new ShortProcessor(targetWidth,targetHeight);
        else if (actualIP instanceof FloatProcessor)  
           transformedIP = new FloatProcessor(targetWidth,targetHeight);
        else if (actualIP instanceof ColorProcessor)  
           transformedIP = new ColorProcessor(targetWidth,targetHeight);
        
        PixelInterpolator ipol;
		ipol = new BilinearInterpolator();
        //IJ.log(""+actualIP.getNChannels());
        FloatProcessor fp1 = null, fp2 = null;          // non-float images will be converted to these
        for (int i=0; i<actualIP.getNChannels(); i++) { //grayscale: once. RBG: once per color, i.e., 3 times
            fp1 = actualIP.toFloat(i, fp1);             // convert image or color channel to float (unless float already)
            fp2 = transformedIP.toFloat(i, fp2);
            map.transformTo(fp1, fp2, ipol);
            transformedIP.setPixels(i, fp2);                 // convert back from float (unless ip is a FloatProcessor)
        }
        resultImage = new ImagePlus("Projekt 10 - Result", transformedIP);
        //return new ImagePlus("Projekt 10 - Result", transformedIP);
		
	}
	
	
	private class ColorCounter {
		public int color;
		public int start;
		public float count;
		
		public ColorCounter(int color, int start, float count) {
			this.color = color;
			this.start = start;
			this.count = count;
		}
	}

}
