package util;

public class Logspace {

    private double hyperFactor;
    private double start;
    private double realEnd;
    private double end;
    private int steps;
    private int currentStep = -1;
    private double ratio;

    public Logspace(double start, double end, int steps, double hyperFactor) {
        this.hyperFactor = hyperFactor;
        this.start = start;
        this.realEnd = end;
        this.end = hyperFactor * (end - start) + 1;
        this.steps = steps;
        this.ratio = Math.pow(this.end - start, (1. / (steps - 1)));
    }

    public boolean hasNext() {
        return currentStep < steps - 1;
    }

    public double next() {
        if (currentStep == steps - 2) {
            currentStep++;
            return realEnd;
        }
        else {
            currentStep++;
            return start + (Math.pow(ratio, currentStep) - 1) / hyperFactor;
        }
    }
}
