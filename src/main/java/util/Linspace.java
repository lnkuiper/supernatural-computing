package util;

public class Linspace {

    private double current;
    private double stepsize;
    private double end;
    private int steps;

    public Linspace(double start, double end, int steps) {
        this.stepsize = (end - start) / (steps - 1);
        this.current = start - this.stepsize;
        this.end = end;
        this.steps = steps;
    }

    public boolean hasNext() {
        return steps > 0;
    }

    public double next() {
        current += stepsize;
        steps -= 1;
        if (steps <= 0) {
            return end;
        }
        else {
            return current;
        }
    }
}
