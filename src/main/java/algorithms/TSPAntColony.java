package algorithms;

import model.TravelingThiefProblem;
import util.FixedSizePriorityQueue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

public class TSPAntColony implements Callable<List<TSPAnt>> {

    private TravelingThiefProblem problem;

    // Model parameters
    private int threadNum;
    private int iterations;
    private int numAnts;

    // Choice parameters
    private float alpha;
    private float beta;
    private float qZero;

    // Pheromone update parameters
    private float rho;
    private float phi;
    private boolean bestAtIteration;

    // Model weights (learned)
    private float[][] pheromones;

    public TSPAntColony(TravelingThiefProblem problem,
                        int threadNum, int iterations, int numAnts,
                        float alpha, float beta, float qZero,
                        float rho, float phi, boolean bestAtIteration) {
        this.problem = problem;

        this.threadNum = threadNum;
        this.iterations = iterations;
        this.numAnts = numAnts;

        this.alpha = alpha;
        this.beta = beta;
        this.qZero = qZero;

        this.rho = rho;
        this.phi = phi;
        this.bestAtIteration = bestAtIteration;

        initializePheromones();
    }

    @Override
    public List<TSPAnt> call() {
        int heapSize = 10;
        FixedSizePriorityQueue<TSPAnt> minHeap = new FixedSizePriorityQueue<>(heapSize);
        double bestFitness = Double.POSITIVE_INFINITY;
        TSPAnt bestAnt = new TSPAnt(problem.numOfCities);
        for (int i = 0; i < iterations; i++) {
            double iterationBestFitness = Double.POSITIVE_INFINITY;
            TSPAnt iterationBestAnt = new TSPAnt(problem.numOfCities);

            // New ants for each iteration
            List<TSPAnt> ants = new ArrayList<>(numAnts);
            for (int j = 0; j < numAnts; j++) {
                ants.add(new TSPAnt(problem.numOfCities));
            }

            // Each ant walks simultaneously, visits each city
            for (int j = 0; j < problem.numOfCities - 1; j++) {
                for (TSPAnt ant : ants) {
                    int currentCity = ant.currentCity;
                    int nextCity = weightedChoice(ant);
                    float distance = problem.euclideanDistance(currentCity, nextCity);
                    ant.step(nextCity, distance);
                    localPheromoneUpdate(currentCity, nextCity);
                }
            }
            // Return to initial city
            for (TSPAnt ant : ants) {
                int currentCity = ant.currentCity;
                int nextCity = ant.pi.get(0);
                float distance = problem.euclideanDistance(currentCity, nextCity);
                ant.travelTime += distance;
                localPheromoneUpdate(currentCity, nextCity);
            }

            // Identify best of iteration
            for (TSPAnt ant : ants) {
                if (ant.travelTime < iterationBestFitness) {
                    iterationBestFitness = ant.travelTime;
                    iterationBestAnt = ant;
                }
            }

            // Find local optimum
            iterationBestAnt = localSearch(iterationBestAnt);
            iterationBestFitness = iterationBestAnt.travelTime;

            // Identify best so far
            if (iterationBestFitness < bestFitness) {
                bestFitness = iterationBestFitness;
                bestAnt = iterationBestAnt;
            }

            // Update pheromones
            if (bestAtIteration) {
                globalPheromoneUpdate(iterationBestAnt);
            }
            else {
                globalPheromoneUpdate(bestAnt);
            }

            // Add good solutions to min heap
            if (!minHeap.contains(iterationBestAnt)) {
                minHeap.add(iterationBestAnt);
            }
            if (i % 10 == 0) {
                System.out.println(String.format("T%d, I%d: \toverall = %f, iter = %f", threadNum, i, bestFitness, iterationBestFitness));
            }
        }

        // Add best tours to list
        List<TSPAnt> bestAnts = new ArrayList<>(heapSize);
        TSPAnt nextAnt = minHeap.pollFirst();
        while (nextAnt != null) {
            bestAnts.add(nextAnt);
            nextAnt = minHeap.pollFirst();
        }
        return bestAnts;
    }

    private float tauZero() {
        return (float) 1 / problem.maxTour;
    }

