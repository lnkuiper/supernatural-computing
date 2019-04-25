package algorithms;

import model.NonDominatedSet;
import model.Solution;
import model.TravelingThiefProblem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AntColony {

    private TravelingThiefProblem problem;

    // Model parameters
    private int maxIterations = 100;
    private int numAnts = 10;
    private double alpha = 1;
    private double beta = 1;
    private double rho = 0.9;
    private double phi = 0.1;

    // Fitness weight parameter
    private double betaWeight = 1;

    // Model weights (learned)
    private double[][] cityPheromones;
    private double[] itemPheromones;

    public AntColony(TravelingThiefProblem problem, int maxIterations, int numAnts, double alpha, double beta,
                     double rho, double phi, double betaWeight) {
        this.problem = problem;
        this.maxIterations = maxIterations;
        this.numAnts = numAnts;
        this.alpha = alpha;
        this.beta = beta;
        this.rho = rho;
        this.phi = phi;
        this.betaWeight = betaWeight;
    }

    public Solution solve() {
        initializePheromones();
        double bestFitness = Double.POSITIVE_INFINITY;
        Ant bestAnt = new Ant(problem.numOfItems, problem.numOfCities);
        for (int i = 0; i < maxIterations; i++) {
            double iterationBest = Double.POSITIVE_INFINITY;
            // New ants for each iteration, pheromones remain
            List<Ant> ants = new ArrayList<>();
            for (int j = 0; j < numAnts; j++) {
                ants.add(new Ant(problem.numOfItems, problem.numOfCities));
            }
            // Each ant completes tour
            for (int j = 0; j < problem.numOfCities; j++) {
                for (Ant ant : ants) {
                    antStep(ant);
                }
            }

            for (Ant ant : ants) {
                double fitness = antFitness(ant);
                if (fitness < iterationBest) {
                    iterationBest = fitness;
                    bestAnt = ant;
                }
            }

            if (iterationBest < bestFitness) {
                bestFitness = iterationBest;
                System.out.println(bestFitness);
                globalCityPheromoneUpdate(bestAnt);
            }
        }

        return null;
    }

    private void antStep(Ant ant) {
        // TODO: Pick item (probability should not add up to 1?)
        // TODO: Update weight, and item decision vector z of ant
        ant.updateSpeed(problem.minSpeed, problem.maxSpeed, problem.maxWeight);

        // Make a step
        int currentCity = ant.currentCity;
        int nextCity = weightedCityChoice(ant);
        double distance = problem.euclideanDistance(currentCity, nextCity);
        ant.step(nextCity, distance);

        // Update pheromones
        localCityPheromoneUpdate(currentCity, nextCity);
        // TODO: Item pheromone update
    }

    private double cityTauZero() {
        return (double) 1 / problem.maxTour;
    }

    private double cityDeltaTau(double fitness) {
        // TODO: choose what to put here (something based on fitness)
        return 1 / fitness;
    }

    private void initializePheromones() {
        int numOfCities = problem.numOfCities;
        int numOfItems = problem.numOfItems;

        // Initialize uniform pheromones
        cityPheromones = new double[numOfCities][numOfCities];
        for (double[] row : cityPheromones) {
            Arrays.fill(row, cityTauZero());
        }

        itemPheromones = new double[numOfItems];
        Arrays.fill(itemPheromones, cityTauZero());
    }

    private void localCityPheromoneUpdate(int currentCity, int nextCity) {
        cityPheromones[currentCity][nextCity] = (1 - phi) * cityPheromones[currentCity][nextCity] + phi * cityTauZero();
    }

    private void globalCityPheromoneUpdate(Ant bestAnt) {
        // Update pheromones
        for (int i = 0; i < problem.numOfCities - 1; i++) {
            int from = bestAnt.pi.get(i);
            int to = bestAnt.pi.get(i + 1);
            double fitness = antFitness(bestAnt);
            double deltaTau = cityDeltaTau(fitness);
            cityPheromones[from][to] = (1 - rho) * cityPheromones[from][to] + rho * deltaTau;
        }

//        System.out.println(bestFitness);
//        System.out.println(bestAnt.pi.toString());
//        System.out.println(Arrays.toString(cityPheromones[0]));

        // Ideas from sudoku paper to prevent stagnation:
        // Add best value pheromone evaporation?
        // There is no global evaporation of pheromone in ACS, might want to add?
    }

    private int weightedCityChoice(Ant ant) {
        // TODO: Pseudo-proportional selection rule?

        // Weigh choice probabilities based on pheromones and distance, and parameters alpha, beta
        double[] probabilities = new double[problem.numOfCities];
        for (int i = 0; i < problem.numOfCities; i++) {
            if (!ant.visitedCities[i]) {
                double distance = problem.euclideanDistance(ant.currentCity, i);
                probabilities[i] = Math.pow(cityPheromones[ant.currentCity][i], alpha) * Math.pow(1 / distance, beta);
            }
            else {
                probabilities[i] = 0;
            }
        }

        // Choose
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

    public double antFitness(Ant ant) {
        // TODO: remove the following line when support for items is implemented, and uncomment the rest
        return ant.time;

//        double itemScore = ant.profit / problem.maxProfit;
//        double tourScore = ant.time / problem.maxTour; // TODO: this rewards long routes, needs to be inverted
//
//        // Harmonic mean (although the scores are not normalized the same, this still might be a good measure)
//        double fitness = (1 - betaWeight) * (itemScore * tourScore) / (Math.pow(betaWeight, 2) * itemScore + tourScore);
//        return fitness;
    }
}
