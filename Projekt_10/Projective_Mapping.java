/**  
  *  Projective_Mapping Version 1.0, 2009-05-20, by 
  *  Martin Schlueter, i3mainz, Mainz University of Applied Sciences, Germany
  *
  *  This ImageJ plugin performs a geometric mapping 
  *  from a source image to an target image using control points.
  *  
  *  Based on code provided by 
  *  Wilhelm BURGER,  Mark J. BURGE:
  *  Digital Image Processing
  *  An Algorithmic Introduction using Java
  *  Textbook, Springer 2008 
  *
  *  To learn more please refer to the related web pages with examples and
  *  applications.
  *
  *  For consultance and practical applications concerning Mobile 3D Coordinate
  *  Measuring Techniques, 3D Digitizing, Deformation Measurement and Analysis 
  *  please contact
  *  Martin Schlueter: schlueter(at)geoinform.fh-mainz.de   
  *  
  *  Copyright (c) [2009-05-20] by Prof. Dr.-Ing. Martin Schlueter, i3mainz,
  *  Mainz University of Applied Sciences, Germany

  *  Permission is hereby granted, free of charge, to any person obtaining a 
  *  copy of this software and associated documentation files (the "Software"), 
  *  to deal in the Software without restriction, including without limitation 
  *  the rights to use, copy, modify, merge, publish, distribute, sublicense, 
  *  and/or sell copies of the Software, and to permit persons to whom the 
  *  Software is furnished to do so, subject to the following conditions:
  *  The above copyright notice and this permission notice shall be included in
  *  all copies or substantial portions of the Software.
  *
  *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
  *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
  *  THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
  *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
  *  DEALINGS IN THE SOFTWARE.
  *
  *  OSI Certified Open Source Software
  *
  * History
  * 2009/05/20: First version online
  *
  */

import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.filter.*;
import interpolators.NearestNeighborInterpolator;
import interpolators.BilinearInterpolator;
import interpolators.BicubicInterpolator;
import interpolators.PixelInterpolator;

import java.awt.geom.Point2D;

import mappings.Mapping;
import mappings.ProjectiveMapping;

public class Projective_Mapping implements PlugInFilter {
    ImagePlus imp;
    private String[] titles;
    int[] wList;
    int actualImageIndex;
    int targetImageIndex;
    ImagePlus Actual;
    ImagePlus Target;
    private static boolean createWindow = false;
    private static int interpolationChoice = 1;

    public int setup(String arg, ImagePlus imp) {
        this.imp = imp;
        return DOES_ALL; //+DOES_32+DOES_RGB;
    }
    
