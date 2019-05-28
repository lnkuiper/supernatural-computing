package algorithms;

import model.TravelingThiefProblem;
import util.FixedSizePriorityQueue;
import util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class KNPRunner {

    private double c;

    public KNPRunner(double c) {
        this.c = c;
    }

    // TODO: cross-contaminate pheromones between colonies
    public KNPAnt computePackingPlan(TravelingThiefProblem problem)
            throws ExecutionException, InterruptedException {

        // Create as many KNPAntColonies as there are processors, and add them all to a pool
        int cores = problem.bestTours.size();
        ExecutorService pool = Executors.newFixedThreadPool(cores*2);
        List<Future<KNPAnt>> futures = new ArrayList<>();
        for (int threadNum = 0; threadNum < cores; threadNum++) {
            int numAnts = 10;
            float phi = (float) (1 / (2*numAnts));
            float qZero = (float) 0.1;
            float rho = (float) 0.15;
            Callable<KNPAnt> AC = new KNPAntColony(problem,
                    threadNum, 5, numAnts,
                    15, 30, qZero,
                    rho, phi, false,
                    1,c, threadNum);
            Future<KNPAnt> future = pool.submit(AC);
            futures.add(future);
        }

        // Identify best ant
        double bestDistance = Double.POSITIVE_INFINITY;
        KNPAnt bestAnt = new KNPAnt(problem.numOfItems, Double.POSITIVE_INFINITY);
        for (Future<KNPAnt> f : futures) {
            while (!f.isDone()) {
                Thread.sleep(50);
            }
            KNPAnt ant = f.get();
            if (ant.distanceToIdealPoint < bestDistance) {
                bestDistance = ant.distanceToIdealPoint;
                bestAnt = ant;
//                bestAnt.z = ant.z;
//                bestAnt.pi = ant.pi;
//                bestAnt.profit = ant.profit;
//                bestAnt.weight = ant.weight;
//                bestAnt.tourTime = ant.tourTime;
//                bestAnt.distanceToIdealPoint = ant.distanceToIdealPoint;
            }
        }
        return bestAnt;
    }
}
