package algorithms;

import model.NonDominatedSet;
import model.Solution;
import model.TravelingThiefProblem;
import util.Linspace;
import util.Logspace;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

public class IndependentSubproblemAlgorithm implements Algorithm {

    private int numberOfTrials;

    public IndependentSubproblemAlgorithm (int numberOfTrials) {
        this.numberOfTrials = numberOfTrials;
    }

    @Override
    public List<Solution> solve(TravelingThiefProblem problem) throws ExecutionException, InterruptedException {

        NonDominatedSet nds = new NonDominatedSet();

        double start = problem.idealDuration / problem.nadirPoint;
        double end = problem.idealDuration * 1.1 / problem.nadirPoint;
        int steps = (int) (1.2 * numberOfTrials);
        //Logspace counter = new Logspace(start, end, steps, 100000);
        Linspace counter = new Linspace(start, end, steps);
        List<Double> cs = new ArrayList<>();
        cs.add(0.0);
        cs.add(0.7);
        cs.add(0.55);
        while (counter.hasNext()) {
            cs.add(counter.next());
        }
        cs.parallelStream().forEach((c) -> {
            KNPRunner runner = new KNPRunner(c);
            KNPAnt bestAnt = null;
            try {
                bestAnt = runner.computePackingPlan(problem);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            List<Boolean> z = new ArrayList<>(problem.numOfItems);
            for (int j = 0; j < problem.numOfItems; j++) {
                z.add(bestAnt.z[j]);
            }
            Solution s = problem.evaluate(bestAnt.pi, z, true);
            nds.add(s);
            System.out.println(nds.entries.size() + " " + c + ", TIME: " + bestAnt.tourTime + ", MAXTIME: " + (c * (problem.nadirPoint - problem.idealDuration) + problem.idealDuration) + ", PROFIT: " + bestAnt.profit + ", WEIGHT: " + bestAnt.weight);
        });
        System.out.println("Before: " + nds.entries.size());
        List<Solution> bestSolutions = nds.getBestSolutions(numberOfTrials);
        System.out.println("After: " + bestSolutions.size());
        return bestSolutions;
    }
}
