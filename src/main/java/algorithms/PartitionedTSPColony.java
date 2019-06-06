package algorithms;

import model.TravelingThiefProblem;
import util.FixedSizePriorityQueue;
import util.SymmetricArray;

import java.util.List;
import java.util.concurrent.Callable;

public class PartitionedTSPColony implements Callable<List<Integer>> {

    private TravelingThiefProblem problem;
    private List<Integer> partition;

    private SymmetricArray pheromones;

    private int iterations;

    public PartitionedTSPColony(TravelingThiefProblem problem, List<Integer> partition) {
        this.problem = problem;
        this.partition = partition;
    }

    @Override
    public List<Integer> call() {
        int heapSize = 10;
        FixedSizePriorityQueue<PartitionedTSPAnt> minHeap = new FixedSizePriorityQueue<>(heapSize);
        double bestFitness = Double.POSITIVE_INFINITY;
        PartitionedTSPAnt bestAnt = new PartitionedTSPAnt(problem, partition, pheromones);
        for (int i = 0; i < iterations; i++) {
            double iterationBestFitness = Double.POSITIVE_INFINITY;
            PartitionedTSPAnt iterationBestAnt = bestAnt;

            // New ants for each iteration
//            List<>
        }

        return null;
    }


}
