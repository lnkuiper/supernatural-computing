package algorithms;

import model.TravelingThiefProblem;
import util.SymmetricArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

public class PartitionedTSPAnt implements Comparable<PartitionedTSPAnt>, Callable<PartitionedTSPAnt> {

    TravelingThiefProblem problem;
    List<Integer> partition;

    public List<Integer> pi;
    public boolean[] visitedCities;
    public int currentCity;
    public float travelDistance = 0;

    private double alpha;
    private double beta;
    private double qZero;

    private SymmetricArray pheromones;

    public PartitionedTSPAnt(TravelingThiefProblem problem, List<Integer> partition, SymmetricArray pheromones) {
        this.problem = problem;
        this.partition = partition;
        pi = new ArrayList<>();
        currentCity = partition.get(0);
        visitedCities = new boolean[problem.numOfCities];
        visitedCities[currentCity] = true;
        this.pheromones = pheromones;
    }

    @Override
    public PartitionedTSPAnt call() {
        for (int i = 0; i < partition.size(); i++) {
            int nextCity = weightedChoice();
            travelDistance += problem.euclideanDistance(currentCity, nextCity);
            currentCity = nextCity;
            visitedCities[currentCity] = true;
        }
        return this;
    }

    private int weightedChoice() {
        // Pseudo-proportional
        double q = Math.random();
        if (q < qZero) {
            double greedyMax = 0;
            int selectedCity = -1;
            for (int i = 0; i < partition.size(); i++) {
                int c = partition.get(i);
                if (!visitedCities[c]) {
                    double distance = problem.euclideanDistance(currentCity, c);
                    double greedyProb = pheromones.get(currentCity, c) * Math.pow(1 / distance, beta);
                    if (greedyProb > greedyMax) {
                        greedyMax = greedyProb;
                        selectedCity = c;
                    }
                }
            }
            return selectedCity;
        }

        // Standard proportional
        double[] probabilities = new double[problem.numOfCities];
        for (int i = 0; i < partition.size(); i++) {
            int c = partition.get(i);
            if (!visitedCities[c]) {
                double distance = problem.euclideanDistance(currentCity, c);
                probabilities[c] = Math.pow(pheromones.get(currentCity, c), alpha) * Math.pow(1 / distance, beta);
            }
        }

        // Choose using Math.random()
        double totalWeight = 0.0d;
        for (double prob : probabilities) {
            totalWeight += prob;
        }
        int randomIndex = -1;
        double random = Math.random() * totalWeight;
        for (int i = 0; i < problem.numOfCities; i++) {
            random -= probabilities[i];
            if (random <= 0.0d) {
                randomIndex = i;
                break;
            }
        }
        return randomIndex;
    }

    private void localSearch() {
        boolean improved = true;
        while (improved) {
            improved = false;
            for (int i = 1; i < partition.size() - 2; i++) {
                for (int j = i + 1; j < partition.size() - 1; j++) {
                    float originalDist = problem.euclideanDistance(pi.get(i-1), pi.get(i)) + problem.euclideanDistance(pi.get(j), pi.get(j+1));
                    float proposedDist = problem.euclideanDistance(pi.get(i), pi.get(j+1)) + problem.euclideanDistance(pi.get(j), pi.get(i-1));

                    if (proposedDist < originalDist) {
                        List<Integer> reversePart = pi.subList(i, j);
                        Collections.reverse(reversePart);
                        travelDistance = travelDistance - originalDist + proposedDist;
                    }
                }
            }
        }
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
