package util;

public class Vector {

    public double pointDistance(double ax, double ay, double bx, double by) {
        return Math.sqrt(Math.pow(Math.abs(ax - bx), 2) + Math.pow(Math.abs(ay - by), 2));
    }
}
