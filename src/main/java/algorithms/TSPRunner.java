package algorithms;

import model.TravelingThiefProblem;
import util.FixedSizePriorityQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class TSPRunner {

    public List<List<Integer>> computeTours(TravelingThiefProblem problem) throws ExecutionException, InterruptedException {

        // Create as many TSPAntColonies as there are processors, and add them all to a pool
        int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService pool = Executors.newFixedThreadPool(cores);
        List<Future<List<TSPAnt>>> futures = new ArrayList<>();
        for (int threadNum = 0; threadNum < cores; threadNum++) {
            double antFrac = 0.7;
            int numAnts = (int) (antFrac * problem.numOfCities);
            double phi = (double) (1 / (7*numAnts));
            Callable<List<TSPAnt>> AC = new TSPAntColony(problem,
                    threadNum, 501, numAnts,
                    15, 20, 0.1,
                    0.15, phi, false);
            Future<List<TSPAnt>> future = pool.submit(AC);
            futures.add(future);
        }

        // Wait until threads are done, add best 10 to minHeap
        FixedSizePriorityQueue<TSPAnt> minHeap = new FixedSizePriorityQueue<>(10);
        for (Future<List<TSPAnt>> f : futures) {
            while (!f.isDone()) {
                Thread.sleep(10000);
            }
            List<TSPAnt> ants = f.get();
            for (TSPAnt a : ants) {
                minHeap.add(a);
            }
        }

        // Get tours from ants in minHeap, print and return
        System.out.println();
        List<List<Integer>> tours = new ArrayList<>();
        TSPAnt nextAnt = minHeap.pollLast();
        while (nextAnt != null) {
            System.out.println(nextAnt.travelTime);
            System.out.println(nextAnt.getTour().toString());
            System.out.println();
            tours.add(nextAnt.pi);
            nextAnt = minHeap.pollLast();
        }

        return tours;
    }
}
