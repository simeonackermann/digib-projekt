/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import ij.*;
import ij.process.*;
import ij.plugin.filter.*;
import ij.plugin.ContrastEnhancer;

public class findchess_ implements PlugInFilter {
    private ImagePlus impOriginal;
          
    @Override
    public int setup(String arg, ImagePlus imp1) {
            impOriginal = imp1;
            return DOES_ALL;
    }

    @Override
    public void run(ImageProcessor ip) {
        ImagePlus impFiltered = impOriginal.duplicate();

        // Kontrast erhöhen und Kanten hervorheben
        enhanceContrast(impFiltered.getProcessor(), 50).findEdges();
        
        // In Graustufen konvertieren
        ImageConverter ic = new ImageConverter(impFiltered);
        ic.convertToGray8();
        
        // Anzeige
        impFiltered.setTitle("image filtered");
        impFiltered.show();
    }
    
    private ImageProcessor enhanceContrast(ImageProcessor ip, double value) {
        ContrastEnhancer ce = new ContrastEnhancer();
        ce.stretchHistogram(ip, value);
        return ip;
    }

}
