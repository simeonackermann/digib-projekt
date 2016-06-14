/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import ij.*;
import ij.process.*;
import ij.plugin.filter.*;
import ij.plugin.ContrastEnhancer;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

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
        
        // Schachbrett finden
        List<LineScanPosition> positions = findChess(impFiltered.getProcessor());
        ic.convertToRGB();
        impFiltered.getProcessor().setColor(Color.RED);
        for (LineScanPosition position : positions) {
            impFiltered.getProcessor().drawLine(position.getX1(), position.getY1(), position.getX1(), position.getY2());
        }
        
        // Anzeige
        impFiltered.setTitle("image filtered");
        impFiltered.show();
    }
    
    private ImageProcessor enhanceContrast(ImageProcessor ip, double value) {
        ContrastEnhancer ce = new ContrastEnhancer();
        ce.stretchHistogram(ip, value);
        return ip;
    }

    private List<LineScanPosition> findChess(ImageProcessor ip) {
        List<LineScanPosition> positions = new ArrayList();

        for (int x = 0; x < ip.getWidth(); x++) {
            
            int chessLineCounter = 0;
            int y1 = 0;
            int y2 = 0;
            
            List<Integer> upper235 = new ArrayList<Integer>();
            
            for (int y = 0; y < ip.getHeight(); y++) {
                if (ip.getPixel(x, y) > 235) {
                    upper235.add(y);
                    //IJ.log("value" + ip.getPixel(x, y));
                }
            }
            
            List<Integer> distance = new ArrayList<Integer>();
            
            for (int i = 1; i < upper235.size(); i++) {
                distance.add(upper235.get(i) - upper235.get(i-1));
            }
            
            int lastDistance = distance.get(0);
            
            for (int i = 1; i < distance.size(); i++) {
                switch(chessLineCounter)
                {
                    case 5:
                        y2 = upper235.get(i-2);
                        break;
                    case 6:
                        y1 = 0;
                        y2 = 0;
                        chessLineCounter = 0;
                        break;
                    default:
                        if (distance.get(i) > 1) {
                            if(lastDistance * 0.90 < distance.get(i) && lastDistance * 1.1 > distance.get(i))
                            {
                                chessLineCounter++;
                                if (chessLineCounter == 1) y1 = upper235.get(i-1);
                            }
                            lastDistance = distance.get(i);
                        }
                    break;
                }
                //if (chessLineCounter == 5) break;
            }
            
            if (y1 != 0) positions.add(new LineScanPosition(x, x, y1, y2));
        }
        return positions;
    }
}

