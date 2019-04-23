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
        List<Ant> ants = new ArrayList<Ant>(numAnts);
        for (int i = 0; i < numAnts; i++) {
            ants.set(i, new Ant(problem.numOfItems, problem.numOfCities));
        }
        initializePheromones();


        for (int i = 0; i < maxIterations; i++) {
            for (int j = 0; j < problem.numOfCities; j++) {
                for (Ant ant : ants) {
                    antStep(ant);
                }
            }
            // TODO: Global pheromone update
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

    private double tauZero() {
        return (double) 1 / this.problem.numOfCities;
    }

    private void initializePheromones() {
        int numOfCities = problem.numOfCities;
        int numOfItems = problem.numOfItems;

        // Initialize uniform pheromones
        cityPheromones = new double[numOfCities][numOfCities];
        for (double[] row : cityPheromones) {
            Arrays.fill(row, tauZero());
        }

        itemPheromones = new double[numOfItems];
        Arrays.fill(itemPheromones, tauZero());
    }

    private void localCityPheromoneUpdate(int currentCity, int nextCity) {
        cityPheromones[currentCity][nextCity] = (1 - phi) * cityPheromones[currentCity][nextCity] + phi * tauZero();
    }

    private void globalCityPheromoneUpdate(List<Ant> ants) {
        // TODO: identify best ant and update pheromones accordingly
    }

    private int weightedCityChoice(Ant ant) {
        // TODO: use alpha and beta parameters for making this choice
        double[] pheromones = cityPheromones[ant.currentCity];
        for (int i = 0; i < pheromones.length; i++) {
            if (ant.visitedCities.get(i)) {
                pheromones[i] = 0;
            }
        }
        double totalWeight = 0.0d;
        for (double pheromone : pheromones) {
            totalWeight += pheromone;
        }
        int randomIndex = -1;
        double random = Math.random() * totalWeight;
        for (int i = 0; i < pheromones.length; i++) {
            random -= pheromones[i];
            if (random <= 0.0d) {
                randomIndex = i;
                break;
            }
        }
        return randomIndex;
    }
}
