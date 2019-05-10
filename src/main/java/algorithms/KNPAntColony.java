package algorithms;

import model.TravelingThiefProblem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

public class KNPAntColony implements Callable<KNPAnt> {

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
    private double[] pheromones;

    public KNPAntColony(TravelingThiefProblem problem,
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

        pheromones = new double[problem.numOfItems];
        Arrays.fill(pheromones, tauZero());
        KNPAnt ant = new KNPAnt(problem.numOfItems);
        ant.z = problem.greedyPackingPlan;
        ant.weight = problem.greedyWeight;
        ant.profit = problem.greedyProfit;
        ant = localSearch(ant);
        for (int i = 0; i < problem.numOfItems; i++) {
            if (ant.z[i]) {
                pheromones[i] *= 3;
            }
        }
    }

    @Override
    public KNPAnt call() {
        KNPAnt bestAnt = new KNPAnt(problem.numOfItems);
        double bestProfit = 0;

        for (int i = 0; i < iterations; i++) {
            double iterationBestProfit = 0;
            KNPAnt iterationBestAnt = new KNPAnt(problem.numOfItems);

            //New ants for each iteration
            List<KNPAnt> ants = new ArrayList<>(numAnts);
            for (int j = 0; j < numAnts; j++) {
                ants.add(new KNPAnt(problem.numOfItems));
            }
            boolean[] fullAnt = new boolean[numAnts];
            // Each ant walks simultaneously, visits each city
            for (int j = 0; j < problem.greedyNumItems; j++) {
                int k = 0;
                for (KNPAnt ant : ants) {
                    if(!fullAnt[k]) {
                        int nextItem = weightedChoice(ant);
                        if (nextItem == -1) {
                            fullAnt[k] = true;
                        }
                        else {
                            double weight = problem.weight[nextItem];
                            double profit = problem.profit[nextItem];
                            ant.step(nextItem, profit, weight);
                            localPheromoneUpdate(nextItem);
                        }
                    }
                    k++;
                }
            }

            // Identify best of iteration
            for (KNPAnt ant : ants) {
                if (ant.profit > iterationBestProfit) {
                    iterationBestProfit = ant.profit;
                    iterationBestAnt = ant;
                }
            }

            iterationBestAnt = localSearch(iterationBestAnt);
            iterationBestProfit = iterationBestAnt.profit;

            // Identify best so far
            if (iterationBestProfit > bestProfit) {
                bestProfit = iterationBestProfit;
                bestAnt = iterationBestAnt;
            }


            // Update pheromones
            if (bestAtIteration) {
                globalPheromoneUpdate(iterationBestAnt);
            }
            else {
                globalPheromoneUpdate(bestAnt);
            }

            System.out.println(String.format("T%d, I%d: \tprofit = %f, weight = %f", threadNum, i, bestProfit, bestAnt.weight));
        }
        return bestAnt;
    }

    private double tauZero() {
        return problem.greedyProfit / 4;
    }

    private double deltaTau(double profit) {
        return profit;
    }

    private int weightedChoice(KNPAnt ant) {
        // Pseudo-proportional selection rule
        double q = Math.random();
        if (q < qZero) {
            double bestProb = 0;
            int bestItem = -1;
            for (int i = 0; i < problem.numOfItems; i++) {
                if (!ant.z[i] && ant.weight + problem.weight[i] <= problem.maxWeight) {
                    double eta = problem.itemEta(i);
                    double prob = pheromones[i] * Math.pow(eta, beta);
                    if (prob > bestProb) {
                        bestProb = prob;
                        bestItem = i;
                    }
                }
            }
            return bestItem;
        }

        // Standard proportional selection rule
        double[] probabilities = new double[problem.numOfItems];
        for (int i = 0; i < problem.numOfItems; i++) {
            if (!ant.z[i] && ant.weight + problem.weight[i] <= problem.maxWeight) {
                double eta = problem.itemEta(i);
                probabilities[i] = Math.pow(pheromones[i], alpha) * Math.pow(eta, beta);
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
        for (int i = 0; i < problem.numOfItems; i++) {
            random -= probabilities[i];
            if (random < 0.0d) {
                randomIndex = i;
                break;
            }
        }
        return randomIndex;
    }

    private KNPAnt localSearch(KNPAnt ant) {
        // Bit flip
        boolean improved = true;
        while (improved) {
            double oldProfit = ant.profit;
            improved = false;
            for (int i = 0; i < problem.numOfItems; i++) {
                if (ant.z[i]) {
                    // Identify best swap (if legal)
                    int bestItem = -1;
                    double bestProfit = 0;
                    for (int j = 0; j < problem.numOfItems; j++) {
                        if (!ant.z[j]) {
                            if (problem.weight[i] + problem.maxWeight - ant.weight >= problem.weight[j] && problem.profit[i] <= problem.profit[j]) {
                                if (problem.profit[j] > bestProfit) {
                                    bestItem = j;
                                    bestProfit = problem.profit[j];
                                }
                            }
                        }
                    }
                    // Swap
                    if (bestItem != -1) {
                        ant.z[i] = false;
                        ant.z[bestItem] = true;
                        double weightChange = problem.weight[i] - problem.weight[bestItem];
                        double profitChange = problem.profit[i] - problem.profit[bestItem];
                        ant.weight -= weightChange;
                        ant.profit -= profitChange;
                    }
                }
            }
            if (ant.profit > oldProfit)
                improved = true;
        }
        return ant;
    }


    private void localPheromoneUpdate(int nextItem) {
        pheromones[nextItem] = (1 - phi) * pheromones[nextItem] + phi * tauZero();
    }

    private void globalPheromoneUpdate(KNPAnt ant) {
        double deltaTau = deltaTau(ant.profit);
        for (int i = 0; i < problem.numOfItems - 1; i++) {
            if(ant.z[i]){
                pheromones[i] = (1 - rho) * pheromones[i] + rho * deltaTau;
            }
        }

        // TODO: think about these ideas
        // Ideas from sudoku paper to prevent stagnation:
        // Add best profit pheromone evaporation?
        // There is no global evaporation of pheromone in ACS, might want to add?
    }
}