    private float deltaTau(float fitness) {
        return 1 / fitness;
    }

    private void initializePheromones() {
        int numOfCities = problem.numOfCities;

        // Initialize uniform pheromones
        pheromones = new float[numOfCities][numOfCities];
        for (float[] row : pheromones) {
            Arrays.fill(row, tauZero());
        }
    }

    private int weightedChoice(TSPAnt ant) {
        // Pseudo-proportional selection rule
        double q = Math.random();
        if (q < qZero) {
            double greedyMax = 0;
            int selectedCity = -1;
            for (int i = 0; i < problem.numOfCities; i++) {
                if (!ant.visitedCities[i]) {
                    double distance = problem.euclideanDistance(ant.currentCity, i);
                    double greedyProb = pheromones[ant.currentCity][i] * Math.pow(1 / distance, beta);
                    if (greedyProb > greedyMax) {
                        greedyMax = greedyProb;
                        selectedCity = i;
                    }
                }
            }
            return selectedCity;
        }

        // Standard proportional selection rule
        double[] probabilities = new double[problem.numOfCities];
        for (int i = 0; i < problem.numOfCities; i++) {
            if (!ant.visitedCities[i]) {
                double distance = problem.euclideanDistance(ant.currentCity, i);
                probabilities[i] = Math.pow(pheromones[ant.currentCity][i], alpha) * Math.pow(1 / distance, beta);
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
        return randomIndex;
    }

    private TSPAnt localSearch(TSPAnt ant) {
        // 2-OPT
        List<Integer> bestTour = ant.pi;
        float bestLength = ant.travelTime;
        System.out.println(String.format("Before 2OPT:", bestLength));
        boolean improved = true;
        while (improved) {
            improved = false;
            for (int i = 1; i < problem.numOfCities - 2; i++) {
                for (int j = i + 1; j < problem.numOfCities; j++) {
                    List<Integer> reversePart = bestTour.subList(i - 1, j - 1);
                    Collections.reverse(reversePart);
                    float length = tourLength(bestTour);
                    if (length < bestLength) {
                        improved = true;
                        bestLength = length;
                    } else {
                        Collections.reverse(reversePart);
                    }
                }
            }
            System.out.println("Iteration");
        }
        ant.pi = bestTour;
        ant.travelTime = bestLength;
        System.out.println(String.format("After 2OPT:", bestLength));
        return ant;
    }

    private float tourLength(List<Integer> pi) {
        float length = 0;
        for (int i = 0; i < problem.numOfCities - 1; i++) {
            length += problem.euclideanDistance(pi.get(i), pi.get(i + 1));
        }
        length += problem.euclideanDistance(pi.get(problem.numOfCities - 1), pi.get(0));
        return length;
    }

    private void localPheromoneUpdate(int currentCity, int nextCity) {
        // Bi-directional
        pheromones[currentCity][nextCity] = (1 - phi) * pheromones[currentCity][nextCity] + phi * tauZero();
        pheromones[nextCity][currentCity] = (1 - phi) * pheromones[nextCity][currentCity] + phi * tauZero();
    }

    private void globalPheromoneUpdate(TSPAnt ant) {
        float deltaTau = deltaTau(ant.travelTime);
        for (int i = 0; i < problem.numOfCities - 1; i++) {
            int from = ant.pi.get(i);
            int to = ant.pi.get(i + 1);

            // Bi-directional
            pheromones[from][to] = (1 - rho) * pheromones[from][to] + rho * deltaTau;
            pheromones[to][from] = (1 - rho) * pheromones[to][from] + rho * deltaTau;
        }
        int from = ant.pi.get(problem.numOfCities - 1);
        int to = ant.pi.get(0);

        pheromones[from][to] = (1 - rho) * pheromones[from][to] + rho * deltaTau;
        pheromones[to][from] = (1 - rho) * pheromones[to][from] + rho * deltaTau;

        // TODO: think about these ideas
        // Ideas from sudoku paper to prevent stagnation:
        // Add best value pheromone evaporation?
        // There is no global evaporation of pheromone in ACS, might want to add?
    }
}
