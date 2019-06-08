package algorithms;

import model.TravelingThiefProblem;
import util.SymmetricArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

public class PartitionedTSPColony implements Callable<List<Integer>> {

    private TravelingThiefProblem problem;
    private List<Integer> partition;

    private SymmetricArray pheromones;
    private float tauZero;

    private int iterations = 50;
    private int numAnts;

    private double alpha = 15;
    private double beta = 15;
    private float phi = (float) 0.01;
    private float rho = (float) 0.15;
    private double qZero = 0.1;

    public PartitionedTSPColony(TravelingThiefProblem problem, List<Integer> partition) {
        this.problem = problem;
        this.numAnts = (int) (0.7 * problem.numOfCities);
        this.partition = partition;
        initialize();
    }

    private void initialize() {
        double length = 0;
        for (int i = 0; i < partition.size() - 1; i++) {
            length += problem.euclideanDistance(partition.get(i), partition.get(i+1));
        }
        tauZero = (float) (1 / (length * 4));
        pheromones = new SymmetricArray(problem.numOfCities, tauZero);


        for (int i = 0; i < partition.size() - 1; i++) {
            pheromones.set(partition.get(i), partition.get(i+1), tauZero * 3);
        }
    }

    private float deltaTau(float fitness) {
        return 1 / fitness;
    }

    @Override
    public List<Integer> call() {
        double bestFitness = Double.POSITIVE_INFINITY;
        PartitionedTSPAnt bestAnt = new PartitionedTSPAnt(partition, problem.numOfCities);
        for (int i = 0; i < iterations; i++) {
            double iterationBestFitness = Double.POSITIVE_INFINITY;
            PartitionedTSPAnt iterationBestAnt = bestAnt;

            // New ants for each iteration
            List<PartitionedTSPAnt> ants = new ArrayList<>(numAnts);
            for (int j = 0; j < numAnts; j++) {
                ants.add(new PartitionedTSPAnt(partition, problem.numOfCities));
            }

            // Visit each city except the last one
            for (int j = 0; j < partition.size() - 2; j++) {
                for (PartitionedTSPAnt ant : ants) {
                    int nextCity = weightedChoice(ant);
                    float distance = problem.euclideanDistance(ant.currentCity, nextCity);
                    ant.step(nextCity, distance);
                    localPheromoneUpdate(ant.currentCity, nextCity);
                }
            }

            // Go to final city
            for (PartitionedTSPAnt ant : ants) {
                int nextCity = partition.get(partition.size() - 1);
                float distance = problem.euclideanDistance(ant.currentCity, nextCity);
                ant.step(nextCity, distance);
                localPheromoneUpdate(ant.currentCity, nextCity);
            }

            // Identify best
            for (PartitionedTSPAnt ant : ants) {
                if (ant.travelDistance < iterationBestFitness) {
                    iterationBestFitness = ant.travelDistance;
                    iterationBestAnt = ant;
                }
            }

            double beforeTime = iterationBestAnt.travelDistance;
            iterationBestAnt = localSearch(iterationBestAnt);
            iterationBestFitness = iterationBestAnt.travelDistance;

            // Identify best so far
            if (iterationBestFitness < bestFitness) {
                bestFitness = iterationBestFitness;
                bestAnt = iterationBestAnt;
            }
            globalPheromoneUpdate(bestAnt);
        }

        return bestAnt.pi;
    }

    private int weightedChoice(PartitionedTSPAnt ant) {
        // Pseudo-proportional
        double q = Math.random();
        if (q < qZero) {
            double greedyMax = -1;
            int selectedCity = -1;
            for (int i = 0; i < partition.size() - 1; i++) {
                int c = partition.get(i);
                if (!ant.visitedCities[c]) {
                    double distance = problem.euclideanDistance(ant.currentCity, c);
                    double greedyProb = pheromones.get(ant.currentCity, c) * Math.pow(1 / distance, beta);
                    if (greedyProb > greedyMax) {
                        greedyMax = greedyProb;
                        selectedCity = c;
                    }
                }
            }
            return selectedCity;
        }

        // Standard proportional
        double[] probabilities = new double[partition.size() - 1];
        for (int i = 0; i < partition.size() - 1; i++) {
            int c = partition.get(i);
            if (!ant.visitedCities[c]) {
                double distance = problem.euclideanDistance(ant.currentCity, c);
                probabilities[i] = Math.pow(pheromones.get(ant.currentCity, c), alpha) * Math.pow(1 / distance, beta);
            }
            else {
                probabilities[i] = 0;
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

        return partition.get(randomIndex);
    }

    private PartitionedTSPAnt localSearch(PartitionedTSPAnt ant) {
        boolean improved = true;
        while (improved) {
            improved = false;
            for (int i = 1; i < partition.size() - 2; i++) {
                for (int j = i + 1; j < partition.size() - 2; j++) {
                    float originalDist = problem.euclideanDistance(ant.pi.get(i-1), ant.pi.get(i)) + problem.euclideanDistance(ant.pi.get(j), ant.pi.get(j+1));
                    float proposedDist = problem.euclideanDistance(ant.pi.get(i), ant.pi.get(j+1)) + problem.euclideanDistance(ant.pi.get(j), ant.pi.get(i-1));

                    if (proposedDist < originalDist) {
                        List<Integer> reversePart = ant.pi.subList(i, j + 1);
                        Collections.reverse(reversePart);
                        ant.travelDistance = ant.travelDistance - originalDist + proposedDist;
                    }
                }
            }
        }
        return ant;
    }

    private void localPheromoneUpdate(int currentCity, int nextCity) {
        float pheromoneVal = (1 - phi) * pheromones.get(currentCity, nextCity) + phi * tauZero;
        pheromones.set(currentCity, nextCity, pheromoneVal);
    }

    private void globalPheromoneUpdate(PartitionedTSPAnt ant) {
        float deltaTau = deltaTau(ant.travelDistance);
        for (int i = 0; i < partition.size() - 1; i++) {
            int from = ant.pi.get(i);
            int to = ant.pi.get(i + 1);

            // Bi-directional
            float pheromoneVal = (1 - rho) * pheromones.get(from, to) + rho * deltaTau;
            pheromones.set(from, to, pheromoneVal);
        }
    }
}
