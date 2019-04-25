package algorithms;

import java.util.ArrayList;
import java.util.List;

public class Ant {

    public List<Integer> pi = new ArrayList<Integer>();
    public List<Boolean> z;
    public boolean[] visitedCities;
    public int currentCity = 0;
    public int profit = 0;
    public double speed;
    public double time = 0;
    public double weight = 0;

    public Ant(int numOfItems, int numOfCities) {
        this.pi.add(0);
        this.z = new ArrayList<Boolean>(numOfItems);
        this.visitedCities = new boolean[numOfCities];
        this.visitedCities[0] =  true;
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
        // Move to next city and update values
        currentCity = nextCity;
        pi.add(currentCity);
        visitedCities[currentCity] = true;
        time += distance / speed;
    }

}
