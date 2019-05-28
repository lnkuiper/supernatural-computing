package algorithms;

import model.TravelingThiefProblem;

import java.lang.reflect.Array;
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

    // For Pareto front
    private double w;
    private double c;

    // Model weights (learned)
    private double[] pheromones;

    // Related to this colony's tour
    private List<Integer> tour;
    private double[] deltaTimes;
    private double maxItemDeltaTimes = 0;
    private double basicTourTime = 0;

    // Related to packing plan
    private boolean[] greedyPackingPlan;
    private double greedyProfit;
    private double greedyWeight;
    private int greedyNumItems;

    public KNPAntColony(TravelingThiefProblem problem,
                        int threadNum, int iterations, int numAnts,
                        float alpha, float beta, float qZero,
                        float rho, float phi, boolean bestAtIteration,
                        double w, double c, int tourIndex) {
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

        this.c = c;
        this.w = w;

        this.tour = problem.bestTours.get(tourIndex);
        this.basicTourTime = getTourTime(new boolean[problem.numOfItems]);
        this.deltaTimes = calculateDeltaTimes(tour,new boolean[problem.numOfItems]);
        this.maxItemDeltaTimes = 0;
        for (double dt : this.deltaTimes) {
            if (dt > this.maxItemDeltaTimes) {
                this.maxItemDeltaTimes = dt;
            }
        }

        initialize();

        pheromones = new double[problem.numOfItems];
        Arrays.fill(pheromones, tauZero());
        KNPAnt ant = new KNPAnt(problem.numOfItems, basicTourTime);
        ant.z = greedyPackingPlan;
        ant.weight = greedyWeight;
        ant.profit = greedyProfit;
        ant = localSearch(ant);
        for (int i = 0; i < problem.numOfItems; i++) {
            if (ant.z[i]) {
                pheromones[i] *= 3;
            }
        }
    }

    private void initialize() {
        greedyPackingPlan = new boolean[problem.numOfItems];
        double weight = 0;
        double profit = 0;
        boolean improved = true;
        while (improved) {
            improved = false;
            int bestItem = -1;
            double bestRatio = 0;
            for (int i = 0; i < problem.numOfItems; i++) {
                if (!greedyPackingPlan[i] && weight + problem.weight[i] < problem.maxWeight * c) {
                    double ratio = itemEta(i);
                    if (ratio > bestRatio) {
                        improved = true;
                        bestItem = i;
                        bestRatio = ratio;
                    }
                }

            }
            if (bestItem != -1) {
                greedyPackingPlan[bestItem] = true;
                weight += problem.weight[bestItem];
                profit += problem.profit[bestItem];
            }
        }
        greedyProfit = profit;
        greedyWeight = weight;

        double[] weightCopy = problem.weight.clone();
        Arrays.sort(weightCopy);
        weight = 0;
        int i = 0;
        while (weight + weightCopy[i] < problem.maxWeight * c) {
            weight += weightCopy[i];
            greedyNumItems++;
            i++;
        }
//        System.out.println(String.format("greedyProfit computed: %f profit, %d items", greedyProfit, greedyNumItems));
    }

    @Override
    public KNPAnt call() {
        KNPAnt bestAnt = new KNPAnt(problem.numOfItems, basicTourTime);
        double bestDistance = 0;

        for (int i = 0; i < iterations; i++) {
            double iterationBestDistance = 0;
            KNPAnt iterationBestAnt = new KNPAnt(problem.numOfItems, basicTourTime);

            //New ants for each iteration
            List<KNPAnt> ants = new ArrayList<>(numAnts);
            for (int j = 0; j < numAnts; j++) {
                ants.add(new KNPAnt(problem.numOfItems, basicTourTime));
            }
            boolean[] fullAnt = new boolean[numAnts];
            // Each ant walks simultaneously, visits each city
            for (int j = 0; j < greedyNumItems; j++) {
                int k = 0;
                for (KNPAnt ant : ants) {
                    if(!fullAnt[k]) {
                        this.deltaTimes = calculateDeltaTimes(tour,ant.z);
                        this.maxItemDeltaTimes = 0;
                        for (double dt : this.deltaTimes) {
                            if (dt > this.maxItemDeltaTimes) {
                                this.maxItemDeltaTimes = dt;
                            }
                        }

                        int nextItem = weightedChoice(ant);
                        if (nextItem == -1) {
                            fullAnt[k] = true;
                        }
                        else {
                            double weight = problem.weight[nextItem];
                            double profit = problem.profit[nextItem];
                            ant.step(nextItem, profit, weight, this.deltaTimes[nextItem]);
                            localPheromoneUpdate(nextItem);
                        }
                    }
                    k++;

                }
            }

            // Identify best of iteration
            for (KNPAnt ant : ants) {
                ant.tourTime = getTourTime(ant.z);
                ant.distanceToIdealPoint = problem.distanceToIdealPoint(ant);
                if (ant.distanceToIdealPoint > iterationBestDistance) {
                    iterationBestDistance = ant.distanceToIdealPoint;
                    iterationBestAnt = ant;
                }
            }

            iterationBestAnt = localSearch(iterationBestAnt);
            iterationBestDistance = iterationBestAnt.profit;

            // Identify best so far
            if (iterationBestDistance > bestDistance) {
                bestDistance = iterationBestDistance;
                bestAnt = iterationBestAnt;
            }


            // Update pheromones
            if (bestAtIteration) {
                globalPheromoneUpdate(iterationBestAnt);
            }
            else {
                globalPheromoneUpdate(bestAnt);
            }

//            System.out.println(String.format("T%d, I%d: \tprofit = %f, weight = %f, time = %f", threadNum, i, bestDistance, bestAnt.weight, getTourTime(bestAnt.z)));
        }
        bestAnt.pi = this.tour;
        return bestAnt;
    }



    private double tauZero() {
        return greedyProfit / 4;
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
                if (!ant.z[i] && ant.tourTime + deltaTimes[i] < c * problem.nadirPoint){
                    double eta = itemEta(i);
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
            if (!ant.z[i] && ant.tourTime + deltaTimes[i] < c * problem.nadirPoint) {
                double eta = itemEta(i);
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
                            if (ant.tourTime + deltaTimes[j] - deltaTimes[i] < c * problem.nadirPoint && problem.profit[i] <= problem.profit[j]){
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
                        ant.tourTime = ant.tourTime - deltaTimes[i] + deltaTimes[bestItem];
                        double weightChange = problem.weight[i] - problem.weight[bestItem];
                        double profitChange = problem.profit[i] - problem.profit[bestItem];
                        ant.weight -= weightChange;
                        ant.profit -= profitChange;
                        this.deltaTimes = calculateDeltaTimes(tour,ant.z);
                        this.maxItemDeltaTimes = 0;
                        for (double dt : this.deltaTimes) {
                            if (dt > this.maxItemDeltaTimes) {
                                this.maxItemDeltaTimes = dt;
                            }
                        }

                    }
                }
            }
            if (ant.profit > oldProfit)
                improved = true;
        }
        return ant;
    }


    public double[] calculateDeltaTimes(List<Integer> tour, boolean[] z){
        double currentTourTime = getTourTime(z);
        double[] deltaTimes = new double[problem.numOfItems];
//        for (int i = 0; i < problem.numOfItems; i++) {
//            if(z[i] == false){
//                z[i] = true;
//                deltaTimes[i] = getTourTime(z) - currentTourTime;
//                z[i] = false;
//            }
//            else{
//                deltaTimes[i] = Double.POSITIVE_INFINITY;
//            }
//        }
//        return deltaTimes;

        double[] weigthKnapsacks = new double[problem.numOfItems];
        for (int i = 0; i < tour.size(); i++) {
            List<Integer> items = problem.getItemsAtCity(tour.get(i));

            for (int item: items){
                if(z[item]){
                    for (int j = 0; j < problem.numOfItems; j++) {
                        weigthKnapsacks[j] += problem.weight[item];
                    }
                }else{
                    weigthKnapsacks[item] += problem.weight[item];
                }
            }

            if (i != problem.numOfCities - 1) {
                for (int j = 0; j < problem.numOfItems; j++) {
                    deltaTimes[j] +=  problem.euclideanDistance(tour.get(i), tour.get(i+1))/problem.speedFromWeight(weigthKnapsacks[j]);
                }
            }
            else{
                for (int j = 0; j < problem.numOfItems; j++) {
                    deltaTimes[j] += problem.euclideanDistance(tour.get(i), 0) / problem.speedFromWeight(weigthKnapsacks[j]);
                }
            }
        }
        for (int i = 0; i < problem.numOfItems; i++) {
            if(z[i]){
                z[i] = false;
                deltaTimes[i] = currentTourTime - getTourTime(z);
                z[i] = true;
            }
            else{
                deltaTimes[i] = deltaTimes[i] - currentTourTime;
            }
        }
        return deltaTimes;
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

    private double getTourTime(boolean[] z){
        double tourTime = 0;
        double weight = 0;
        for (int i = 0; i < problem.numOfCities; i++) {
            for (int item : problem.getItemsAtCity(tour.get(i))) {
                if(z[item]){
                    weight += problem.weight[item];
                }
            }
            double speed = problem.speedFromWeight(weight);
            tourTime += problem.euclideanDistance(tour.get(i), tour.get((i+1)%problem.numOfCities)) / speed;
        }
        return tourTime;
    }

    public double itemEta(int i) {
        return (problem.profit[i]/problem.maxItemProfit) / (w * problem.weight[i]/problem.maxItemWeight + (1 - w) * deltaTimes[i]/maxItemDeltaTimes);
    }
}
