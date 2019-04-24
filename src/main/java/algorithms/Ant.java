package algorithms;

import java.util.ArrayList;
import java.util.List;

public class Ant {

    public List<Integer> pi = new ArrayList<Integer>();
    public List<Boolean> z;
    public List<Boolean> visitedCities;
    public int currentCity = 0;
    public int profit = 0;
    public double speed;
    public double time = 0;
    public double weight = 0;

    public Ant(int numOfItems, int numOfCities) {
        this.pi.add(0);
        this.z = new ArrayList<Boolean>(numOfItems);
        this.visitedCities = new ArrayList<Boolean>(numOfCities);
        this.visitedCities.set(0, true);
    }

    public void updateSpeed(double minSpeed, double maxSpeed, int maxWeight) {
        if (weight > maxWeight) {
            speed = minSpeed;
        }
        else {
            speed = maxSpeed - weight / maxWeight * (maxSpeed - minSpeed);
        }
    }

    public void step(int nextCity, double distance) {
        // Keep track of visited cities
        visitedCities.set(currentCity, true);

        // Move to next city and update values
        currentCity = nextCity;
        pi.add(currentCity);
        time += distance / speed;
    }

}