    public void run(ImageProcessor ip) {
        wList = WindowManager.getIDList();
        if (wList==null || wList.length<2) {
            IJ.showMessage("Projective Mapping", "There must be at least two windows open");
            return;
        }
        
        titles = new String[wList.length];
        for (int i=0; i<wList.length; i++) {
            ImagePlus imp = WindowManager.getImage(wList[i]);
            if (imp!=null)
                titles[i] = imp.getTitle();
            else
                titles[i] = "";
        }
        
        if (!showDialog())
            return;

        PixelInterpolator ipol;
        if (interpolationChoice == 0)
           ipol = new NearestNeighborInterpolator();
        else if (interpolationChoice == 1)
           ipol = new BilinearInterpolator();
        else
           ipol = new BicubicInterpolator();

        // Prepare Transformation based on ROI-points;
        // ROI-points from actual image:
        Roi actualRoi = Actual.getRoi();
        if (actualRoi==null || actualRoi.getType() != Roi.POLYGON) {
            IJ.showMessage("Projective Mapping", "No PolygonRoi defined in actual geometry image!");
            return;
        }
        PolygonRoi actualPoRoi = (PolygonRoi) actualRoi; 
        int an = actualPoRoi.getNCoordinates();
        if (an != 4) {
            IJ.showMessage("Projective Mapping", "No 4-corner PolygonROI in actual geometry image!");
            return;
        }
        Rectangle rectactual = actualPoRoi.getBounds();
        int[] xactual = actualPoRoi.getXCoordinates();
        int[] yactual = actualPoRoi.getYCoordinates();

        // ROI-points from target image:
        Roi targetRoi = Target.getRoi();
        if (targetRoi==null || targetRoi.getType() != Roi.POLYGON) {
            IJ.showMessage("Projective Mapping", "No PolygonRoi defined in target geometry image!");
            return;
        }
        PolygonRoi targetPoRoi = (PolygonRoi) targetRoi; 
        int tn = targetPoRoi.getNCoordinates();
        if (tn != 4) {
            IJ.showMessage("Projective Mapping", "No 4-corner PolygonROI in target geometry image!");
            return;
        }
        Rectangle recttarget = targetPoRoi.getBounds();
        int[] xtarget = targetPoRoi.getXCoordinates();
        int[] ytarget = targetPoRoi.getYCoordinates();

        if (an != tn) {
            IJ.showMessage("Projective Mapping", "Number of PolygonROI corners must be equal for both actual and target geometry images!");
            return;
        }

        Point2D p0 = new Point(xactual[0] + rectactual.x, yactual[0] + rectactual.y);
        Point2D p1 = new Point(xactual[1] + rectactual.x, yactual[1] + rectactual.y);
        Point2D p2 = new Point(xactual[2] + rectactual.x, yactual[2] + rectactual.y);
        Point2D p3 = new Point(xactual[3] + rectactual.x, yactual[3] + rectactual.y);
        
        Point2D q0 = new Point(xtarget[0] + recttarget.x, ytarget[0] + recttarget.y);
        Point2D q1 = new Point(xtarget[1] + recttarget.x, ytarget[1] + recttarget.y);
        Point2D q2 = new Point(xtarget[2] + recttarget.x, ytarget[2] + recttarget.y);
        Point2D q3 = new Point(xtarget[3] + recttarget.x, ytarget[3] + recttarget.y);
        
        ProjectiveMapping map = ProjectiveMapping.makeMapping(p0,p1,p2,p3,q0,q1,q2,q3);

        ImageProcessor actualIP = Actual.getProcessor();
        ImageProcessor targetIP = Target.getProcessor();
        ImageProcessor transformedIP = null;

        if (createWindow) {
           int targetWidth = Target.getWidth();
           int targetHeight = Target.getHeight();
            
           if (actualIP instanceof ByteProcessor)
              transformedIP = new ByteProcessor(targetWidth,targetHeight);
           else if (actualIP instanceof ShortProcessor)  
              transformedIP = new ShortProcessor(targetWidth,targetHeight);
           else if (actualIP instanceof FloatProcessor)  
              transformedIP = new FloatProcessor(targetWidth,targetHeight);
           else if (actualIP instanceof ColorProcessor)  
              transformedIP = new ColorProcessor(targetWidth,targetHeight);

           FloatProcessor fp1 = null, fp2 = null;          // non-float images will be converted to these
           for (int i=0; i<actualIP.getNChannels(); i++) { //grayscale: once. RBG: once per color, i.e., 3 times
               fp1 = actualIP.toFloat(i, fp1);             // convert image or color channel to float (unless float already)
               fp2 = transformedIP.toFloat(i, fp2);
               map.transformTo(fp1, fp2, ipol);
               transformedIP.setPixels(i, fp2);                 // convert back from float (unless ip is a FloatProcessor)
           }
           ImagePlus ResultImage = new ImagePlus("Projective Mapping", transformedIP);

           // Anzeige des transformierten Bildes mit dem Target-ROI ueberlagert:
           ResultImage.setRoi(targetPoRoi);
           ResultImage.show();
        }
        else {
           FloatProcessor fp1 = null, fp2 = null;          // non-float images will be converted to these
           for (int i=0; i<actualIP.getNChannels(); i++) { //grayscale: once. RBG: once per color, i.e., 3 times
               fp1 = actualIP.toFloat(i, fp1);             // convert image or color channel to float (unless float already)
               fp2 = targetIP.toFloat(i, fp2);
               map.transformTo(fp1, fp2, ipol);
               targetIP.setPixels(i, fp2);                 // convert back from float (unless ip is a FloatProcessor)
           }
           Target.updateAndDraw();
        }
    }

    public boolean showDialog() {
        final String[] interpolationMethods = { "Next Neighbour", "Bilinear", "Bicubic" };
        GenericDialog gd = new GenericDialog("Projective Mapping");
        gd.addMessage("Choose two images. These images must show\na closed polygon selection containing 4 points each.");
        gd.addChoice("Actual geometry image:", titles, titles[0]);
        gd.addChoice("Target geometry image:", titles, titles[1]);
        gd.addChoice("Interpolation:", interpolationMethods, interpolationMethods[interpolationChoice]);
        gd.addCheckbox("Create New Window", createWindow);
        gd.showDialog();
        if (gd.wasCanceled())
            return false;
        int actualImageIndex = gd.getNextChoiceIndex();
        int targetImageIndex = gd.getNextChoiceIndex();
        Actual = WindowManager.getImage(wList[actualImageIndex]);
        Target = WindowManager.getImage(wList[targetImageIndex]);
        interpolationChoice = gd.getNextChoiceIndex();
        createWindow = gd.getNextBoolean();
        return true;
    }
}
