/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Robert
 */
public class DistancePosition {
    private int x, y1, y2, distance;

    public int getX() {
        return x;
    }

    public int getY1() {
        return y1;
    }

    public int getY2() {
        return y2;
    }
    
    public int getDistance() {
        return distance;
    }

    public DistancePosition(int distance, int x, int y1, int y2) {
        this.distance = distance;
        this.x = x;
        this.y1 = y1;
        this.y2 = y2;
    }
}
