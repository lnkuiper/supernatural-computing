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
    private int maxIterations = 1000;
    private int numAnts = 10;
    private double alpha = 0.5;
    private double beta = 0.5;
    private double rho = 0.9;
    private double phi = 0.1;

    // Model weights
    private double[][] cityPheromones;
    private double[] itemPheromones;

    public AntColony(TravelingThiefProblem problem, int maxIterations, int numAnts, double alpha, double beta, double rho, double phi) {
        this.problem = problem;
        this.maxIterations = maxIterations;
        this.numAnts = numAnts;
        this.alpha = alpha;
        this.beta = beta;
        this.rho = rho;
        this.phi = phi;
    }

    public Solution solve() {
        initializePheromones();
        for (int i = 0; i < maxIterations; i++) {
            // New ants for each iteration, pheromones remain
            List<Ant> ants = new ArrayList<Ant>(numAnts);
            for (int j = 0; j < numAnts; i++) {
                ants.set(j, new Ant(problem.numOfItems, problem.numOfCities));
            }

            // Each ant completes tour
            for (int j = 0; j < problem.numOfCities; j++) {
                for (Ant ant : ants) {
                    antStep(ant);
                }
            }
            globalCityPheromoneUpdate(ants);
        }

        return null;
    }

    private void antStep(Ant ant) {
        // TODO: Pick item (probability should not add up to 1?)
        // TODO: Update weight, and item decision vector z
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
        return (double) 1 / this.problem.numOfCities;
    }

    private double cityDeltaTau() {
        // TODO: choose what to put here (something based on fitness)
        return 1;
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

    private void globalCityPheromoneUpdate(List<Ant> ants) {
        // TODO: identify best ant (fitness?)
        Ant bestAnt = ants.get(0);
        for (int i = 0; i < problem.numOfCities - 1; i++) {
            int from = bestAnt.pi.get(i);
            int to = bestAnt.pi.get(i + 1);
            cityPheromones[from][to] = (1 - rho) * cityPheromones[from][to] + rho * cityDeltaTau();
        }
        // Add best value evaporation as with sudoku?
        // There is no global evaporation of pheromone in ACS, might want to add, again just like sudoku?
    }

    private int weightedCityChoice(Ant ant) {
        double[] probabilities = new double[problem.numOfCities];
        for (int i = 0; i < problem.numOfCities; i++) {
            if (!ant.visitedCities.get(i)) {
                // TODO: distance "changes" based on current weight?
                double distance = problem.euclideanDistance(ant.currentCity, i);
                probabilities[i] = Math.pow(cityPheromones[ant.currentCity][i], alpha) * Math.pow(1 / distance, beta);
            }
        }

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
