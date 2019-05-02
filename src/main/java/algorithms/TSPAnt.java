package algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class TSPAnt implements Comparable<TSPAnt>{

    public List<Integer> pi;
    public boolean[] visitedCities;
    public int currentCity;
    public float travelTime = 0;

    public TSPAnt(int numOfCities) {
        this.pi = new ArrayList<>(numOfCities);
        this.visitedCities = new boolean[numOfCities];
        this.currentCity = ThreadLocalRandom.current().nextInt(0, numOfCities);
        this.pi.add(currentCity);
        this.visitedCities[currentCity] = true;
    }

    public void step(int nextCity, float distance) {
        // Move to next city and update values
        currentCity = nextCity;
        pi.add(currentCity);
        visitedCities[currentCity] = true;
        travelTime += distance;
    }

    public List<Integer> getTour() {
        List<Integer> tour = new ArrayList<>();
        int indexOfZero = pi.indexOf(0);
        List<Integer> partOne = pi.subList(indexOfZero, pi.size());
        List<Integer> partTwo = pi.subList(0, indexOfZero);
        tour.addAll(partOne);
        tour.addAll(partTwo);
        return tour;
    }

    @Override
    public int compareTo(TSPAnt otherAnt) {
        return -Double.compare(this.travelTime, otherAnt.travelTime);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TSPAnt)) {
            return false;
        }
        else {
            TSPAnt otherAnt = (TSPAnt) o;
            return this.compareTo(otherAnt) == 0;
        }
    }
}
