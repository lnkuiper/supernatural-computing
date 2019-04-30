package algorithms;

import model.TravelingThiefProblem;
import util.FixedSizePriorityQueue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TSPAntColony {

    private TravelingThiefProblem problem;

    // Model parameters
    private int iterations;
    private int restarts;
    private int numAnts;

    // Choice parameters
    private double alpha;
    private double beta;
    private double qZero;

    // Pheromone update parameters
    private double rho;
    private double phi;
    private boolean bestAtIteration;

    // Model weights (learned)
    private double[][] pheromones;

    public TSPAntColony(TravelingThiefProblem problem,
                        int restarts, int iterations, int numAnts,
                        double alpha, double beta, double qZero,
                        double rho, double phi, boolean bestAtIteration) {
        this.problem = problem;

        this.iterations = iterations;
        this.restarts = restarts;
        this.numAnts = numAnts;

        this.alpha = alpha;
        this.beta = beta;
        this.qZero = qZero;

        this.rho = rho;
        this.phi = phi;
        this.bestAtIteration = bestAtIteration;
    }

    public List<List<Integer>> computeTours() {
        // TODO: change arbitrary maxSize (variable?)
        int maxSize = 10;
        FixedSizePriorityQueue<TSPAnt> minHeap = new FixedSizePriorityQueue<>(maxSize);

        for (int i = 0; i < restarts; i++) {
            double bestFitness = Double.POSITIVE_INFINITY;
            TSPAnt bestAnt = new TSPAnt(problem.numOfCities);
            initializePheromones();
            for (int j = 0; j < iterations; j++) {
                double iterationBestFitness = Double.POSITIVE_INFINITY;
                TSPAnt iterationBestAnt = new TSPAnt(problem.numOfCities);

                // New ants for each iteration
                List<TSPAnt> ants = new ArrayList<>(numAnts);
                for (int k = 0; k < numAnts; k++) {
                    ants.add(new TSPAnt(problem.numOfCities));
                }

                // Each ant walks simultaneously, visits each city
                for (int k = 0; k < problem.numOfCities - 1; k++) {
                    for (TSPAnt ant : ants) {
                        int currentCity = ant.currentCity;
                        int nextCity = weightedChoice(ant);
                        double distance = problem.euclideanDistance(currentCity, nextCity);
                        ant.step(nextCity, distance);
                        localPheromoneUpdate(currentCity, nextCity);
                    }
                }
                // Return to initial city
                for (TSPAnt ant : ants) {
                    int currentCity = ant.currentCity;
                    int nextCity = ant.pi.get(0);
                    double distance = problem.euclideanDistance(currentCity, nextCity);
                    ant.time += distance;
                    localPheromoneUpdate(currentCity, nextCity);
                }

                // TODO: add hill-climb somewhere here?

                // Identify best of iteration
                for (TSPAnt ant : ants) {
                    if (ant.time < iterationBestFitness) {
                        iterationBestFitness = ant.time;
                        iterationBestAnt = ant;
                    }
                }

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
                System.out.println(String.format("R%d, I%d: \toverall = %f, iter = %f", i, j, bestFitness, iterationBestFitness));
            }
        }
        System.out.println();

        // Add best tours to list
        List<List<Integer>> bestTours = new ArrayList<>(maxSize);
        TSPAnt nextAnt = minHeap.pollFirst();
        while (nextAnt != null) {
            System.out.println(nextAnt.time);
            System.out.println(nextAnt.getTour().toString());
            System.out.println();
            bestTours.add(nextAnt.getTour());
            nextAnt = minHeap.pollFirst();
        }
        return bestTours;
    }

    private double tauZero() {
        return (double) 1 / problem.maxTour;
    }

    private double deltaTau(double fitness) {
        return 1 / fitness;
    }

    private void initializePheromones() {
        int numOfCities = problem.numOfCities;
        int numOfItems = problem.numOfItems;

        // Initialize uniform pheromones
        pheromones = new double[numOfCities][numOfCities];
        for (double[] row : pheromones) {
            Arrays.fill(row, tauZero());
        }
    }

    private void localPheromoneUpdate(int currentCity, int nextCity) {
        // TODO: bi-directional y/n?
        pheromones[currentCity][nextCity] = (1 - phi) * pheromones[currentCity][nextCity] + phi * tauZero();
        pheromones[nextCity][currentCity] = (1 - phi) * pheromones[nextCity][currentCity] + phi * tauZero();
    }

    private void globalPheromoneUpdate(TSPAnt ant) {
        for (int i = 0; i < problem.numOfCities - 1; i++) {
            int from = ant.pi.get(i);
            int to = ant.pi.get(i + 1);
            double deltaTau = deltaTau(ant.time);

            // TODO: bi-directional y/n?
            pheromones[from][to] = (1 - rho) * pheromones[from][to] + rho * deltaTau;
            pheromones[to][from] = (1 - rho) * pheromones[to][from] + rho * deltaTau;
        }

        // TODO: think about these ideas
        // Ideas from sudoku paper to prevent stagnation:
        // Add best value pheromone evaporation?
        // There is no global evaporation of pheromone in ACS, might want to add?
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
}
