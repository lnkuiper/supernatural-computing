package algorithms;

import model.NonDominatedSet;
import model.Solution;
import model.TravelingThiefProblem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AntColony implements Algorithm{

    private int maxNumOfTrials = 100;
    private int numAnts = 10;
    private double alpha = 0.5;
    private double beta = 0.5;
    private double rho = 0.9;

    /*  We need to define:
        Ants (list of matrices)
        Global pheromone matrix
        List of local pheromone matrices

        Pheromone update methods (global & local)

     */

    private TravelingThiefProblem problem;

    private double[][] globalCityPheromones;
    private ArrayList<double[][]> localCityPheromones;

    private double[] globalItemPheromones;
    private ArrayList<double[]> localItemPheromones;

    public AntColony(int numberOfTrials, int numAnts, double alpha, double beta, double rho) {
        this.maxNumOfTrials = numberOfTrials;
        this.numAnts = numAnts;
        this.alpha = alpha;
        this.beta = beta;
        this.rho = rho;
    }

    @Override
    public List<Solution> solve(TravelingThiefProblem problem) {
        NonDominatedSet nds = new NonDominatedSet();

        initializePheromones(problem);

        return null;
    }

    private void initializePheromones(TravelingThiefProblem problem) {
        this.problem = problem;
        int numOfCities = problem.numOfCities;
        int numOfItems = problem.numOfItems;

        // Initialize uniform pheromones
        globalCityPheromones = new double[numOfCities][numOfCities];
        for (double[] row : globalCityPheromones) {
            Arrays.fill(row, (double)(1/numOfCities));
        }
        for (int i = 0; i < numOfCities; i++) {
            localCityPheromones.add(globalCityPheromones);
        }

        globalItemPheromones = new double[numOfItems];
        Arrays.fill(globalItemPheromones, (double)(1/numOfItems));
        for (int i = 0; i < numOfItems; i++) {
            localItemPheromones.add(globalItemPheromones);
        }

    }

    private Solution runAnt(int antIndex) {
        List<Integer> pi = new ArrayList<Integer>(1); // 0 is in there
        List<Boolean> z = new ArrayList<>(problem.numOfItems);

        int profit = 0;
        double speed = this.problem.maxSpeed;
        double time = 0;
        int weight = 0;

        boolean[] visitedCities = new boolean[this.problem.numOfCities];
        int currentCity = 0;
        for (int i = 0; i < this.problem.numOfCities - 1; i++) {
            // TODO: Pick item (probability should not add up to 1?)
            // TODO: Update weight, speed, z

            visitedCities[currentCity] = true;
            int nextCity = randomCity(localCityPheromones.get(antIndex)[currentCity], visitedCities);
            double distance = this.problem.euclideanDistance(currentCity, nextCity);
            time += distance / speed;
            currentCity = nextCity;
            pi.add(currentCity);
        }

        // TODO: Update local pheromones base on pi, z
        return this.problem.evaluate(pi, z, true);
    }

    private int randomCity (double[] pheromones, boolean[] visitedCities) {
        for (int i = 0; i < pheromones.length; i++) {
            if (visitedCities[i]) {
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
