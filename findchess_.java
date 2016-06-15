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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Findchess_ implements PlugInFilter {
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
        enhanceContrast(impFiltered.getProcessor(), 65).findEdges();
        
        // In Graustufen konvertieren
        ImageConverter ic = new ImageConverter(impFiltered);
        ic.convertToGray8();
        
        // Schachbrett finden
        List<Line> positions = findChess(impFiltered.getProcessor());
        // Bild wieder nach RGB konvertieren
        ic.convertToRGB();
        // Farbe rot setzen und alle gefunden Positionen einzeichnen.
        impFiltered.getProcessor().setColor(Color.RED);
        for (Line position : positions) {
            impFiltered.getProcessor().drawLine(position.getX1(), position.getY1(), position.getX1(), position.getY2());
        }
        
        // Anzeige
        impFiltered.setTitle("image filtered");
        impFiltered.show();
    }
    
    // Kontrast erhoehen
    private ImageProcessor enhanceContrast(ImageProcessor ip, double value) {
        ContrastEnhancer ce = new ContrastEnhancer();
        ce.stretchHistogram(ip, value);
        return ip;
    }

    private List<Line> findChess(ImageProcessor ip) {
        
        List<Line> positions = new ArrayList();
        
        // Spalten durchlaufen
        for (int x = 0; x < ip.getWidth(); x++) {
            
            List<DistancePosition> distances = new ArrayList<DistancePosition>();
            List<DistancePosition> selectedDistances = new ArrayList<DistancePosition>();
            List<Integer> upper235 = new ArrayList<Integer>();
            
            // Alle Zeilen der Spalte durchlaufen und Y-Koordinate in upper235 speichern, falls Helligkeitswert groesser 235
            for (int y = 0; y < ip.getHeight(); y++) {
                if (ip.getPixel(x, y) > 235) {
                    upper235.add(y);
                }
            }
            
            // In die Liste distances die Abstände zwischen den aufeinander folgenden Y-Koordinaten aus upper235 speichern
            for (int i = 1; i < upper235.size(); i++) {
                // Konstruktur: DistancePosition(Distanz, X, Y1, Y2)
                distances.add(new DistancePosition(upper235.get(i) - upper235.get(i-1), x, upper235.get(i-1), upper235.get(i)));
            }
            
            // Die Liste distances nach Distanzen absteigend sortieren
            Collections.sort(distances, new Comparator<DistancePosition>() {
                @Override
                public int compare(DistancePosition dp1, DistancePosition dp2) {
                    if (dp1.getDistance() > dp2.getDistance()) return -1;
                    else if (dp1.getDistance() < dp2.getDistance()) return 1;
                    else return 0;
                }
            });
            
            /*
             * Aufeinander folgende Distanzen vergleichen.
             * Wenn mindestens 5 hintereinander folgende Distanzen gleich sind,
             * werden diese der Liste selectedDistances hinzugefuegt.
            */
            int equalDistances = 1;
            for (int i = 1; i < distances.size(); i++) {
                if(isEqual(distances.get(i-1).getDistance(), distances.get(i).getDistance(), 0.15f)) {
                    equalDistances++;
                    if (equalDistances >= 5 && i ==  distances.size() - 1) {
                        if (distances.get(i).getDistance() > 2)
                        {
                            for (int j = i; j > i - equalDistances; j--)
                                selectedDistances.add(distances.get(j));
                        }
                    }
                }
                else if (equalDistances >= 5) {
                    if (distances.get(i).getDistance() > 2)
                    {
                        for (int j = i; j > i - equalDistances; j--)
                            selectedDistances.add(distances.get(j));
                    }
                    equalDistances = 1;
                }
                else equalDistances = 1;
            }
            
            // Die Liste selectedDistances nach Y1-Koordinaten aufsteigend sortieren
            Collections.sort(selectedDistances, new Comparator<DistancePosition>() {
                @Override
                public int compare(DistancePosition dp1, DistancePosition dp2) {
                    if (dp1.getY1() > dp2.getY1()) return 1;
                    else if (dp1.getY1() < dp2.getY1()) return -1;
                    else return 0;
                }
            });
            
            /*
             * Liste vom Ende bis zum Anfang durchlaufen und Abstand zwischen Y1-Werten
             * benachbarter Listenelemente mit deren eingetragener Distanz vergleich.
             * Falls unterschiedlich: löschen!
             */
            for (int i = selectedDistances.size() - 1; i > 0; i--) {
                if (!isEqual(selectedDistances.get(i).getY1() - selectedDistances.get(i-1).getY1(), selectedDistances.get(i).getDistance(), 0.20f)) {
                    selectedDistances.remove(i);
                }
            }
            
            // Nur die letzten 5 Elemente aus selectedDistances der Liste positions hinzufuegen.
            if (selectedDistances.size() >= 5) {
                for (int i = selectedDistances.size() - 5; i < selectedDistances.size(); i++) {
                    positions.add(new Line(selectedDistances.get(i).getX(), selectedDistances.get(i).getX(),
                        selectedDistances.get(i).getY1(), selectedDistances.get(i).getY2()));
                }
            }
        }
        
        return positions;
    }
    
    // Zwei Werte miteinander vergleichen unter Einbeziehung einer Abweichung
    private boolean isEqual(int a, int b, float variation){
        return Math.abs((float)a / (float)b - 1.0f) <= variation;
    }
}

