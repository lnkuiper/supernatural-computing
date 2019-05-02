package algorithms;

import model.TravelingThiefProblem;
import util.FixedSizePriorityQueue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class KNPRunner {

    // TODO: cross-contaminate pheromones between colonies
    public boolean[] computePackingPlan(TravelingThiefProblem problem)
            throws ExecutionException, InterruptedException {

        // Create as many KNPAntColonies as there are processors, and add them all to a pool
        int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService pool = Executors.newFixedThreadPool(cores);
        List<Future<KNPAnt>> futures = new ArrayList<>();
        for (int threadNum = 0; threadNum < cores; threadNum++) {
            float antFrac = (float) 0.01;
            int numAnts = 20;//(int) (antFrac * problem.numOfItems);
            float phi = (float) (1 / (5*numAnts));
            float qZero = (float) 0.1;
            float rho = (float) 0.15;
            Callable<KNPAnt> AC = new KNPAntColony(problem,
                    threadNum, 100, numAnts,
                    15, 30, qZero,
                    rho, phi, false);
            Future<KNPAnt> future = pool.submit(AC);
            futures.add(future);
        }

        // Identify best ant
        double bestProfit = 0;
        KNPAnt bestAnt = new KNPAnt(problem.numOfItems);
        for (Future<KNPAnt> f : futures) {
            while (!f.isDone()) {
                Thread.sleep(10000);
            }
            KNPAnt ant = f.get();
            if (ant.profit > bestProfit) {
                bestAnt = ant;
            }
        }

        System.out.println();
        System.out.println(bestAnt.profit);
        System.out.println(Arrays.toString(bestAnt.z));

        return bestAnt.z;
    }
}
