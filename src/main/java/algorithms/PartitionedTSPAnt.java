package algorithms;

import java.util.ArrayList;
import java.util.List;

public class PartitionedTSPAnt implements Comparable<PartitionedTSPAnt> {

    public List<Integer> pi;
    public boolean[] visitedCities;
    public int currentCity;
    public float travelDistance = 0;

    public PartitionedTSPAnt(List<Integer> partition, int numOfCities) {
        pi = new ArrayList<>(partition.size());
        currentCity = partition.get(0);
        pi.add(currentCity);
        visitedCities = new boolean[numOfCities];
        visitedCities[currentCity] = true;
    }

    public void step(int nextCity, float distance) {
        // Move to next city and update values
        currentCity = nextCity;
        pi.add(currentCity);
        visitedCities[currentCity] = true;
        travelDistance += distance;
    }

    @Override
    public int compareTo(PartitionedTSPAnt otherAnt) {
        return -Double.compare(this.travelDistance, otherAnt.travelDistance);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TSPAnt)) {
            return false;
        }
        else {
            PartitionedTSPAnt otherAnt = (PartitionedTSPAnt) o;
            return this.compareTo(otherAnt) == 0;
        }
    }
}
