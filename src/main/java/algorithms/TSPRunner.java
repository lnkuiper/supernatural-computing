package algorithms;

import model.TravelingThiefProblem;
import util.FixedSizePriorityQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class TSPRunner {

    // TODO: cross-contaminate pheromones between colonies
    public List<List<Integer>> computeTours(TravelingThiefProblem problem)
            throws ExecutionException, InterruptedException {

        // Create as many TSPAntColonies as there are processors, and add them all to a pool
        int cores = 4; //Runtime.getRuntime().availableProcessors();
        ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<List<TSPAnt>>> futures = new ArrayList<>();
        for (int threadNum = 0; threadNum < cores; threadNum++) {
            float antFrac = (float) 0.7;
            int numAnts = 50; //(int) (antFrac * problem.numOfCities);
            float phi = (float) 0.01; // (1 / (7*numAnts));
            float qZero = (float) 0.1;
            float rho = (float) 0.15;
            Callable<List<TSPAnt>> AC = new TSPAntColony(problem,
                    threadNum, 500, numAnts,
                    15, 15, qZero,
                    rho, phi, false, pool);
            Future<List<TSPAnt>> future = pool.submit(AC);
            futures.add(future);
        }

        // Wait until threads are done, add best 10 to minHeap
        FixedSizePriorityQueue<TSPAnt> minHeap = new FixedSizePriorityQueue<>((int) cores/2);
        for (Future<List<TSPAnt>> f : futures) {
            while (!f.isDone()) {
                Thread.sleep(10000);
            }
            List<TSPAnt> ants = f.get();
            minHeap.addAll(ants);
        }

        // Get tours from ants in minHeap, print and return
        System.out.println();
        List<List<Integer>> tours = new ArrayList<>();
        TSPAnt nextAnt = minHeap.pollLast();
        while (nextAnt != null) {
            System.out.println(nextAnt.travelDistance);
            System.out.println(nextAnt.getTour().toString());
            System.out.println();
            tours.add(nextAnt.getTour());
            nextAnt = minHeap.pollLast();
        }

        return tours;
    }
}
