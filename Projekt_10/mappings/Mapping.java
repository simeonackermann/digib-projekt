/**
 * This sample code is made available as part of the book "Digital Image
 * Processing - An Algorithmic Introduction using Java" by Wilhelm Burger
 * and Mark J. Burge, Copyright (C) 2005-2008 Springer-Verlag Berlin, 
 * Heidelberg, New York.
 * Note that this code comes with absolutely no warranty of any kind.
 * See http://www.imagingbook.com for details and licensing conditions.
 * 
 * Date: 2007/11/10
 */

package mappings;
//import ij.*;
import ij.process.*;
import interpolators.PixelInterpolator;

import java.awt.geom.Point2D;


public abstract class Mapping implements Cloneable {
    boolean isInverse = false;
    
    // subclasses must implement this method
    abstract Point2D applyTo(Point2D pnt);
    
    Mapping invert() {
        throw new IllegalArgumentException("cannot invert mapping");
    }
    
    Mapping getInverse() {
        if (isInverse)
            return this;
        else 
            return invert(); // only linear mappings invert
    }
    
    // transforms the image from ip to ipResult using this geometric mapping    // added by ms, 2009
    // and the specified pixel interpolator
    public void transformTo(ImageProcessor ip, ImageProcessor ipResult, PixelInterpolator intPol){

        Mapping invMap = this.getInverse(); // get inverse mapping 
        intPol.setImageProcessor(ip);

        int w = ipResult.getWidth();
        int h = ipResult.getHeight();

        Point2D pt = new Point2D.Double();

       //  if (ip instanceof ShortProcessor) 
       //  if (ip instanceof ByteProcessor) 
       //  else if (ip instanceof ColorProcessor) pixelsi[i + (j*width)] = ((int []) array[zc-1])[xc + (yc*oldWidth)]; 
       //  else if (ip instanceof FloatProcessor) pixelsf[i + (j*width)] = ((float []) array[zc-1])[xc + (yc*oldWidth)]; 
                                    
//        if (ip instanceof ByteProcessor) {
           for (int v=0; v<h; v++){
               for (int u=0; u<w; u++){
                   pt.setLocation(u,v);
                   invMap.applyTo(pt);
                   //int p = (int) Math.rint(intPol.getInterpolatedPixel(pt));
                   double p = intPol.getInterpolatedPixel(pt);
                   // if(v == 50) IJ.write("hups: " + p);
                   ipResult.putPixelValue(u,v,p); // Value!
                   //ipResult.putPixel(u,v,p);
               }
           }
//        }
//        else if (ip instanceof ColorProcessor) {
//           for (int v=0; v<h; v++){
//               for (int u=0; u<w; u++){
//                   pt.setLocation(u,v);
//                   invMap.applyTo(pt);
//                   int p = (int) Math.rint(intPol.getInterpolatedPixel(pt));
//                   ipResult.putPixel(u,v,p);
//               }
//           }
//        }

    }
    
//    // transforms the image in ip using this geometric mapping
//    // and the specified pixel interpolator
//    public void applyTo(ImageProcessor ip, PixelInterpolator intPol){
//        ImageProcessor targetIp = ip;
//        // make a temporary copy of the image:
//        ImageProcessor sourceIp = ip.duplicate();
//        
//        Mapping invMap = this.getInverse(); // get inverse mapping 
//        intPol.setImageProcessor(sourceIp);
//
//        int w = targetIp.getWidth();
//        int h = targetIp.getHeight();
//
//        Point2D pt = new Point2D.Double();
//        for (int v=0; v<h; v++){
//            for (int u=0; u<w; u++){
//                pt.setLocation(u,v);
//                invMap.applyTo(pt);
//                //int p = (int) Math.rint(intPol.getInterpolatedPixel(pt));
//                double p = intPol.getInterpolatedPixel(pt);
//                //targetIp.putPixel(u,v,p);
//                targetIp.putPixelValue(u,v,p);
//            }
//        }
//    }

    Mapping duplicate() { //clones any mapping
        Mapping newMap = null;
        try {
            newMap = (Mapping) this.clone();
        }
        catch (CloneNotSupportedException e){};
        return newMap;
    }
    
}










